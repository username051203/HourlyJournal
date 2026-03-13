package com.activitylogger;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashLogger implements Thread.UncaughtExceptionHandler {

    private static final String TAG     = "ActivityLogger";
    private static final String FILE    = "crash_log.txt";
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final Context context;

    public static void init(Context ctx) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashLogger(ctx));
    }

    private CrashLogger(Context ctx) {
        this.context        = ctx.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            File f = new File(context.getFilesDir(), FILE);
            FileWriter fw = new FileWriter(f, true); // append
            PrintWriter pw = new PrintWriter(fw);
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
            pw.println("══════════════════════════════");
            pw.println("CRASH at " + ts);
            pw.println("Thread: " + thread.getName());
            ex.printStackTrace(pw);
            pw.println();
            pw.flush(); pw.close();
        } catch (Exception ignored) {}
        Log.e(TAG, "Uncaught exception", ex);
        if (defaultHandler != null) defaultHandler.uncaughtException(thread, ex);
    }

    public static String readLog(Context ctx) {
        try {
            File f = new File(ctx.getFilesDir(), FILE);
            if (!f.exists()) return "No crashes logged yet.";
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(f));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            br.close();
            // Return last 6000 chars so dialog isn't huge
            String s = sb.toString();
            return s.length() > 6000 ? "...\n" + s.substring(s.length() - 6000) : s;
        } catch (Exception e) { return "Could not read log: " + e.getMessage(); }
    }

    public static void clearLog(Context ctx) {
        new File(ctx.getFilesDir(), FILE).delete();
    }

    // Also log non-fatal errors manually
    public static void log(Context ctx, String tag, Exception e) {
        Log.e(TAG, tag, e);
        try {
            File f = new File(ctx.getFilesDir(), FILE);
            FileWriter fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
            pw.println("── ERROR at " + ts + " [" + tag + "]");
            e.printStackTrace(pw);
            pw.println();
            pw.flush(); pw.close();
        } catch (Exception ignored) {}
    }
}
