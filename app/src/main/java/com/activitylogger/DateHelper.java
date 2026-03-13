package com.activitylogger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateHelper {

    public static long startOfDay(long epochMs) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(epochMs);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long endOfDay(long epochMs) {
        return startOfDay(epochMs) + 86400_000L - 1;
    }

    public static String formatNavLabel(long epochMs) {
        return new SimpleDateFormat("MMM d", Locale.getDefault()).format(new Date(epochMs));
    }

    public static String formatEntryTime(long epochMs) {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date(epochMs));
    }

    public static String greeting() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (h < 12) return "Good Morning";
        if (h < 17) return "Good Afternoon";
        return "Good Evening";
    }

    public static String currentTimeLabel() {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date());
    }

    public static List<HourSlot> buildHourSlots(long dayEpochMs, List<Entry> entries) {
        List<HourSlot> slots = new ArrayList<>();
        Calendar day = Calendar.getInstance();
        day.setTimeInMillis(startOfDay(dayEpochMs));
        boolean isToday = startOfDay(dayEpochMs) == startOfDay(System.currentTimeMillis());
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        for (int h = 0; h < 24; h++) {
            // For today: show up to and including current hour
            // For past days: show all 24 hours
            day.set(Calendar.HOUR_OF_DAY, h);
            day.set(Calendar.MINUTE, 0);
            day.set(Calendar.SECOND, 0);
            day.set(Calendar.MILLISECOND, 0);
            long slotStart = day.getTimeInMillis();
            long slotEnd   = slotStart + 3600_000L - 1;
            String label = (isToday && h == currentHour) ? "Now" : sdf.format(new Date(slotStart));
            Entry matched = null;
            for (Entry e : entries)
                if (e.getTimestamp() >= slotStart && e.getTimestamp() <= slotEnd) { matched = e; break; }
            slots.add(new HourSlot(label, slotStart, matched));
        }
        return slots;
    }
}
