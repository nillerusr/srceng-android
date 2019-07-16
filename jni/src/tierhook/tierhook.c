#include <jni.h>
#include <android/log.h>
#define HIJACK_SIZE 12
#include <sys/mman.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <stdint.h>
#include <SDL2/SDL.h>
#include <SDL2/SDL_events.h>
#include "SDL_mouse_c.h"
extern SDL_Window *Android_Window;

void simple_hook( void *target, void *func )
{
    unsigned char o_code[HIJACK_SIZE], n_code[HIJACK_SIZE];
	size_t pagesize = sysconf(_SC_PAGESIZE);

    if ( (unsigned long)target % 4 == 0 )
    {
        // ldr pc, [pc, #0]; .long addr; .long addr
        memcpy(n_code, "\x00\xf0\x9f\xe5\x00\x00\x00\x00\x00\x00\x00\x00", HIJACK_SIZE);
        *(unsigned long *)&n_code[4] = (unsigned long)func;
        *(unsigned long *)&n_code[8] = (unsigned long)func;
    }
    else // Thumb
    {
        // add r0, pc, #4; ldr r0, [r0, #0]; mov pc, r0; mov pc, r0; .long addr
        memcpy(n_code, "\x01\xa0\x00\x68\x87\x46\x87\x46\x00\x00\x00\x00", HIJACK_SIZE);
        *(unsigned long *)&n_code[8] = (unsigned long)func;
        target--;
    }

	mprotect((char *)(((int) target - (pagesize-1)) & ~(pagesize-1)), pagesize * 2, PROT_READ | PROT_WRITE );

        memcpy(o_code, target, HIJACK_SIZE);

        memcpy(target, n_code, HIJACK_SIZE);
	mprotect((char *)(((int) target - (pagesize-1)) & ~(pagesize-1)), pagesize * 2, PROT_READ | PROT_EXEC );
	cacheflush((long)target, (long)target + HIJACK_SIZE, 0);
}

// _SpewMessage hook hook
int DefaultSpewFunc( int type, char *msg );
void SpewOutputFunc(int (*func)( int, char*));
void *GetSpewOutputFunc();

JNIEXPORT void JNICALL Java_org_libsdl_app_SDLSurface_SetMousePos(JNIEnv *env, jclass cls, jint x, jint y)
{
	FILE *f;
	f=fopen("/sdcard/touch.txt","a+");
	SDL_Mouse *mouse = SDL_GetMouse();
	/*__android_log_print( ANDROID_LOG_INFO,"FAGGOT", "x:%d y:%d", x, y );
	fprintf(f,"setx:%d sety:%d\n",x,y);
	fprintf(f,"mouse->x=%d, mouse->y=%d\n",mouse->x, mouse->y);
	fprintf(f,"mouse->last_x=%d, mouse->last_y=%d\n",mouse->last_x, mouse->last_y);
	fprintf(f,"mouse->xdelta=%d, mouse->ydelta=%d\n",mouse->xdelta, mouse->ydelta);*/
	mouse->x = x;
        mouse->y = y;
//	mouse->xdelta = 0;
//	mouse->ydelta = 0;
	fclose(f);
//	SDL_SendMouseMotion(0, SDL_TOUCH_MOUSEID, 0, 540, 360);
}

JNIEXPORT void JNICALL Java_org_libsdl_app_SDLSurface_MoveMouse(JNIEnv *env, jclass cls, jint x, jint y)
{

    SDL_Mouse *mouse = SDL_GetMouse();
/*    int xrel;
    int yrel;

    xrel = x - mouse->last_x;
    yrel = y - mouse->last_y;

    if (!xrel && !yrel)
        return;
   __android_log_print( ANDROID_LOG_INFO,"FAGGOT", "OH YEAH" );
    mouse->x += xrel;
    mouse->y += yrel;

    mouse->xdelta += xrel;
    mouse->ydelta += yrel;

    mouse->last_x = x;
    mouse->last_y = y;
*/
FILE *f;
f=fopen("/sdcard/touch.txt","a+");
fprintf(f,"setx:%d sety:%d\n",x,y);
fprintf(f,"BEFORE:\n");
fprintf(f,"mouse->x=%d, mouse->y=%d\n",mouse->x, mouse->y);
        fprintf(f,"mouse->last_x=%d, mouse->last_y=%d\n",mouse->last_x, mouse->last_y);
        fprintf(f,"mouse->xdelta=%d, mouse->ydelta=%d\n",mouse->xdelta, mouse->ydelta);
SDL_SendMouseMotion(0, SDL_TOUCH_MOUSEID, 0, x, y);
	fprintf(f,"AFTER:\n");
        fprintf(f,"mouse->x=%d, mouse->y=%d\n",mouse->x, mouse->y);
        fprintf(f,"mouse->last_x=%d, mouse->last_y=%d\n",mouse->last_x, mouse->last_y);
        fprintf(f,"mouse->xdelta=%d, mouse->ydelta=%d\n",mouse->xdelta, mouse->ydelta);
fclose(f);
}
int _NewSpewMessage( int type, const char *fmt, va_list ap )
{
	char log[1000];
	int (*func)( int, char*);

	vsnprintf(log, 999, fmt, ap);
	log[999] = 0;

	__android_log_print( ANDROID_LOG_INFO,"SRCENGINE", "%s", log );

	func = GetSpewOutputFunc();

	if( func && func != DefaultSpewFunc )
		func( 0, log );

	return 1;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* x)
{
	// offset seems to be correct in 23-78
	simple_hook( DefaultSpewFunc + 0x50, _NewSpewMessage );
	// force enable text input (no need in SDL patching now)

	return JNI_VERSION_1_4;
}
