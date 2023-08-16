LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE            := boat
LOCAL_SRC_FILES         := ./libboat.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE            := glfw
LOCAL_SHARED_LIBRARIES  := boat
LOCAL_SRC_FILES         := src/context.c \
                           src/init.c \
                           src/input.c \
                           src/monitor.c \
                           src/vulkan.c \
                           src/window.c \
                           src/boat_init.c \
                           src/boat_monitor.c \
                           src/boat_window.c \
                           src/egl_context.c \
                           src/osmesa_context.c \
                           src/posix_thread.c \
                           src/posix_time.c \
                           src/null_joystick.c
LOCAL_C_INCLUDES        := $(LOCAL_PATH)/include/GLFW \
                           $(LOCAL_PATH)/include/boat
LOCAL_CFLAGS            := -Wall -D_GLFW_BOAT
LOCAL_LDLIBS            := -llog -ldl -landroid
include $(BUILD_SHARED_LIBRARY)