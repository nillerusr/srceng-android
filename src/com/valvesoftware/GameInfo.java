package com.valvesoftware;

public class GameInfo {
  public static final int GAME_HL2 = 0;
  public static final int GAME_HL2EP1 = 1;
  public static final int GAME_HL2EP2 = 2;
  public static final int GAME_PORTAL = 3;

  public static class hl2 {
    public static final String mod = "hl2";
    public static final String server = "libserver_hl2.so";
    public static final String client = "libclient_hl2.so";
    public static final String main_obb = "main.22.com.nvidia.valvesoftware.halflife2.obb";
    public static final String patch_obb = "patch.22.com.nvidia.valvesoftware.halflife2.obb";
    public static final String extras_obb = "";
  }
  public static class hl2ep1 {
    public static final String mod = "episodic";
    public static final String server = "libserver_episodic.so";
    public static final String client = "libclient_episodic.so";
    public static final String main_obb = "main.37.com.nvidia.valvesoftware.halflife2ep1.obb";
    public static final String patch_obb = "patch.37.com.nvidia.valvesoftware.halflife2ep1.obb";
    public static final String extras_obb = "";
  }
  public static class hl2ep2 {
    public static final String mod = "ep2";
    public static final String server = "libserver_episodic.so";
    public static final String client = "libclient_episodic.so";
    public static final String main_obb = "main.32.com.nvidia.valvesoftware.halflife2ep2.obb";
    public static final String patch_obb = "patch.32.com.nvidia.valvesoftware.halflife2ep2.obb";
    public static final String extras_obb = "extras.58.com.nvidia.valvesoftware.halflife2ep2.obb";
  }
  public static class portal {
    public static final String mod = "portal";
    public static final String server = "libserver_portal.so";
    public static final String client = "libclient_portal.so";
    public static final String main_obb = "main.22.com.nvidia.valvesoftware.portal.obb";
    public static final String patch_obb = "patch.22.com.nvidia.valvesoftware.portal.obb";
    public static final String extras_obb = "";
  }
}
