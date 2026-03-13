package com.activitylogger;

/**
 * HourSlot.java
 * Represents one row in the hourly timeline list.
 * Each slot holds the hour label (e.g. "2:00 AM") and
 * the Entry logged for that hour, if any.
 */
public class HourSlot {
    private String label;      // e.g. "2:00 AM" or "Now"
    private long   slotStart;  // epoch ms for start of this hour
    private Entry  entry;      // null if no entry logged yet

    public HourSlot(String label, long slotStart, Entry entry) {
        this.label     = label;
        this.slotStart = slotStart;
        this.entry     = entry;
    }

    public String getLabel()           { return label; }
    public long   getSlotStart()       { return slotStart; }
    public Entry  getEntry()           { return entry; }
    public void   setEntry(Entry e)    { entry = e; }
    public boolean hasEntry()          { return entry != null; }
}
