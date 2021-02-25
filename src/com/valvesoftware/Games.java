package com.valvesoftware;

public class Games {
	static int iGameCount = 0;
	static final int MAX_GAMES = 128;

	public static class Game {
		public String name;
		public String mod;
		public String server_lib;
		public String client_lib;
		public String main_obb;
		public String patch_obb;
		public String extras_obb;
	}

	static Game[] games = new Game[MAX_GAMES];

	static public void addGame(String gamename, String modname, String sv, String cl, String mainobb, String patchobb, String extrasobb) {
		if( iGameCount >= MAX_GAMES )
			return;

		games[iGameCount] = new Game();

		games[iGameCount].name = gamename;
		games[iGameCount].mod = modname;
		games[iGameCount].server_lib = sv;
		games[iGameCount].client_lib = cl;
		games[iGameCount].main_obb = mainobb;
		games[iGameCount].patch_obb = patchobb;
		games[iGameCount].extras_obb = extrasobb;

		iGameCount++;
	}

	static public Game at( int at ) {
		return games[at];
	}

	static public int count() {
		return iGameCount;
	}

	static {
		addGame("Half-Life 2","hl2", "libserver_hl2.so", "libclient_hl2.so",  "main.22.com.nvidia.valvesoftware.halflife2.obb", "patch.22.com.nvidia.valvesoftware.halflife2.obb", null);
		addGame("Half-Life 2 Episode 1","episodic", "libserver_episodic.so", "libclient_episodic.so",  "main.37.com.nvidia.valvesoftware.halflife2ep1.obb", "patch.37.com.nvidia.valvesoftware.halflife2ep1.obb", null);
		addGame("Half-Life 2 Episode 2","ep2", "libserver_episodic.so", "libclient_episodic.so",  "main.32.com.nvidia.valvesoftware.halflife2ep2.obb", "patch.32.com.nvidia.valvesoftware.halflife2ep2.obb", "extras.58.com.nvidia.valvesoftware.halflife2ep2.obb");
		addGame("Portal","portal", "libserver_portal.so", "libclient_portal.so",  "main.22.com.nvidia.valvesoftware.portal.obb", "patch.22.com.nvidia.valvesoftware.portal.obb", null);
	}
}
