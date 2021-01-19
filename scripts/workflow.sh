#!/bin/bash

export NDK_HOME=$(pwd)/ndk-binaries
export PATH=$PATH:$(pwd)/ndk-binaries
export LIBPATH=$(pwd)/libs/armeabi-v7a

inst()
{
	cp $1 $LIBPATH
}

cd jni/src/tierhook/
./build-ndk.sh
inst libtierhook.so
cd ../../../

cd srcsdk/main
./build.sh -j$(nproc --all)
inst libmain.so

cd ../gl4es/
./build.sh -j$(nproc --all)
inst libRegal.so

cd ../vinterface_wrapper/client
./build-ndk.sh -j$(nproc --all)
inst libclient.so

cd ../server
./build-ndk.sh -j$(nproc --all)
inst libserver.so

cd ../../../
