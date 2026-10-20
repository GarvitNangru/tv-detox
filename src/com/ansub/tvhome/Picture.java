package com.ansub.tvhome;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.provider.Settings;

// MediaTek TVs mirror picture settings into Settings.Global, and their Settings app applies any change it sees there.
final class Picture {
    static final String MODE = "picture_mode";
    static final String[] LEVELS = {"picture_backlight", "picture_contrast", "picture_brightness"};
    static final String[] LEVEL_NAMES = {"Backlight", "Contrast", "Brightness"};
    static final int[] LEVEL_STEPS = {5, 2, 2};
    static final int[] DARK_DEFAULTS = {20, 40, 46};
    // The TV's only adjustable mode: changing a level in any other mode turns it into this one.
    static final int USER = 0;
    private static final String TV_SETTINGS = "com.android.tv.settings";

    final String[] names;
    final int[] values;

    private Picture(String[] names, int[] values) {
        this.names = names;
        this.values = values;
    }

    static Picture load(Context c) {
        if (Settings.Global.getString(c.getContentResolver(), MODE) == null) return null;
        try {
            Resources r = c.getPackageManager().getResourcesForApplication(TV_SETTINGS);
            int n = r.getIdentifier("picture_mode_ref_entries", "array", TV_SETTINGS);
            int v = r.getIdentifier("picture_mode_ref_entry_values", "array", TV_SETTINGS);
            if (n == 0 || v == 0) return null;
            String[] names = r.getStringArray(n), raw = r.getStringArray(v);
            if (names.length != raw.length || names.length == 0) return null;
            int[] values = new int[raw.length];
            for (int i = 0; i < raw.length; i++) values[i] = Integer.parseInt(raw[i].trim());
            return new Picture(names, values);
        } catch (Exception e) {
            return null;
        }
    }

    boolean hasUser() {
        for (int v : values) if (v == USER) return true;
        return false;
    }

    static int[] current(Context c) {
        int[] out = new int[LEVELS.length];
        for (int i = 0; i < out.length; i++) out[i] = get(c, LEVELS[i], 50);
        return out;
    }

    String nameOf(int mode) {
        for (int i = 0; i < values.length; i++) if (values[i] == mode) return names[i];
        return null;
    }

    static boolean canWrite(Context c) {
        return c.checkPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS, android.os.Process.myPid(),
                android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED;
    }

    static int get(Context c, String key, int fallback) {
        return Settings.Global.getInt(c.getContentResolver(), key, fallback);
    }

    static boolean put(Context c, String key, int value) {
        try {
            return Settings.Global.putInt(c.getContentResolver(), key, value);
        } catch (SecurityException e) {
            return false;
        }
    }
}
