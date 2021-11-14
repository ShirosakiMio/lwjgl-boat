/*
 * Copyright (c) 2002-2008 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.lwjgl.opengl;

/**
 * @author elias_naur
 */

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Keyboard;

final class BoatKeyboard {
	private static final int LockMapIndex                      = 1;
	private static final long NoSymbol = 0;
	private static final long ShiftMask = 1 << 0;
	private static final long LockMask = 1 << 1;

	private static final int KEYBOARD_BUFFER_SIZE = 50;

	private final int numlock_mask;
	private final int modeswitch_mask;
	private final int caps_lock_mask;
	private final int shift_lock_mask;

	private final byte[] key_down_buffer = new byte[Keyboard.KEYBOARD_SIZE];
	private final EventQueue event_queue = new EventQueue(Keyboard.EVENT_SIZE);

	private final ByteBuffer tmp_event = ByteBuffer.allocate(Keyboard.EVENT_SIZE);
	private final int[] temp_translation_buffer = new int[KEYBOARD_BUFFER_SIZE];

	// Deferred key released event, to detect key repeat
	private boolean has_deferred_event;
	private int deferred_keycode;
	private int deferred_event_keycode;
	private long deferred_nanos;
	private byte deferred_key_state;

	BoatKeyboard(long display, long window) {
	}

	public void destroy(long display) {
	}

	public void read(ByteBuffer buffer) {
		flushDeferredEvent();
		event_queue.copyEvents(buffer);
	}

	public void poll(ByteBuffer keyDownBuffer) {
		flushDeferredEvent();
		int old_position = keyDownBuffer.position();
		keyDownBuffer.put(key_down_buffer);
		keyDownBuffer.position(old_position);
	}

	private void putKeyboardEvent(int keycode, byte state, int ch, long nanos, boolean repeat) {
		tmp_event.clear();
		tmp_event.putInt(keycode).put(state).putInt(ch).putLong(nanos).put(repeat ? (byte)1 : (byte)0);
		tmp_event.flip();
		event_queue.putEvent(tmp_event);
	}

	private void translateEvent(long event_ptr, int keycode, byte key_state, long nanos, boolean repeat) {
		int num_chars, i;
		int ch;

		num_chars = lookupString(event_ptr, temp_translation_buffer);
		if (num_chars > 0) {
			ch = temp_translation_buffer[0];
			putKeyboardEvent(keycode, key_state, ch, nanos, repeat);
			for (i = 1; i < num_chars; i++) {
				ch = temp_translation_buffer[i];
				putKeyboardEvent(0, (byte)0, ch, nanos, repeat);
			}
		} else {
			putKeyboardEvent(keycode, key_state, 0, nanos, repeat);
		}
	}

	private int getKeycode(long event_ptr, int event_state) {
		int keycode = BoatKeycodes.mapBoatKeyCodeToLWJGLKeyCode(keysym);
		return keycode;
	}

	private static byte getKeyState(int event_type) {
		switch (event_type) {
			case LinuxEvent.KeyPress:
				return 1;
			case LinuxEvent.KeyRelease:
				return 0;
			default:
				throw new IllegalArgumentException("Unknown event_type: " + event_type);
		}
	}

	/** This is called when the window loses focus: we release all currently pressed keys. */
	void releaseAll() {
		for ( int i = 0; i < key_down_buffer.length; i++ ) {
			if ( key_down_buffer[i] != 0 ) {
				key_down_buffer[i] = 0;
				putKeyboardEvent(i, (byte)0, 0, 0L, false);
			}
		}
	}

	private void handleKeyEvent(long event_ptr, long millis, int event_type, int event_keycode, int event_state) {
		int keycode = getKeycode(event_ptr, event_state);
		byte key_state = getKeyState(event_type);
		boolean repeat = key_state == key_down_buffer[keycode];
		if ( repeat && event_type == LinuxEvent.KeyRelease ) // This can happen for modifier keys after losing and regaining focus.
			return;
		key_down_buffer[keycode] = key_state;
		long nanos = millis*1000000;
		if (event_type == LinuxEvent.KeyPress) {
			if (has_deferred_event) {
				if (nanos == deferred_nanos && event_keycode == deferred_event_keycode) {
					has_deferred_event = false;
					repeat = true; // Repeated event
				} else
					flushDeferredEvent();
			}
			translateEvent(event_ptr, keycode, key_state, nanos, repeat);
		} else {
			flushDeferredEvent();
			has_deferred_event = true;
			deferred_keycode = keycode;
			deferred_event_keycode = event_keycode;
			deferred_nanos = nanos;
			deferred_key_state = key_state;
		}
	}

	private void flushDeferredEvent() {
		if (has_deferred_event) {
			putKeyboardEvent(deferred_keycode, deferred_key_state, 0, deferred_nanos, false);
			has_deferred_event = false;
		}
	}

	public boolean filterEvent(LinuxEvent event) {
		switch (event.getType()) {
			case LinuxEvent.KeyPress: /* Fall through */
			case LinuxEvent.KeyRelease:
				handleKeyEvent(event.getKeyAddress(), event.getKeyTime(), event.getKeyType(), event.getKeyKeyCode(), event.getKeyState());
				return true;
			default:
				break;
		}
		return false;
	}
}
