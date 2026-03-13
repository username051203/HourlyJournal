package com.activitylogger;
public class Entry {
    private long id; private String content; private long tagId;
    private String tagName; private String tagColor; private long timestamp;
    private String mood; private int starred;
    public Entry(long id, String content, long tagId, String tagName,
                 String tagColor, long timestamp, String mood, int starred) {
        this.id=id; this.content=content; this.tagId=tagId; this.tagName=tagName;
        this.tagColor=tagColor; this.timestamp=timestamp;
        this.mood=mood!=null?mood:""; this.starred=starred;
    }
    public long   getId()        { return id; }
    public String getContent()   { return content; }
    public long   getTagId()     { return tagId; }
    public String getTagName()   { return tagName; }
    public String getTagColor()  { return tagColor; }
    public long   getTimestamp() { return timestamp; }
    public String getMood()      { return mood; }
    public int    getStarred()   { return starred; }
    public boolean isStarred()   { return starred == 1; }
    public void setTagName(String v)  { tagName=v; }
    public void setTagColor(String v) { tagColor=v; }
}
