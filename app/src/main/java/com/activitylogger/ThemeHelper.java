package com.activitylogger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

public class ThemeHelper {

    public static final int MODE_LIGHT  = 0;
    public static final int MODE_DARK   = 1;
    public static final int MODE_AUTO   = 2; // dark 8pm-7am, light otherwise

    private static final String PREF_KEY = "theme_mode";

    public static int getMode(Context ctx) {
        return ctx.getSharedPreferences("prefs", 0).getInt(PREF_KEY, MODE_AUTO);
    }

    public static void setMode(Context ctx, int mode) {
        ctx.getSharedPreferences("prefs", 0).edit().putInt(PREF_KEY, mode).apply();
    }

    /** Returns true if dark should be active right now given current mode */
    public static boolean isDark(Context ctx) {
        int mode = getMode(ctx);
        if (mode == MODE_DARK)  return true;
        if (mode == MODE_LIGHT) return false;
        // AUTO: dark between 8pm and 7am
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour >= 20 || hour < 7;
    }

    /** Apply correct theme — call before setContentView */
    public static void apply(Activity activity) {
        if (isDark(activity)) {
            activity.setTheme(R.style.AppTheme_Dark);
        } else {
            activity.setTheme(R.style.AppTheme);
        }
    }

    /** After changing mode, recreate activity to apply */
    public static void applyAndRecreate(Activity activity, int newMode) {
        setMode(activity, newMode);
        activity.recreate();
    }

    // ── Color helpers for programmatic views ──────────────
    public static int bgColor(Context ctx) {
        return isDark(ctx) ? 0xFF121212 : 0xFFF0F4F8;
    }

    public static int surfaceColor(Context ctx) {
        return isDark(ctx) ? 0xFF1E1E1E : 0xFFFFFFFF;
    }

    public static int surface2Color(Context ctx) {
        return isDark(ctx) ? 0xFF2A2A2A : 0xFFF5F5F5;
    }

    public static int textPrimary(Context ctx) {
        return isDark(ctx) ? 0xFFEEEEEE : 0xFF212121;
    }

    public static int textSecondary(Context ctx) {
        return isDark(ctx) ? 0xFF9E9E9E : 0xFF757575;
    }

    public static int textHint(Context ctx) {
        return isDark(ctx) ? 0xFF616161 : 0xFFBDBDBD;
    }

    public static int dividerColor(Context ctx) {
        return isDark(ctx) ? 0xFF333333 : 0xFFE0E0E0;
    }

    public static int headerBg(Context ctx) {
        // Dark: deep navy instead of golden, keeps the sun motif
        return isDark(ctx) ? 0xFF0D1B2A : 0xFFFFF9E6;
    }

    public static int navBg(Context ctx) {
        return isDark(ctx) ? 0xFF1A1A1A : 0xFFFFFFFF;
    }

    public static int cardBg(Context ctx) {
        return isDark(ctx) ? 0xFF1E1E1E : 0xFFFFFFFF;
    }

    public static int pillUnselected(Context ctx) {
        return isDark(ctx) ? 0xFF2A2A2A : 0xFFEEEEEE;
    }

    public static int pillUnselectedText(Context ctx) {
        return isDark(ctx) ? 0xFFCCCCCC : 0xFF424242;
    }
}
