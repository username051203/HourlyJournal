package com.activitylogger;
public class Chapter {
    private long id; private long subjectId; private String name;
    private double hoursRequired; private int setsCount; private boolean done;
    public Chapter(long id, long subjectId, String name, double hoursRequired, int setsCount, boolean done) {
        this.id=id; this.subjectId=subjectId; this.name=name;
        this.hoursRequired=hoursRequired; this.setsCount=setsCount; this.done=done;
    }
    public long   getId()            { return id; }
    public long   getSubjectId()     { return subjectId; }
    public String getName()          { return name; }
    public double getHoursRequired() { return hoursRequired; }
    public int    getSetsCount()     { return setsCount; }
    public boolean isDone()          { return done; }
    @Override public String toString() { return name; }
}
