package com.activitylogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class StudyDao {
    private final DatabaseHelper db;
    private final Context ctx;

    public StudyDao(Context context) {
        ctx = context.getApplicationContext();
        db  = DatabaseHelper.getInstance(context);
    }

    // ── Subjects ──────────────────────────────────────────
    public long insertSubject(String name, String color, double hours) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_SUBJ_NAME, name);
        cv.put(DatabaseHelper.COL_SUBJ_COLOR, color);
        cv.put(DatabaseHelper.COL_SUBJ_HOURS_REQ, hours);
        return db.getWritableDatabase().insert(DatabaseHelper.TABLE_SUBJECTS, null, cv);
    }

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().query(
            DatabaseHelper.TABLE_SUBJECTS, null, null, null, null, null,
            DatabaseHelper.COL_SUBJ_ID + " ASC");
        if (c != null) {
            while (c.moveToNext()) list.add(new Subject(
                c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_NAME)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_COLOR)),
                c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_SUBJ_HOURS_REQ))));
            c.close();
        }
        return list;
    }

    public void deleteSubject(long id) {
        db.getWritableDatabase().delete(DatabaseHelper.TABLE_SUBJECTS,
            DatabaseHelper.COL_SUBJ_ID + "=?", new String[]{String.valueOf(id)});
        // Cascade chapters
        List<Chapter> chapters = getChaptersForSubject(id);
        for (Chapter ch : chapters) deleteChapter(ch.getId());
    }

    // ── Chapters ──────────────────────────────────────────
    public long insertChapter(long subjectId, String name, double hours, int sets) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_CHAP_SUBJ_ID, subjectId);
        cv.put(DatabaseHelper.COL_CHAP_NAME, name);
        cv.put(DatabaseHelper.COL_CHAP_HOURS, hours);
        cv.put(DatabaseHelper.COL_CHAP_SETS, sets);
        cv.put(DatabaseHelper.COL_CHAP_DONE, 0);
        return db.getWritableDatabase().insert(DatabaseHelper.TABLE_CHAPTERS, null, cv);
    }

    public List<Chapter> getChaptersForSubject(long subjectId) {
        List<Chapter> list = new ArrayList<>();
        Cursor c = db.getReadableDatabase().query(DatabaseHelper.TABLE_CHAPTERS, null,
            DatabaseHelper.COL_CHAP_SUBJ_ID + "=?",
            new String[]{String.valueOf(subjectId)}, null, null,
            DatabaseHelper.COL_CHAP_ID + " ASC");
        if (c != null) {
            while (c.moveToNext()) list.add(new Chapter(
                c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_CHAP_ID)),
                c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_CHAP_SUBJ_ID)),
                c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_CHAP_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COL_CHAP_HOURS)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_CHAP_SETS)),
                c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_CHAP_DONE)) == 1));
            c.close();
        }
        return list;
    }

    public void markChapterDone(long id, boolean done) {
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_CHAP_DONE, done ? 1 : 0);
        db.getWritableDatabase().update(DatabaseHelper.TABLE_CHAPTERS, cv,
            DatabaseHelper.COL_CHAP_ID + "=?", new String[]{String.valueOf(id)});
    }

    public void deleteChapter(long id) {
        db.getWritableDatabase().delete(DatabaseHelper.TABLE_CHAPTERS,
            DatabaseHelper.COL_CHAP_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ── Study Prefs ───────────────────────────────────────
    // Returns int[4]: {dailyMinutes, startHour, daysOffBitmask, examDateExists}
    public int[] getStudyPrefs() {
        Cursor c = db.getReadableDatabase().query(
            DatabaseHelper.TABLE_STUDY_PREFS, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            int daily   = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PREF_DAILY_MINUTES));
            int start   = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COL_PREF_START_HOUR));
            String days = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PREF_DAYS_OFF));
            long exam   = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PREF_EXAM_DATE));
            c.close();
            return new int[]{daily, start, parseDaysBitmask(days), (int)(exam > 0 ? exam : 0)};
        }
        if (c != null) c.close();
        return new int[]{120, 9, 0, 0}; // defaults: 2h/day, 9am, no days off
    }

    public long getExamDate() {
        Cursor c = db.getReadableDatabase().query(
            DatabaseHelper.TABLE_STUDY_PREFS, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            long exam = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.COL_PREF_EXAM_DATE));
            c.close();
            return exam;
        }
        if (c != null) c.close();
        return 0;
    }

    public String getDaysOffString() {
        Cursor c = db.getReadableDatabase().query(
            DatabaseHelper.TABLE_STUDY_PREFS, null, null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            String s = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COL_PREF_DAYS_OFF));
            c.close();
            return s != null ? s : "";
        }
        if (c != null) c.close();
        return "";
    }

    public void saveStudyPrefs(int dailyMinutes, int startHour, String daysOff, long examDate) {
        db.getWritableDatabase().delete(DatabaseHelper.TABLE_STUDY_PREFS, null, null);
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_PREF_DAILY_MINUTES, dailyMinutes);
        cv.put(DatabaseHelper.COL_PREF_START_HOUR, startHour);
        cv.put(DatabaseHelper.COL_PREF_DAYS_OFF, daysOff);
        cv.put(DatabaseHelper.COL_PREF_EXAM_DATE, examDate);
        db.getWritableDatabase().insert(DatabaseHelper.TABLE_STUDY_PREFS, null, cv);
    }

    private int parseDaysBitmask(String days) {
        if (days == null || days.isEmpty()) return 0;
        int mask = 0;
        for (String d : days.split(",")) {
            try { mask |= (1 << Integer.parseInt(d.trim())); } catch (Exception ignored) {}
        }
        return mask;
    }
}
