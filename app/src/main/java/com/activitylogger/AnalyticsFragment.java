package com.activitylogger;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private EntryDao     dao;
    private int          filterMode = 1;
    private TextView     btnToday, btnWeek, btnMonth;
    private LinearLayout llSummaryCards, llTagChart;
    private TextView     tvTagEmpty, tvPeriodLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s) {
        dao = new EntryDao(getContext());

        ScrollView scroll = new ScrollView(getContext());
        scroll.setBackgroundColor(ThemeHelper.bgColor(getContext()));

        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 80);
        scroll.addView(root);

        // Header
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(LinearLayout.VERTICAL);
        header.setBackgroundColor(ThemeHelper.isDark(getContext())
            ? 0xFF0D1B2A : 0xFFFFF9E6);
        header.setPadding(48, 48, 48, 0);
        root.addView(header);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText("Analytics");
        tvTitle.setTextSize(24); tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(ThemeHelper.textPrimary(getContext()));
        tvTitle.setPadding(0, 0, 0, 4);
        header.addView(tvTitle);

        tvPeriodLabel = new TextView(getContext());
        tvPeriodLabel.setTextSize(13);
        tvPeriodLabel.setTextColor(ThemeHelper.textSecondary(getContext()));
        tvPeriodLabel.setPadding(0, 0, 0, 20);
        header.addView(tvPeriodLabel);

        LinearLayout pillRow = new LinearLayout(getContext());
        pillRow.setOrientation(LinearLayout.HORIZONTAL);
        pillRow.setPadding(0, 0, 0, 20);

        btnToday = makePill("Today");
        btnWeek  = makePill("Week");
        btnMonth = makePill("Month");
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pp.setMargins(0, 0, 10, 0);
        btnToday.setLayoutParams(pp);
        btnWeek.setLayoutParams(new LinearLayout.LayoutParams(pp));
        pillRow.addView(btnToday); pillRow.addView(btnWeek); pillRow.addView(btnMonth);
        header.addView(pillRow);

        btnToday.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setFilter(0); }
        });
        btnWeek.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setFilter(1); }
        });
        btnMonth.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { setFilter(2); }
        });

        // Summary cards
        llSummaryCards = new LinearLayout(getContext());
        llSummaryCards.setOrientation(LinearLayout.HORIZONTAL);
        llSummaryCards.setPadding(16, 20, 16, 4);
        root.addView(llSummaryCards);

        // Tag breakdown header
        TextView tvTagHeader = new TextView(getContext());
        tvTagHeader.setText("Tag Breakdown");
        tvTagHeader.setTextSize(16); tvTagHeader.setTypeface(null, Typeface.BOLD);
        tvTagHeader.setTextColor(ThemeHelper.textPrimary(getContext()));
        tvTagHeader.setPadding(48, 24, 48, 8);
        root.addView(tvTagHeader);

        tvTagEmpty = new TextView(getContext());
        tvTagEmpty.setText("No entries logged in this period yet.");
        tvTagEmpty.setTextSize(14); tvTagEmpty.setTextColor(ThemeHelper.textSecondary(getContext()));
        tvTagEmpty.setPadding(48, 8, 48, 8);
        tvTagEmpty.setVisibility(View.GONE);
        root.addView(tvTagEmpty);

        llTagChart = new LinearLayout(getContext());
        llTagChart.setOrientation(LinearLayout.VERTICAL);
        root.addView(llTagChart);

        setFilter(1);
        return scroll;
    }

    private void setFilter(int mode) {
        filterMode = mode;
        setPillActive(btnToday, mode == 0);
        setPillActive(btnWeek,  mode == 1);
        setPillActive(btnMonth, mode == 2);
        loadStats();
    }

    private void loadStats() {
        final long today = DateHelper.startOfDay(System.currentTimeMillis());
        final long end   = DateHelper.endOfDay(today);
        final long start = filterMode == 0 ? today
            : filterMode == 1 ? today - 6 * 86400_000L
            : today - 29 * 86400_000L;
        final String periodLabel = filterMode == 0
            ? new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(new Date(today))
            : filterMode == 1 ? "Last 7 days" : "Last 30 days";
        tvPeriodLabel.setText(periodLabel);

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                try {
                    final List<TagStat> stats   = dao.getTagStats(start, end);
                    final int           total   = dao.getTotalEntries(start, end);
                    final int           active  = dao.getActiveDays(start, end);
                    final int           streak  = dao.getCurrentStreak();
                    final String        topMood = dao.getTopMood(start, end);
                    if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            renderSummaryCards(total, active, streak, topMood);
                            renderTagChart(stats, total, start, end);
                        }
                    });
                } catch (Exception e) { CrashLogger.log(getContext(), "Analytics.load", e); }
            }
        });
    }

    private void renderSummaryCards(int total, int active, int streak, String topMood) {
        llSummaryCards.removeAllViews();
        String period = filterMode == 0 ? "today" : filterMode == 1 ? "this week" : "this month";
        addCard("📝", String.valueOf(total), "entries\n" + period, "#26C6DA");
        addCard("📅", String.valueOf(active), "active\ndays", "#4CAF50");
        addCard("🔥", String.valueOf(streak), "day\nstreak", "#FF9800");
        addCard(topMood != null ? topMood : "—", topMood != null ? "top" : "no",
            "mood\nlogged", "#E91E63");
    }

    private void addCard(String icon, String value, String label, String colorHex) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(android.view.Gravity.CENTER);
        card.setPadding(8, 20, 8, 20);
        card.setBackgroundColor(ThemeHelper.surfaceColor(getContext()));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(6, 0, 6, 0); card.setLayoutParams(lp);

        TextView tvIcon = new TextView(getContext());
        tvIcon.setText(icon); tvIcon.setTextSize(20);
        tvIcon.setGravity(android.view.Gravity.CENTER);
        tvIcon.setPadding(0, 0, 0, 6);
        card.addView(tvIcon);

        TextView tvValue = new TextView(getContext());
        tvValue.setText(value); tvValue.setTextSize(22);
        tvValue.setTypeface(null, Typeface.BOLD);
        tvValue.setTextColor(ThemeHelper.textPrimary(getContext()));
        tvValue.setGravity(android.view.Gravity.CENTER);
        card.addView(tvValue);

        TextView tvLabel = new TextView(getContext());
        tvLabel.setText(label); tvLabel.setTextSize(10);
        tvLabel.setTextColor(ThemeHelper.textSecondary(getContext()));
        tvLabel.setGravity(android.view.Gravity.CENTER);
        tvLabel.setLineSpacing(0, 1.1f);
        card.addView(tvLabel);

        View bar = new View(getContext());
        bar.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 3));
        try { bar.setBackgroundColor(Color.parseColor(colorHex)); } catch (Exception ig) {}
        card.addView(bar);

        llSummaryCards.addView(card);
    }

    private void renderTagChart(final List<TagStat> stats, int total,
                                 final long start, final long end) {
        llTagChart.removeAllViews();
        if (stats.isEmpty()) { tvTagEmpty.setVisibility(View.VISIBLE); return; }
        tvTagEmpty.setVisibility(View.GONE);
        int maxCount = stats.get(0).getEntryCount();

        for (final TagStat stat : stats) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setBackgroundColor(ThemeHelper.surfaceColor(getContext()));
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rlp.setMargins(24, 3, 24, 3); row.setLayoutParams(rlp);
            row.setPadding(24, 18, 24, 18);

            // Top line
            LinearLayout topLine = new LinearLayout(getContext());
            topLine.setOrientation(LinearLayout.HORIZONTAL);
            topLine.setGravity(android.view.Gravity.CENTER_VERTICAL);
            topLine.setPadding(0, 0, 0, 10);

            View dot = new View(getContext());
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(12, 12);
            dp.setMargins(0, 0, 12, 0); dot.setLayoutParams(dp);
            try {
                GradientDrawable dg = new GradientDrawable();
                dg.setShape(GradientDrawable.OVAL);
                dg.setColor(Color.parseColor(stat.getColor()));
                dot.setBackground(dg);
            } catch (Exception ig) {}
            topLine.addView(dot);

            TextView tvName = new TextView(getContext());
            tvName.setText(stat.getName()); tvName.setTextSize(14);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setTextColor(ThemeHelper.textPrimary(getContext()));
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            topLine.addView(tvName);

            TextView tvCount = new TextView(getContext());
            int cnt = stat.getEntryCount();
            tvCount.setText(cnt + (cnt == 1 ? " entry" : " entries"));
            tvCount.setTextSize(12); tvCount.setTypeface(null, Typeface.BOLD);
            tvCount.setTextColor(Color.WHITE); tvCount.setPadding(20, 6, 20, 6);
            try {
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.RECTANGLE);
                bg.setCornerRadius(40f);
                bg.setColor(Color.parseColor(stat.getColor()));
                tvCount.setBackground(bg);
            } catch (Exception ig) {}
            topLine.addView(tvCount);
            row.addView(topLine);

            // Bar track
            LinearLayout barTrack = new LinearLayout(getContext());
            barTrack.setOrientation(LinearLayout.HORIZONTAL);
            barTrack.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 10));
            try {
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(ThemeHelper.isDark(getContext()) ? 0xFF333333 : 0xFFF0F0F0);
                bg.setCornerRadius(10f);
                barTrack.setBackground(bg);
            } catch (Exception ig) {}

            float fill = maxCount > 0 ? (float) stat.getEntryCount() / maxCount : 0f;
            View filled = new View(getContext());
            filled.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, fill));
            try {
                GradientDrawable fg = new GradientDrawable();
                fg.setColor(Color.parseColor(stat.getColor()));
                fg.setCornerRadius(10f);
                filled.setBackground(fg);
            } catch (Exception ig) {}
            barTrack.addView(filled);
            if (fill < 1f) {
                View rem = new View(getContext());
                rem.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1f - fill));
                barTrack.addView(rem);
            }
            row.addView(barTrack);

            int pct = total > 0 ? (int)(stat.getEntryCount() * 100f / total) : 0;
            TextView tvPct = new TextView(getContext());
            tvPct.setText(pct + "% of all entries");
            tvPct.setTextSize(11); tvPct.setTextColor(ThemeHelper.textSecondary(getContext()));
            tvPct.setPadding(0, 6, 0, 0);
            row.addView(tvPct);

            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showTagEntries(stat, start, end); }
            });
            llTagChart.addView(row);
        }
    }

    private void showTagEntries(final TagStat stat, final long start, final long end) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                final List<Entry> entries = dao.getEntriesForTag(stat.getTagId(), start, end);
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (entries.isEmpty()) {
                            new AlertDialog.Builder(getContext())
                                .setTitle(stat.getName()).setMessage("No entries found.")
                                .setPositiveButton("OK", null).show();
                            return;
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                        StringBuilder sb = new StringBuilder();
                        for (Entry e : entries) {
                            sb.append(sdf.format(new Date(e.getTimestamp())));
                            if (e.getMood() != null && !e.getMood().isEmpty())
                                sb.append("  ").append(e.getMood());
                            sb.append("\n").append(e.getContent()).append("\n\n");
                        }
                        android.widget.EditText et = new android.widget.EditText(getContext());
                        et.setText(sb.toString().trim());
                        et.setTextSize(13f);
                        et.setTextColor(ThemeHelper.textPrimary(getContext()));
                        et.setBackgroundColor(ThemeHelper.surfaceColor(getContext()));
                        et.setFocusable(false); et.setTextIsSelectable(true);
                        et.setPadding(48, 32, 48, 32);
                        android.widget.ScrollView sv = new android.widget.ScrollView(getContext());
                        sv.addView(et);
                        new AlertDialog.Builder(getContext())
                            .setTitle(stat.getName() + "  (" + entries.size() + ")")
                            .setView(sv).setPositiveButton("Close", null).show();
                    }
                });
            }
        });
    }

    private TextView makePill(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text); tv.setTextSize(13);
        tv.setPadding(32, 14, 32, 14);
        tv.setClickable(true); tv.setFocusable(true);
        setPillActive(tv, false);
        return tv;
    }

    private void setPillActive(TextView tv, boolean active) {
        try {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(40f);
            bg.setColor(active ? 0xFF26C6DA : ThemeHelper.pillUnselected(getContext()));
            tv.setBackground(bg);
            tv.setTextColor(active ? Color.WHITE : ThemeHelper.pillUnselectedText(getContext()));
            tv.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        } catch (Exception ig) {}
    }
}
