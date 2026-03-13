package com.activitylogger;
public class Snapshot {
    private long id; private long timestamp; private byte[] jpeg; private String usageSummary;
    public Snapshot(long id, long timestamp, byte[] jpeg, String usageSummary) {
        this.id=id; this.timestamp=timestamp; this.jpeg=jpeg;
        this.usageSummary=usageSummary!=null?usageSummary:"";
    }
    public long   getId()           { return id; }
    public long   getTimestamp()    { return timestamp; }
    public byte[] getJpeg()         { return jpeg; }
    public String getUsageSummary() { return usageSummary; }
}
