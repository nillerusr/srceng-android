package org.libsdl.app;

import org.libsdl.app.SDLActivity;

class SDLMain implements Runnable {
	@Override
	public void run() {
		SDLActivity.nativeInit();
		SDLActivity.quit();
	}
}