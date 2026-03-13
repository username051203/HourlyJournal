package com.activitylogger;

public class TagStat {
    private long   tagId;
    private String name;
    private String color;
    private int    entryCount;

    public TagStat(long tagId, String name, String color, int entryCount) {
        this.tagId      = tagId;
        this.name       = name;
        this.color      = color;
        this.entryCount = entryCount;
    }

    public long   getTagId()      { return tagId; }
    public String getName()       { return name; }
    public String getColor()      { return color; }
    public int    getEntryCount() { return entryCount; }
}
