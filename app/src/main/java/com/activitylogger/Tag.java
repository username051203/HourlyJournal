package com.activitylogger;

public class Tag {
    private long   id;
    private String name;
    private String color;

    public Tag(long id, String name, String color) {
        this.id    = id;
        this.name  = name;
        this.color = color;
    }

    public long   getId()    { return id; }
    public String getName()  { return name; }
    public String getColor() { return color; }

    @Override
    public String toString() { return name; }
}
