package com.activitylogger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "activity_logger.db";
    private static final int    DB_VERSION = 6;

    public static final String TABLE_TAGS    = "tags";
    public static final String COL_TAG_ID    = "_id";
    public static final String COL_TAG_NAME  = "name";
    public static final String COL_TAG_COLOR = "color";

    public static final String TABLE_ENTRIES     = "entries";
    public static final String COL_ENTRY_ID      = "_id";
    public static final String COL_ENTRY_CONTENT = "content";
    public static final String COL_ENTRY_TAG_ID  = "tag_id";
    public static final String COL_ENTRY_TS      = "timestamp";
    public static final String COL_ENTRY_MOOD    = "mood";
    public static final String COL_ENTRY_STARRED  = "starred";
    public static final String COL_ENTRY_PLANNED  = "is_planned";

    public static final String TABLE_SUBJECTS     = "subjects";
    public static final String COL_SUBJ_ID        = "_id";
    public static final String COL_SUBJ_NAME      = "name";
    public static final String COL_SUBJ_COLOR     = "color";
    public static final String COL_SUBJ_HOURS_REQ = "hours_required";

    public static final String TABLE_CHAPTERS   = "chapters";
    public static final String COL_CHAP_ID      = "_id";
    public static final String COL_CHAP_SUBJ_ID = "subject_id";
    public static final String COL_CHAP_NAME    = "name";
    public static final String COL_CHAP_HOURS   = "hours_required";
    public static final String COL_CHAP_SETS    = "sets_count";
    public static final String COL_CHAP_DONE    = "is_done";

    // Stores user's personal study schedule preferences
    public static final String TABLE_STUDY_PREFS      = "study_prefs";
    public static final String COL_PREF_ID            = "_id";
    public static final String COL_PREF_DAILY_MINUTES = "daily_minutes";
    public static final String COL_PREF_START_HOUR    = "start_hour";
    public static final String COL_PREF_DAYS_OFF      = "days_off";
    public static final String COL_PREF_EXAM_DATE     = "exam_date";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null)
            instance = new DatabaseHelper(ctx.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context c) { super(c, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        createAllTables(db);
        insertDefaultTags(db);
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            try { db.execSQL("ALTER TABLE " + TABLE_ENTRIES + " ADD COLUMN " + COL_ENTRY_PLANNED + " INTEGER DEFAULT 0"); }
            catch (Exception ignored) {}
        }
        try { db.execSQL("CREATE TABLE IF NOT EXISTS entries_backup AS SELECT * FROM entries"); }
        catch (Exception ignored) {}

        db.execSQL("DROP TRIGGER IF EXISTS entries_ai");
        db.execSQL("DROP TRIGGER IF EXISTS entries_ad");
        db.execSQL("DROP TRIGGER IF EXISTS entries_au");
        db.execSQL("DROP TABLE IF EXISTS entries_fts");
        db.execSQL("DROP TABLE IF EXISTS entries");
        db.execSQL("DROP TABLE IF EXISTS tags");
        db.execSQL("DROP TABLE IF EXISTS subjects");
        db.execSQL("DROP TABLE IF EXISTS chapters");
        db.execSQL("DROP TABLE IF EXISTS study_prefs");

        createAllTables(db);
        insertDefaultTags(db);

        try {
            db.execSQL(
                "INSERT INTO entries(content,timestamp,mood,starred) " +
                "SELECT content,timestamp,COALESCE(mood,''),COALESCE(starred,0) " +
                "FROM entries_backup");
        } catch (Exception ignored) {}
        try { db.execSQL("DROP TABLE IF EXISTS entries_backup"); } catch (Exception ignored) {}
    }

    private void createAllTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TAGS + "(" +
            COL_TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_TAG_NAME + " TEXT NOT NULL UNIQUE," +
            COL_TAG_COLOR + " TEXT NOT NULL DEFAULT '#26C6DA');");

        db.execSQL("CREATE TABLE " + TABLE_ENTRIES + "(" +
            COL_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_ENTRY_CONTENT + " TEXT NOT NULL," +
            COL_ENTRY_TAG_ID + " INTEGER," +
            COL_ENTRY_TS + " INTEGER NOT NULL," +
            COL_ENTRY_MOOD + " TEXT DEFAULT ''," +
            COL_ENTRY_STARRED + " INTEGER DEFAULT 0," +
            COL_ENTRY_PLANNED + " INTEGER DEFAULT 0);");

        db.execSQL("CREATE TABLE " + TABLE_SUBJECTS + "(" +
            COL_SUBJ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_SUBJ_NAME + " TEXT NOT NULL," +
            COL_SUBJ_COLOR + " TEXT NOT NULL DEFAULT '#26C6DA'," +
            COL_SUBJ_HOURS_REQ + " REAL DEFAULT 0);");

        db.execSQL("CREATE TABLE " + TABLE_CHAPTERS + "(" +
            COL_CHAP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_CHAP_SUBJ_ID + " INTEGER NOT NULL," +
            COL_CHAP_NAME + " TEXT NOT NULL," +
            COL_CHAP_HOURS + " REAL DEFAULT 1," +
            COL_CHAP_SETS + " INTEGER DEFAULT 1," +
            COL_CHAP_DONE + " INTEGER DEFAULT 0);");

        db.execSQL("CREATE TABLE " + TABLE_STUDY_PREFS + "(" +
            COL_PREF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COL_PREF_DAILY_MINUTES + " INTEGER DEFAULT 120," +
            COL_PREF_START_HOUR + " INTEGER DEFAULT 9," +
            COL_PREF_DAYS_OFF + " TEXT DEFAULT ''," +
            COL_PREF_EXAM_DATE + " INTEGER DEFAULT 0);");
    }

    private void insertDefaultTags(SQLiteDatabase db) {
        String[][] tags = {
            {"No Tag","#607D8B"},{"Work","#4CAF50"},{"Travel","#26C6DA"},
            {"Passion","#FFC107"},{"Play / Fun","#3F51B5"},{"Gaming","#9C27B0"},
            {"Shopping","#FF9800"},{"Productive","#8BC34A"},{"Cleaning","#00BCD4"},
            {"Sleep","#311B92"},{"Morning Routine","#FDD835"},{"Evening Routine","#AD1457"},
            {"Nap","#B0BEC5"},{"Social Media","#C62828"},{"Love","#E91E63"},
            {"Meeting","#7B1FA2"},{"Creative","#388E3C"},{"Planning","#00796B"},
            {"Studying","#00E676"},{"Feeling Sick","#B71C1C"}
        };
        for (String[] t : tags)
            db.execSQL("INSERT OR IGNORE INTO " + TABLE_TAGS + "(name,color) VALUES(?,?)",
                new Object[]{t[0], t[1]});
    }
}
