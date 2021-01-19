#!/bin/sh

export NDK_HOME=$(pwd)/ndk-binaries
export PATH=$PATH:$(pwd)/ndk-binaries

cd jni/src/tierhook/
./build-ndk.sh
cp libtierhook.so ../../../libs/armeabi-v7a
cd ../../../

cd srcsdk/main
./build.sh -j$(nproc --all)
cp libmain.so ../../libs/armeabi-v7a
cd ../vinterface_wrapper/client
./build-ndk.sh -j$(nproc --all)
cp libclient.so ../../../libs/armeabi-v7a
cd ../server
./build-ndk.sh -j$(nproc --all)
cp libserver.so ../../../libs/armeabi-v7a
cd ../../../
