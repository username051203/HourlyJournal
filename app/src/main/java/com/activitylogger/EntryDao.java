package com.activitylogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class EntryDao {
    private final DatabaseHelper dbHelper;
    private final Context context;

    private static final String FULL_SELECT =
        "SELECT e." + DatabaseHelper.COL_ENTRY_ID      + "," +
               "e." + DatabaseHelper.COL_ENTRY_CONTENT + "," +
               "e." + DatabaseHelper.COL_ENTRY_TAG_ID  + "," +
               "t." + DatabaseHelper.COL_TAG_NAME      + " AS tagName," +
               "t." + DatabaseHelper.COL_TAG_COLOR     + " AS tagColor," +
               "e." + DatabaseHelper.COL_ENTRY_TS      + "," +
               "e." + DatabaseHelper.COL_ENTRY_MOOD    + "," +
               "e." + DatabaseHelper.COL_ENTRY_STARRED +
        " FROM "  + DatabaseHelper.TABLE_ENTRIES + " e" +
        " LEFT JOIN " + DatabaseHelper.TABLE_TAGS + " t" +
        " ON e." + DatabaseHelper.COL_ENTRY_TAG_ID + "=t." + DatabaseHelper.COL_TAG_ID;

    public EntryDao(Context ctx) {
        context  = ctx.getApplicationContext();
        dbHelper = DatabaseHelper.getInstance(ctx);
    }

    public long insertPlanned(String content, long tagId, String mood, long timestamp) {
        try {
            android.content.ContentValues v = new android.content.ContentValues();
            v.put(DatabaseHelper.COL_ENTRY_CONTENT, content);
            v.put(DatabaseHelper.COL_ENTRY_TAG_ID, tagId);
            v.put(DatabaseHelper.COL_ENTRY_MOOD, mood);
            v.put(DatabaseHelper.COL_ENTRY_TS, timestamp);
            v.put(DatabaseHelper.COL_ENTRY_STARRED, 0);
            v.put(DatabaseHelper.COL_ENTRY_PLANNED, 1);
            return DatabaseHelper.getInstance(context).getWritableDatabase()
                .insert(DatabaseHelper.TABLE_ENTRIES, null, v);
        } catch (Exception e) { CrashLogger.log(context, "insertPlanned", e); return -1; }
    }

    public long insertEntryAt(String content, long tagId, String mood, long timestamp) {
        try {
            ContentValues v = new ContentValues();
            v.put(DatabaseHelper.COL_ENTRY_CONTENT, content);
            v.put(DatabaseHelper.COL_ENTRY_TAG_ID, tagId);
            v.put(DatabaseHelper.COL_ENTRY_MOOD, mood);
            v.put(DatabaseHelper.COL_ENTRY_TS, timestamp);
            v.put(DatabaseHelper.COL_ENTRY_STARRED, 0);
            return DatabaseHelper.getInstance(context).getWritableDatabase()
                .insert(DatabaseHelper.TABLE_ENTRIES, null, v);
        } catch (Exception e) { CrashLogger.log(context, "insertEntryAt", e); return -1; }
    }

    public long insertEntry(String content, long tagId, String mood) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_ENTRY_CONTENT, content.trim());
            if (tagId > 0) cv.put(DatabaseHelper.COL_ENTRY_TAG_ID, tagId);
            cv.put(DatabaseHelper.COL_ENTRY_TS,      System.currentTimeMillis());
            cv.put(DatabaseHelper.COL_ENTRY_MOOD,    mood != null ? mood : "");
            cv.put(DatabaseHelper.COL_ENTRY_STARRED, 0);
            return dbHelper.getWritableDatabase()
                .insert(DatabaseHelper.TABLE_ENTRIES, null, cv);
        } catch (Exception e) { CrashLogger.log(context, "insertEntry", e); return -1; }
    }

    public int deleteEntry(long entryId) {
        try {
            return dbHelper.getWritableDatabase().delete(
                DatabaseHelper.TABLE_ENTRIES,
                DatabaseHelper.COL_ENTRY_ID + "=?",
                new String[]{String.valueOf(entryId)});
        } catch (Exception e) { CrashLogger.log(context, "deleteEntry", e); return 0; }
    }

    public void toggleStar(long entryId, boolean starred) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_ENTRY_STARRED, starred ? 1 : 0);
            dbHelper.getWritableDatabase().update(
                DatabaseHelper.TABLE_ENTRIES, cv,
                DatabaseHelper.COL_ENTRY_ID + "=?",
                new String[]{String.valueOf(entryId)});
        } catch (Exception e) { CrashLogger.log(context, "toggleStar", e); }
    }

    public List<Entry> getAllEntries() {
        try { return runQuery(FULL_SELECT +
            " ORDER BY e." + DatabaseHelper.COL_ENTRY_TS + " DESC", null);
        } catch (Exception e) { CrashLogger.log(context, "getAllEntries", e); return new ArrayList<>(); }
    }

    public List<Entry> getEntriesForDate(long start, long end) {
        try { return runQuery(FULL_SELECT +
            " WHERE e." + DatabaseHelper.COL_ENTRY_TS + " BETWEEN ? AND ?" +
            " ORDER BY e." + DatabaseHelper.COL_ENTRY_TS + " ASC",
            new String[]{String.valueOf(start), String.valueOf(end)});
        } catch (Exception e) { CrashLogger.log(context, "getEntriesForDate", e); return new ArrayList<>(); }
    }

    public List<Entry> getEntriesForTag(long tagId, long start, long end) {
        try { return runQuery(FULL_SELECT +
            " WHERE e." + DatabaseHelper.COL_ENTRY_TAG_ID + "=?" +
            " AND e." + DatabaseHelper.COL_ENTRY_TS + " BETWEEN ? AND ?" +
            " ORDER BY e." + DatabaseHelper.COL_ENTRY_TS + " DESC",
            new String[]{String.valueOf(tagId), String.valueOf(start), String.valueOf(end)});
        } catch (Exception e) { CrashLogger.log(context, "getEntriesForTag", e); return new ArrayList<>(); }
    }

    public List<Entry> getStarredEntries() {
        try { return runQuery(FULL_SELECT +
            " WHERE e." + DatabaseHelper.COL_ENTRY_STARRED + "=1" +
            " ORDER BY e." + DatabaseHelper.COL_ENTRY_TS + " DESC", null);
        } catch (Exception e) { CrashLogger.log(context, "getStarredEntries", e); return new ArrayList<>(); }
    }

    public List<Entry> searchByContent(String term) {
        try { return runQuery(FULL_SELECT +
            " WHERE e." + DatabaseHelper.COL_ENTRY_CONTENT + " LIKE ?" +
            " ORDER BY e." + DatabaseHelper.COL_ENTRY_TS + " DESC",
            new String[]{"%" + term + "%"});
        } catch (Exception e) { CrashLogger.log(context, "searchByContent", e); return new ArrayList<>(); }
    }

    public List<Tag> getAllTags() {
        try {
            List<Tag> tags = new ArrayList<>();
            Cursor c = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_TAGS, null, null, null, null, null,
                DatabaseHelper.COL_TAG_NAME + " ASC");
            if (c != null) {
                while (c.moveToNext()) tags.add(new Tag(
                    c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAG_ID)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAG_NAME)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_TAG_COLOR))));
                c.close();
            }
            return tags;
        } catch (Exception e) { CrashLogger.log(context, "getAllTags", e); return new ArrayList<>(); }
    }

    public long insertTag(String name, String color) {
        try {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_TAG_NAME,  name.trim());
            cv.put(DatabaseHelper.COL_TAG_COLOR, color);
            return dbHelper.getWritableDatabase().insertWithOnConflict(
                DatabaseHelper.TABLE_TAGS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        } catch (Exception e) { CrashLogger.log(context, "insertTag", e); return -1; }
    }

    /** Total entries in range — for analytics summary card */
    public int getTotalEntries(long start, long end) {
        try {
            Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ENTRIES +
                " WHERE " + DatabaseHelper.COL_ENTRY_TS + " BETWEEN ? AND ?",
                new String[]{String.valueOf(start), String.valueOf(end)});
            int count = 0;
            if (c != null) { if (c.moveToFirst()) count = c.getInt(0); c.close(); }
            return count;
        } catch (Exception e) { CrashLogger.log(context, "getTotalEntries", e); return 0; }
    }

    /** Count distinct days that have at least one entry */
    public int getActiveDays(long start, long end) {
        try {
            // Group by day (divide timestamp by ms-per-day)
            Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(DISTINCT (" + DatabaseHelper.COL_ENTRY_TS + " / 86400000))" +
                " FROM " + DatabaseHelper.TABLE_ENTRIES +
                " WHERE " + DatabaseHelper.COL_ENTRY_TS + " BETWEEN ? AND ?",
                new String[]{String.valueOf(start), String.valueOf(end)});
            int count = 0;
            if (c != null) { if (c.moveToFirst()) count = c.getInt(0); c.close(); }
            return count;
        } catch (Exception e) { CrashLogger.log(context, "getActiveDays", e); return 0; }
    }

    /** Most used mood in range */
    public String getTopMood(long start, long end) {
        try {
            Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT " + DatabaseHelper.COL_ENTRY_MOOD + ", COUNT(*) AS cnt" +
                " FROM " + DatabaseHelper.TABLE_ENTRIES +
                " WHERE " + DatabaseHelper.COL_ENTRY_TS + " BETWEEN ? AND ?" +
                " AND " + DatabaseHelper.COL_ENTRY_MOOD + " != ''" +
                " GROUP BY " + DatabaseHelper.COL_ENTRY_MOOD +
                " ORDER BY cnt DESC LIMIT 1",
                new String[]{String.valueOf(start), String.valueOf(end)});
            String mood = null;
            if (c != null) { if (c.moveToFirst()) mood = c.getString(0); c.close(); }
            return mood;
        } catch (Exception e) { CrashLogger.log(context, "getTopMood", e); return null; }
    }

    /**
     * Returns tags WITH entries only, sorted by entry count desc.
     * Includes tagId for drill-down navigation.
     */
    public List<TagStat> getTagStats(long start, long end) {
        try {
            List<TagStat> stats = new ArrayList<>();
            // INNER JOIN so tags with 0 entries are excluded
            String sql =
                "SELECT t." + DatabaseHelper.COL_TAG_ID    + " AS tagId," +
                       "t." + DatabaseHelper.COL_TAG_NAME  + " AS tagName," +
                       "t." + DatabaseHelper.COL_TAG_COLOR + " AS tagColor," +
                       "COUNT(e." + DatabaseHelper.COL_ENTRY_ID + ") AS entryCount" +
                " FROM " + DatabaseHelper.TABLE_TAGS + " t" +
                " INNER JOIN " + DatabaseHelper.TABLE_ENTRIES + " e" +
                " ON e." + DatabaseHelper.COL_ENTRY_TAG_ID + "=t." + DatabaseHelper.COL_TAG_ID +
                " AND e." + DatabaseHelper.COL_ENTRY_TS + " BETWEEN ? AND ?" +
                " GROUP BY t." + DatabaseHelper.COL_TAG_ID +
                " ORDER BY entryCount DESC";
            Cursor c = dbHelper.getReadableDatabase().rawQuery(
                sql, new String[]{String.valueOf(start), String.valueOf(end)});
            if (c != null) {
                while (c.moveToNext()) stats.add(new TagStat(
                    c.getLong(c.getColumnIndexOrThrow("tagId")),
                    c.getString(c.getColumnIndexOrThrow("tagName")),
                    c.getString(c.getColumnIndexOrThrow("tagColor")),
                    c.getInt(c.getColumnIndexOrThrow("entryCount"))));
                c.close();
            }
            return stats;
        } catch (Exception e) { CrashLogger.log(context, "getTagStats", e); return new ArrayList<>(); }
    }

    public int getCurrentStreak() {
        try {
            long dayMs = DateHelper.startOfDay(System.currentTimeMillis());
            int streak = 0;
            while (true) {
                Cursor c = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ENTRIES +
                    " WHERE " + DatabaseHelper.COL_ENTRY_TS + " BETWEEN ? AND ?",
                    new String[]{String.valueOf(dayMs), String.valueOf(dayMs + 86400_000L - 1)});
                boolean has = false;
                if (c != null) { if (c.moveToFirst()) has = c.getInt(0) > 0; c.close(); }
                if (!has) break;
                streak++; dayMs -= 86400_000L;
            }
            return streak;
        } catch (Exception e) { CrashLogger.log(context, "getCurrentStreak", e); return 0; }
    }

    private List<Entry> runQuery(String sql, String[] args) {
        List<Entry> results = new ArrayList<>();
        try {
            Cursor c = dbHelper.getReadableDatabase().rawQuery(sql, args);
            if (c != null) {
                while (c.moveToNext()) {
                    try {
                        results.add(new Entry(
                            c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_ID)),
                            c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_CONTENT)),
                            c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_TAG_ID)),
                            c.getString(c.getColumnIndexOrThrow("tagName")),
                            c.getString(c.getColumnIndexOrThrow("tagColor")),
                            c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_TS)),
                            c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_MOOD)),
                            c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_ENTRY_STARRED))));
                    } catch (Exception row) { CrashLogger.log(context, "runQuery row", row); }
                }
                c.close();
            }
        } catch (Exception e) { CrashLogger.log(context, "runQuery", e); }
        return results;
    }
}
