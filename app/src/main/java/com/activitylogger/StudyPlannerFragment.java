package com.activitylogger;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudyPlannerFragment extends DialogFragment {

    private StudyDao     dao;
    private LinearLayout llSubjects;
    private TextView     tvScheduleSummary;

    @Override public void onCreate(Bundle s) {
        super.onCreate(s);
        setStyle(DialogFragment.STYLE_NORMAL,
            android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle s) {
        dao = new StudyDao(getContext());

        ScrollView scroll = new ScrollView(getContext());
        scroll.setBackgroundColor(Color.parseColor("#F0F4F8"));

        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 100);
        scroll.addView(root);

        // ── Top bar ──
        LinearLayout topBar = new LinearLayout(getContext());
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(Color.WHITE);
        topBar.setPadding(16, 0, 16, 0);
        LinearLayout.LayoutParams tbLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 56);
        topBar.setLayoutParams(tbLp);

        TextView tvClose = new TextView(getContext());
        tvClose.setText("✕");
        tvClose.setTextSize(20); tvClose.setTextColor(Color.parseColor("#9E9E9E"));
        tvClose.setPadding(16, 0, 32, 0);
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dismiss(); }
        });
        topBar.addView(tvClose);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText("Study Planner 📚");
        tvTitle.setTextSize(18); tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#212121"));
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topBar.addView(tvTitle);

        // Settings gear button
        TextView tvGear = new TextView(getContext());
        tvGear.setText("⚙");
        tvGear.setTextSize(22); tvGear.setTextColor(Color.parseColor("#26C6DA"));
        tvGear.setPadding(16, 0, 16, 0);
        tvGear.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showScheduleSetup(); }
        });
        topBar.addView(tvGear);
        root.addView(topBar);

        // ── Schedule summary card ──
        tvScheduleSummary = new TextView(getContext());
        tvScheduleSummary.setTextSize(13);
        tvScheduleSummary.setTextColor(Color.parseColor("#424242"));
        tvScheduleSummary.setPadding(48, 20, 48, 20);
        tvScheduleSummary.setBackgroundColor(Color.parseColor("#E3F2FD"));
        root.addView(tvScheduleSummary);

        // ── Subjects section header ──
        LinearLayout subjHeader = new LinearLayout(getContext());
        subjHeader.setOrientation(LinearLayout.HORIZONTAL);
        subjHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);
        subjHeader.setPadding(48, 28, 48, 8);

        TextView tvSubjTitle = new TextView(getContext());
        tvSubjTitle.setText("Subjects");
        tvSubjTitle.setTextSize(16); tvSubjTitle.setTypeface(null, Typeface.BOLD);
        tvSubjTitle.setTextColor(Color.parseColor("#212121"));
        tvSubjTitle.setLayoutParams(new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        subjHeader.addView(tvSubjTitle);

        TextView btnAdd = new TextView(getContext());
        btnAdd.setText("+ Add");
        btnAdd.setTextSize(14); btnAdd.setTextColor(Color.parseColor("#26C6DA"));
        btnAdd.setTypeface(null, Typeface.BOLD);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAddSubjectDialog(); }
        });
        subjHeader.addView(btnAdd);

        TextView btnImport = new TextView(getContext());
        btnImport.setText("JEE");
        btnImport.setTextSize(13);
        btnImport.setTextColor(0xFFFF9800);
        btnImport.setTypeface(null, android.graphics.Typeface.BOLD);
        btnImport.setPadding(16, 0, 8, 0);
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { confirmImportJee(); }
        });
        subjHeader.addView(btnImport);
        root.addView(subjHeader);

        llSubjects = new LinearLayout(getContext());
        llSubjects.setOrientation(LinearLayout.VERTICAL);
        root.addView(llSubjects);

        loadAll();
        return scroll;
    }

    private void loadAll() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                try {
                    final List<Subject> subjects = dao.getAllSubjects();
                    final int[]         prefs    = dao.getStudyPrefs();
                    final long          examDate = dao.getExamDate();
                    if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            renderScheduleSummary(prefs, examDate);
                            renderSubjects(subjects);
                        }
                    });
                } catch (Exception e) { CrashLogger.log(getContext(), "SPF.loadAll", e); }
            }
        });
    }

    // ── Schedule summary ──────────────────────────────────
    private void renderScheduleSummary(int[] prefs, long examDate) {
        int dailyMin  = prefs[0];
        int startHour = prefs[1];
        String daysOff = dao.getDaysOffString();

        String time   = formatHour(startHour) + " – " + formatHour(startHour + dailyMin/60);
        String offStr = daysOffLabel(daysOff);
        String examStr = examDate > 0
            ? "  •  Exam: " + new SimpleDateFormat("MMM d", Locale.getDefault()).format(new Date(examDate))
            : "";

        String summary = "⏰ " + (dailyMin/60) + "h " + (dailyMin%60>0?dailyMin%60+"m ":"") +
            "daily  •  " + time + (offStr.isEmpty() ? "" : "  •  Off: " + offStr) + examStr;
        tvScheduleSummary.setText(summary);
        tvScheduleSummary.setVisibility(View.VISIBLE);
    }

    private String formatHour(int h) {
        h = h % 24;
        int display = h > 12 ? h - 12 : (h == 0 ? 12 : h);
        return display + (h >= 12 ? "pm" : "am");
    }

    private String daysOffLabel(String daysOff) {
        if (daysOff == null || daysOff.isEmpty()) return "";
        String[] names = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        StringBuilder sb = new StringBuilder();
        for (String d : daysOff.split(",")) {
            try {
                int n = Integer.parseInt(d.trim());
                if (n >= 1 && n <= 7) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(names[n-1]);
                }
            } catch (Exception ignored) {}
        }
        return sb.toString();
    }

    // ── Subjects list ─────────────────────────────────────
    private void renderSubjects(List<Subject> subjects) {
        llSubjects.removeAllViews();
        if (subjects.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No subjects yet.");
            empty.setTextSize(13); empty.setTextColor(Color.parseColor("#BDBDBD"));
            empty.setPadding(48, 8, 48, 8);
            llSubjects.addView(empty);
            return;
        }
        for (final Subject s : subjects) {
            LinearLayout card = new LinearLayout(getContext());
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            clp.setMargins(24, 6, 24, 6);
            card.setLayoutParams(clp);
            card.setPadding(16, 16, 16, 16);

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);

            // Color dot
            View dot = new View(getContext());
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(14, 14);
            dp.setMargins(0, 0, 16, 0);
            dot.setLayoutParams(dp);
            try { dot.setBackgroundColor(Color.parseColor(s.getColor())); } catch (Exception ig) {}
            row.addView(dot);

            LinearLayout info = new LinearLayout(getContext());
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvName = new TextView(getContext());
            tvName.setText(s.getName());
            tvName.setTextSize(15); tvName.setTypeface(null, Typeface.BOLD);
            tvName.setTextColor(Color.parseColor("#212121"));
            info.addView(tvName);

            TextView tvMeta = new TextView(getContext());
            tvMeta.setText(s.getHoursRequired() + "h total");
            tvMeta.setTextSize(12); tvMeta.setTextColor(Color.parseColor("#9E9E9E"));
            info.addView(tvMeta);
            row.addView(info);
            card.addView(row);

            card.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showChaptersDialog(s); }
            });
            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    new AlertDialog.Builder(getContext())
                        .setMessage("Delete \"" + s.getName() + "\" and all its chapters?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int w) {
                                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                                    @Override public void run() {
                                        dao.deleteSubject(s.getId()); loadAll();
                                    }
                                });
                            }
                        }).setNegativeButton("Cancel", null).show();
                    return true;
                }
            });
            llSubjects.addView(card);
        }
    }

    // ── Study Schedule Setup dialog ───────────────────────
    private void showScheduleSetup() {
        // Load existing prefs
        final int[]  prefs    = dao.getStudyPrefs();
        final long   examDate = dao.getExamDate();
        final String daysOff  = dao.getDaysOffString();

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        // Daily study hours
        TextView tvH = makeLabel("How many hours per day?");
        layout.addView(tvH);

        final android.widget.EditText etHours = new android.widget.EditText(getContext());
        etHours.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etHours.setHint("Hours per day  (0 = no limit)");
        etHours.setTextColor(ThemeHelper.textPrimary(getContext()));
        etHours.setHintTextColor(ThemeHelper.textHint(getContext()));
        etHours.setPadding(16, 16, 16, 16);
        float prevHours = prefs[0] > 0 ? prefs[0] / 60.0f : 2.0f;
        etHours.setText(prevHours == (int) prevHours ? String.valueOf((int) prevHours) : String.valueOf(prevHours));
        layout.addView(etHours);

        // Start time
        layout.addView(makeLabel("Preferred start time"));
        final String[] timeLabels = {"6 am","7 am","8 am","9 am","2 pm","4 pm","6 pm","7 pm","8 pm"};
        final int[]    timeValues = {6, 7, 8, 9, 14, 16, 18, 19, 20};
        final int[]    selTime    = {3}; // default 9am
        for (int i = 0; i < timeValues.length; i++)
            if (timeValues[i] == prefs[1]) { selTime[0] = i; break; }

        // Two rows of 5 and 4
        final TextView[] timeBtns = new TextView[timeLabels.length];
        LinearLayout timeRow1 = makeFlowRow();
        LinearLayout timeRow2 = makeFlowRow();
        for (int i = 0; i < timeLabels.length; i++) {
            final int idx = i;
            TextView tv = makePill(timeLabels[i], i == selTime[0]);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    selTime[0] = idx;
                    for (TextView b : timeBtns) setPillState(b, false);
                    setPillState(timeBtns[idx], true);
                }
            });
            timeBtns[i] = tv;
            (i < 5 ? timeRow1 : timeRow2).addView(tv);
        }
        layout.addView(timeRow1);
        layout.addView(timeRow2);

        // Days off
        layout.addView(makeLabel("Rest days (no studying)"));
        String[] dayNames = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        final CheckBox[] dayCbs = new CheckBox[7];
        LinearLayout dayRow = makeFlowRow();
        for (int i = 0; i < 7; i++) {
            final int dayNum = i + 1;
            CheckBox cb = new CheckBox(getContext());
            cb.setText(dayNames[i]);
            cb.setTextSize(13);
            cb.setChecked(daysOff.contains(String.valueOf(dayNum)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            cb.setLayoutParams(lp);
            dayCbs[i] = cb;
            dayRow.addView(cb);
        }
        layout.addView(dayRow);

        // Exam date
        layout.addView(makeLabel("Exam / deadline date (optional)"));
        final long[] examTs = {examDate};
        final TextView tvExam = new TextView(getContext());
        tvExam.setText(examDate > 0
            ? new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(examDate))
            : "Tap to set date");
        tvExam.setTextSize(14); tvExam.setTextColor(Color.parseColor("#26C6DA"));
        tvExam.setPadding(0, 8, 0, 16);
        tvExam.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                if (examTs[0] > 0) cal.setTimeInMillis(examTs[0]);
                new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override public void onDateSet(DatePicker vw, int y, int m, int d) {
                            Calendar p = Calendar.getInstance();
                            p.set(y, m, d, 23, 59, 0);
                            examTs[0] = p.getTimeInMillis();
                            tvExam.setText(new SimpleDateFormat("MMM d, yyyy",
                                Locale.getDefault()).format(new Date(examTs[0])));
                        }
                    },
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        layout.addView(tvExam);

        new AlertDialog.Builder(getContext())
            .setTitle("Study Schedule")
            .setView(layout)
            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int w) {
                    float hoursInput = 2;
                    try { hoursInput = Float.parseFloat(etHours.getText().toString().trim()); } catch (Exception ex) { hoursInput = 2; }
                    int dailyMin = (int)(hoursInput * 60);
                    int startHour = timeValues[selTime[0]];
                    StringBuilder daysOffSb = new StringBuilder();
                    for (int i = 0; i < 7; i++) {
                        if (dayCbs[i].isChecked()) {
                            if (daysOffSb.length() > 0) daysOffSb.append(",");
                            daysOffSb.append(i + 1);
                        }
                    }
                    dao.saveStudyPrefs(dailyMin, startHour, daysOffSb.toString(), examTs[0]);
                    loadAll();
                    Toast.makeText(getContext(), "Schedule saved ✓", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    // ── Chapters dialog ───────────────────────────────────
    private void showChaptersDialog(final Subject subject) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                final List<Chapter> chapters = dao.getChaptersForSubject(subject.getId());
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        String[] items = new String[chapters.size() + 1];
                        for (int i = 0; i < chapters.size(); i++) {
                            Chapter c = chapters.get(i);
                            items[i] = (c.isDone() ? "✓ " : "○ ") + c.getName() +
                                "  (" + c.getHoursRequired() + "h × " + c.getSetsCount() + " sets)";
                        }
                        items[chapters.size()] = "+ Add chapter";

                        new AlertDialog.Builder(getContext())
                            .setTitle(subject.getName())
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface d, int which) {
                                    if (which == chapters.size()) showAddChapterDialog(subject);
                                    else showChapterOptions(chapters.get(which));
                                }
                            })
                            .setNegativeButton("Close", null).show();
                    }
                });
            }
        });
    }

    private void showChapterOptions(final Chapter ch) {
        new AlertDialog.Builder(getContext())
            .setTitle(ch.getName())
            .setItems(new String[]{ch.isDone() ? "Mark as pending" : "✓ Mark done", "✕ Delete"},
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == 0) {
                            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                                @Override public void run() {
                                    dao.markChapterDone(ch.getId(), !ch.isDone()); loadAll();
                                }
                            });
                        } else {
                            new AlertDialog.Builder(getContext())
                                .setMessage("Delete chapter \"" + ch.getName() + "\"?")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface d2, int w) {
                                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                                            @Override public void run() {
                                                dao.deleteChapter(ch.getId()); loadAll();
                                            }
                                        });
                                    }
                                }).setNegativeButton("Cancel", null).show();
                        }
                    }
                }).show();
    }

    private void showAddSubjectDialog() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        final EditText etName = new EditText(getContext());
        etName.setHint("Subject name  e.g. Mathematics");
        etName.setBackground(null); layout.addView(etName);

        final EditText etHours = new EditText(getContext());
        etHours.setHint("Total hours required  e.g. 40");
        etHours.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etHours.setBackground(null); layout.addView(etHours);

        final String[] colors = {"#26C6DA","#4CAF50","#FFC107","#E91E63",
                                  "#9C27B0","#FF5722","#3F51B5","#795548"};
        final int[] picked = {0};
        LinearLayout colorRow = new LinearLayout(getContext());
        colorRow.setOrientation(LinearLayout.HORIZONTAL);
        colorRow.setPadding(0, 20, 0, 0);
        final View[] dots = new View[colors.length];
        for (int i = 0; i < colors.length; i++) {
            final int idx = i;
            View dot = new View(getContext());
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(44, 44);
            dp.setMargins(0, 0, 12, 0);
            dot.setLayoutParams(dp);
            try { dot.setBackgroundColor(Color.parseColor(colors[i])); } catch (Exception ig) {}
            dot.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    picked[0] = idx;
                    for (View d : dots) d.setScaleX(1f);
                    v.setScaleX(1.5f);
                }
            });
            dots[i] = dot; colorRow.addView(dot);
        }
        layout.addView(colorRow);

        new AlertDialog.Builder(getContext())
            .setTitle("New Subject").setView(layout)
            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int w) {
                    final String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    double hours = 0;
                    try { hours = Double.parseDouble(etHours.getText().toString().trim()); }
                    catch (Exception ig) {}
                    final double fh = hours; final String color = colors[picked[0]];
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        @Override public void run() { dao.insertSubject(name,color,fh); loadAll(); }
                    });
                }
            }).setNegativeButton("Cancel", null).show();
    }

    private void showAddChapterDialog(final Subject subject) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);

        final EditText etName = new EditText(getContext());
        etName.setHint("Chapter name  e.g. Chapter 3 – Calculus");
        etName.setBackground(null); layout.addView(etName);

        final EditText etHours = new EditText(getContext());
        etHours.setHint("Hours to study this chapter  e.g. 2.5");
        etHours.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etHours.setBackground(null); layout.addView(etHours);

        final EditText etSets = new EditText(getContext());
        etSets.setHint("Number of revisions / sets  e.g. 3");
        etSets.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etSets.setBackground(null); layout.addView(etSets);

        new AlertDialog.Builder(getContext())
            .setTitle("Add Chapter").setView(layout)
            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int w) {
                    final String name = etName.getText().toString().trim();
                    if (name.isEmpty()) return;
                    double hours = 1; int sets = 1;
                    try { hours = Double.parseDouble(etHours.getText().toString()); }
                    catch (Exception ig) {}
                    try { sets = Integer.parseInt(etSets.getText().toString()); }
                    catch (Exception ig) {}
                    final double fh = hours; final int fs = sets;
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        @Override public void run() {
                            dao.insertChapter(subject.getId(), name, fh, fs); loadAll();
                        }
                    });
                }
            }).setNegativeButton("Cancel", null).show();
    }

    // ── UI helpers ────────────────────────────────────────
    private TextView makeLabel(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text); tv.setTextSize(13);
        tv.setTextColor(Color.parseColor("#757575"));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, 20, 0, 8);
        return tv;
    }

    private TextView makePill(String text, boolean selected) {
        TextView tv = new TextView(getContext());
        tv.setText(text); tv.setTextSize(12);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setPadding(8, 16, 8, 16);
        setPillState(tv, selected);
        return tv;
    }

    private void setPillState(TextView tv, boolean selected) {
        try {
            android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
            bg.setColor(selected ? Color.parseColor("#26C6DA") : Color.parseColor("#F0F0F0"));
            bg.setCornerRadius(40f);
            tv.setBackground(bg);
            tv.setTextColor(selected ? Color.WHITE : Color.parseColor("#424242"));
        } catch (Exception ignored) {}
    }

    private LinearLayout makeFlowRow() {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 6);
        row.setLayoutParams(lp);
        return row;
    }
    private void confirmImportJee() {
        new android.app.AlertDialog.Builder(getContext())
            .setTitle("Import JEE Chapters")
            .setMessage("This will DELETE all existing subjects and chapters and import Physics, Chemistry and Maths with capped 6h per chapter, 12h/day, deadline April 1 2026. Continue?")
            .setPositiveButton("Import", new android.content.DialogInterface.OnClickListener() {
                @Override public void onClick(android.content.DialogInterface d, int w) {
                    android.os.AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        @Override public void run() { doImportJee(); }
                    });
                }
            }).setNegativeButton("Cancel", null).show();
    }

    private double cap(double hrs) { return Math.min(hrs, 6.0); }

    private void doImportJee() {
        // Clear existing
        android.database.sqlite.SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CHAPTERS, null, null);
        db.delete(DatabaseHelper.TABLE_SUBJECTS, null, null);

        // Physics - blue
        long phy = dao.insertSubject("Physics", "#2196F3", 0);
        Object[][] phyChapters = {
            {"Kinematics", 15.83}, {"NLM", 16.5}, {"Work, Power, Energy", 10.78},
            {"Rotation Motion", 24.2}, {"Gravitation", 20.23}, {"Oscillation", 16.73},
            {"Waves", 19.0}, {"Thermodynamics", 16.0}, {"KTG", 13.82},
            {"Properties of Matter", 11.42}, {"Electric Charges & Fields", 20.62},
            {"Potential & Capacitance", 22.57}, {"Current Electricity", 23.5},
            {"Magnetism", 12.0}, {"EMI", 13.22}, {"Ray Optics", 26.03},
            {"Wave Optics", 14.5}, {"Modern Physics", 24.03}, {"COM", 16.67},
            {"Communication Systems", 4.25}, {"Experiments & Instruments", 4.0},
            {"Semiconductor", 9.3}, {"Purification", 4.0}
        };
        for (Object[] ch : phyChapters)
            dao.insertChapter(phy, (String)ch[0], cap((double)ch[1]), 0);

        // Chemistry - green
        long chem = dao.insertSubject("Chemistry", "#4CAF50", 0);
        Object[][] chemChapters = {
            {"Mole Concept", 9.5}, {"Atomic Structure", 10.72}, {"Chemical Bonding", 17.0},
            {"States of Matter", 9.0}, {"Thermodynamics", 20.0}, {"Equilibrium", 28.0},
            {"Electrochemistry", 17.25}, {"Chemical Kinetics", 20.0}, {"Surface Chemistry", 12.0},
            {"GOC", 18.0}, {"Hydrocarbons", 23.0}, {"Haloalkanes & Arenes", 18.5},
            {"Alcohols, Phenols, Ethers", 19.0}, {"Aldehyde, Ketone, Carb Acids", 10.5},
            {"Amines", 12.0}, {"Biomolecules", 9.5}, {"Periodic Table", 13.0},
            {"S Block", 4.0}, {"P Block", 22.0}, {"D & F Block", 10.0},
            {"Coordination Compounds", 16.0}, {"Metallurgy", 4.0}, {"Hydrogen", 1.67},
            {"Environmental Chemistry", 2.53}, {"Nuclear Chemistry", 5.0},
            {"Practical Chemistry", 12.0}
        };
        for (Object[] ch : chemChapters)
            dao.insertChapter(chem, (String)ch[0], cap((double)ch[1]), 0);

        // Maths - orange
        long math = dao.insertSubject("Mathematics", "#FF9800", 0);
        Object[][] mathChapters = {
            {"Sets & Relations", 9.0}, {"Functions", 9.23}, {"Complex Numbers", 8.28},
            {"Matrices", 13.5}, {"Determinants", 5.57}, {"Sequence & Series", 9.88},
            {"PnC", 7.3}, {"Binomial Theorem", 7.57}, {"Limits, Continuity & Derivatives", 9.55},
            {"Indefinite Integration", 8.57}, {"Quadratic Equations", 7.7},
            {"Differential Equations", 7.5}, {"Vectors & 3D Geometry", 11.02},
            {"Trigonometry & Inverse Trigo", 12.15}, {"Statistics", 6.23},
            {"Straight Lines", 9.1}, {"Circle", 11.92}, {"Conic Sections", 6.33},
            {"Solutions", 21.0}, {"Redox", 13.1}
        };
        for (Object[] ch : mathChapters)
            dao.insertChapter(math, (String)ch[0], cap((double)ch[1]), 0);

        // Save prefs: 12hr/day, 9am start, no days off, April 1 2026
        java.util.Calendar exam = java.util.Calendar.getInstance();
        exam.set(2026, 3, 1, 0, 0, 0);
        exam.set(java.util.Calendar.MILLISECOND, 0);
        dao.saveStudyPrefs(720, 9, "", exam.getTimeInMillis());

        if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
            @Override public void run() {
                loadAll();
                android.widget.Toast.makeText(getContext(),
                    "JEE chapters imported!", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

}