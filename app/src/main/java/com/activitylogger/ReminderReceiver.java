package com.activitylogger;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "reminder_channel";
    private static final int    NOTIF_ID   = 42;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            int mins = intervalForIndex(
                context.getSharedPreferences("prefs", 0).getInt("remind_idx", 1));
            schedule(context, mins);
            return;
        }
        showNotification(context);
        int mins = intervalForIndex(
            context.getSharedPreferences("prefs", 0).getInt("remind_idx", 1));
        schedule(context, mins);
    }

    private void showNotification(Context context) {
        createChannel(context);
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open, pendingFlags());

        String[] prompts = {
            "Log this hour - what have you been up to?",
            "Quick check-in time! Tap to journal",
            "Don't forget to log this hour",
            "Your journal is waiting - 2 minutes is all it takes",
            "Time to reflect! What did this hour look like?"
        };
        String prompt = prompts[(int)((System.currentTimeMillis() / 3600_000L) % prompts.length)];

        NotificationManager nm =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIF_ID, new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Activity Logger")
            .setContentText(prompt)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(prompt))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFF26C6DA)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build());
    }

    private void createChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL_ID, "Journal Reminders", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Hourly reminders to log your activity");
            ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(ch);
        }
    }

    public static void schedule(Context ctx, int intervalMinutes) {
        cancel(ctx);
        if (intervalMinutes <= 0) return;

        Calendar next = Calendar.getInstance();
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);

        if (intervalMinutes >= 60) {
            next.set(Calendar.MINUTE, 0);
            next.add(Calendar.HOUR_OF_DAY, 1);
        } else {
            int cur = next.get(Calendar.MINUTE);
            int toNext = intervalMinutes - (cur % intervalMinutes);
            if (toNext == 0) toNext = intervalMinutes;
            next.add(Calendar.MINUTE, toNext);
        }

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = getPendingIntent(ctx);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
        }
    }

    public static void cancel(Context ctx) {
        ((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE))
            .cancel(getPendingIntent(ctx));
    }

    private static PendingIntent getPendingIntent(Context ctx) {
        return PendingIntent.getBroadcast(ctx, 1,
            new Intent(ctx, ReminderReceiver.class), pendingFlags());
    }

    private static int pendingFlags() {
        return Build.VERSION.SDK_INT >= 23
            ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            : PendingIntent.FLAG_UPDATE_CURRENT;
    }

    public static int intervalForIndex(int idx) {
        int[] mins = {30, 60, 120, 180, 240, 0};
        return idx >= 0 && idx < mins.length ? mins[idx] : 60;
    }
}
