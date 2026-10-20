package com.ansub.tvhome;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import java.util.Calendar;
import java.util.Random;

final class Wallpapers {
    static final String[] NAMES = {"Automatic", "Aurora", "Nebula", "Ocean", "Forest", "Ember", "Rose", "Graphite",
            "Sunrise", "Daylight", "Sunset", "Midnight"};
    private static final int SUNRISE = 8, DAYLIGHT = 9, SUNSET = 10, MIDNIGHT = 11;

    // Base top and bottom colour, then glows of {colour with strength as alpha, x, y, radius} in thousandths of the screen.
    private static final int[][] PALETTES = {
        null,
        {0xFF0A1626, 0xFF04070D, 0xCC138A78, 180, 120, 560, 0xB02E9E5A, 560, 40, 420, 0xB85A3FC4, 880, 300, 520,
                0x80183A7A, 420, 950, 620},
        {0xFF120A1E, 0xFF050308, 0xC08A2BA8, 150, 200, 520, 0xB03B2C9C, 700, 120, 560, 0xA01C5BB0, 920, 700, 520,
                0x70B03070, 350, 950, 500},
        {0xFF061624, 0xFF02070C, 0xC0137FA8, 220, 150, 560, 0xA0145C96, 780, 250, 520, 0x9012907E, 550, 950, 600},
        {0xFF08140E, 0xFF030805, 0xB02A7A46, 200, 150, 520, 0x904E7A2A, 750, 120, 460, 0x8018706A, 850, 850, 560},
        {0xFF1A0C08, 0xFF070303, 0xC0C8501E, 180, 180, 520, 0xA0A0202E, 780, 300, 520, 0x80C08A20, 500, 1000, 560},
        {0xFF1A0B14, 0xFF070306, 0xB8C04A78, 200, 150, 520, 0xA0D07A5A, 800, 180, 460, 0x906A3AB0, 600, 950, 560},
        {0xFF141618, 0xFF060708, 0x604A5A70, 200, 150, 560, 0x50706258, 850, 600, 520},
        {0xFF2A1630, 0xFF0C0710, 0xC0E88A5C, 150, 250, 560, 0xA0E05A7A, 600, 80, 480, 0x807A6AD8, 900, 400, 520},
        {0xFF0E3A60, 0xFF061426, 0xB02A9FD8, 200, 120, 580, 0x9030C0B0, 800, 200, 500, 0x706A80E0, 600, 950, 600},
        {0xFF24102A, 0xFF0A050C, 0xC8E06A2A, 180, 300, 560, 0xB0D03A6A, 550, 120, 500, 0xA05A2E9C, 900, 500, 560},
        {0xFF070A18, 0xFF020309, 0x902C3A9A, 180, 150, 520, 0x702A1F6A, 850, 350, 480, 0x50163070, 500, 1000, 600},
    };

    private static final float[] STOPS = {0f, 0.2f, 0.45f, 0.7f, 1f};
    private static final float[] FALLOFF = {1f, 0.82f, 0.45f, 0.14f, 0f};

    private Wallpapers() {}

    // Automatic picks by the hour: sunrise 5-8, daylight 8-17, sunset 17-20, midnight otherwise.
    static int resolve(int index) {
        if (index > 0 && index < NAMES.length) return index;
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return h >= 5 && h < 8 ? SUNRISE : h >= 8 && h < 17 ? DAYLIGHT : h >= 17 && h < 20 ? SUNSET : MIDNIGHT;
    }

    static Bitmap render(int index, int w, int h, boolean shade) {
        int[] p = PALETTES[resolve(index)];
        w = Math.max(1, w);
        h = Math.max(1, h);
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setShader(new LinearGradient(0, 0, w * 0.35f, h, p[0], p[1], Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        for (int i = 2; i + 3 < p.length; i += 4) {
            float x = p[i + 1] * w / 1000f, y = p[i + 2] * h / 1000f, r = Math.max(1f, p[i + 3] * w / 1000f);
            paint.setShader(new RadialGradient(x, y, r, falloff(p[i]), STOPS, Shader.TileMode.CLAMP));
            c.drawCircle(x, y, r, paint);
        }
        paint.setXfermode(null);
        if (shade) {
            paint.setShader(new RadialGradient(w * 0.45f, h * 0.4f, Math.max(w, h) * 0.8f,
                    new int[] {0, 0, 0x8C000000}, new float[] {0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, w, h, paint);
            paint.setShader(new LinearGradient(0, h * 0.5f, 0, h, 0, 0x73000000, Shader.TileMode.CLAMP));
            c.drawRect(0, h * 0.5f, w, h, paint);
        }
        return b;
    }

    static Bitmap dot(int index, int size) {
        int w = size * 16 / 9;
        Bitmap src = render(index, w, size, false);
        Bitmap out = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        BitmapShader s = new BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Matrix m = new Matrix();
        m.setTranslate(-(w - size) / 2f, 0);
        s.setLocalMatrix(m);
        p.setShader(s);
        new Canvas(out).drawCircle(size / 2f, size / 2f, size / 2f, p);
        return out;
    }

    static Bitmap grain() {
        int n = 128;
        int[] px = new int[n * n];
        Random r = new Random(7);
        for (int i = 0; i < px.length; i++) px[i] = r.nextInt(12) << 24 | (r.nextBoolean() ? 0xFFFFFF : 0);
        return Bitmap.createBitmap(px, n, n, Bitmap.Config.ARGB_8888);
    }

    private static int[] falloff(int color) {
        int a = color >>> 24, rgb = color & 0xFFFFFF;
        int[] out = new int[FALLOFF.length];
        for (int i = 0; i < out.length; i++) out[i] = Math.round(a * FALLOFF[i]) << 24 | rgb;
        return out;
    }
}
