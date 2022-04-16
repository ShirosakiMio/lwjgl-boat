# How to compile?
ant -Dbuild.64bit.only=true -Dlwjgl.cross.jdk="/usr/lib/jvm/java-8-openjdk-amd64/include" -Dcross.compile.target="aarch64-linux-android" -Dlwjgl.platform.boat=true -Dboat.includedir=/home/mio/boat/include -Dboat.libdir=/home/mio/boat/lib -Dlwjgl.cross.cc=$CC -Dlwjgl.cross.strip=$STRIP
LWJGL - Lightweight Java Game Library
======

The Lightweight Java Game Library (LWJGL) is a solution aimed directly at professional and amateur Java programmers alike to enable commercial quality games to be written in Java. 
LWJGL provides developers access to high performance crossplatform libraries such as OpenGL (Open Graphics Library), OpenCL (Open Computing Language) and OpenAL (Open Audio Library) allowing for state of the art 3D games and 3D sound.
Additionally LWJGL provides access to controllers such as Gamepads, Steering wheel and Joysticks.
All in a simple and straight forward API.

Website: [http://lwjgl.org](http://lwjgl.org)
Forum: [http://lwjgl.org/forum](http://lwjgl.org/forum)
Bugs/Suggestions: [https://github.com/LWJGL/lwjgl/issues](https://github.com/LWJGL/lwjgl/issues)

Compilation
-----------

LWJGL requires a JDK and Ant installed to compile, as well as your platforms native compiler to compile the JNI.

* ant generate-all
* ant compile
* ant compile_native
