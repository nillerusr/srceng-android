#include <jni.h>
#include <android/log.h>
#include <stdbool.h>
#include <dlfcn.h>

bool bClient_loaded = false;
void *libclient;
int (*TouchEvent)(int, int, int, int, int);

extern void clientLoaded( void )
{
	bClient_loaded = true;
	libclient = dlopen("libclient.so",RTLD_LAZY);
	*(void**)(&TouchEvent) = dlsym(libclient, "TouchEvent");
	__android_log_print( ANDROID_LOG_INFO, "HL2TOUCH", "CLIENT LOADED!" );
}

JNIEXPORT void JNICALL Java_com_valvesoftware_ValveActivity2_TouchEvent(JNIEnv *env, jobject obj, jint touchDevId, jint fingerid, jint x, jint y, jint action)
{
	if( !bClient_loaded )
		return;

	TouchEvent( touchDevId, fingerid, x, y, action );
}
