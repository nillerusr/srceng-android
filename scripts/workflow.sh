#!/bin/sh

export NDK_HOME=$(pwd)/ndk-binaries
export PATH=$PATH:$(pwd)/ndk-binaries

cd jni/src/tierhook/
./build-ndk.sh
cp libtierhook.so ../../../libs/armeabi-v7a
cd ../../../

#git clone --depth 1 https://github.com/nillerusr/srcsdk

