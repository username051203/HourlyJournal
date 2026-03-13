package com.activitylogger;
public class Subject {
    private long id; private String name; private String color; private double hoursRequired;
    public Subject(long id, String name, String color, double hoursRequired) {
        this.id=id; this.name=name; this.color=color; this.hoursRequired=hoursRequired;
    }
    public long   getId()           { return id; }
    public String getName()         { return name; }
    public String getColor()        { return color; }
    public double getHoursRequired(){ return hoursRequired; }
    @Override public String toString() { return name; }
}
