package com.ansub.tvhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.provider.Settings;

// MediaTek TVs put their Live TV app on top during a cold boot; bring Home back as early as the boot allows.
// Starting from the background needs the "display over other apps" permission, granted over adb.
public class BootReceiver extends BroadcastReceiver {
    private static final long[] TRIES_MS = {0, 2000, 5000};

    @Override
    public void onReceive(Context c, Intent intent) {
        String a = intent.getAction();
        if (!Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(a) && !Intent.ACTION_BOOT_COMPLETED.equals(a)) return;
        UserManager um = (UserManager) c.getSystemService(Context.USER_SERVICE);
        if (um != null && !um.isUserUnlocked()) return;
        android.content.pm.ResolveInfo home = c.getPackageManager().resolveActivity(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
        if (home == null || home.activityInfo == null || !c.getPackageName().equals(home.activityInfo.packageName)) return;
        Context dp = c.createDeviceProtectedStorageContext();
        SharedPreferences prefs = dp.getSharedPreferences("boot", 0);
        int boot = Settings.Global.getInt(dp.getContentResolver(), Settings.Global.BOOT_COUNT, -1);
        // Once per boot: the later broadcast can land after someone has already opened an app.
        if (boot >= 0 && prefs.getInt("handled", -2) == boot) return;
        prefs.edit().putInt("handled", boot).apply();

        final Context app = c.getApplicationContext();
        final PendingResult done = goAsync();
        Handler h = new Handler(Looper.getMainLooper());
        for (int i = 0; i < TRIES_MS.length; i++) {
            final boolean last = i == TRIES_MS.length - 1;
            h.postDelayed(() -> {
                home(app);
                if (last) done.finish();
            }, TRIES_MS[i]);
        }
    }

    private static void home(Context c) {
        Intent i = new Intent(c, MainActivity.class).setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { c.startActivity(i); } catch (Exception ignored) {}
    }
}
