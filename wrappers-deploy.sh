/mnt/data/ndk/android-ndk-r10d/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc 
--sysroot=/mnt/data/ndk/android-ndk-r10d/platforms/android-8/arch-arm/ -I. first.c -shared -o libs/armeabi-v7a/libfirst.so -Wl,--no-undefined 
-llog&&/mnt/data/ndk/android-ndk-r10d/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin/arm-linux-androideabi-gcc 
--sysroot=/mnt/data/ndk/android-ndk-r10d/platforms/android-8/arch-arm/ -I. tierhook.c -shared -o libs/armeabi-v7a/libtierhook.so -ltier0 -L libs/armeabi-v7a/ -landroidwrapper -O0 
-fno-omit-frame-pointer -g -llog -lSDL2 -Wl,--no-undefined&&JAVA_HOME=/opt/icedtea-bin-6.1.13.9/ sh build-java.sh&&/mnt/data2/android-sdk-linux/platform-tools/adb install -r 
bin/hl2-signed.apk

