package com.activitylogger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s) {
        boolean dark = ThemeHelper.isDark(getContext());
        ScrollView scroll = new ScrollView(getContext());
        scroll.setBackgroundColor(ThemeHelper.bgColor(getContext()));
        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 40);
        scroll.addView(root);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText("Settings");
        tvTitle.setTextSize(28); tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(ThemeHelper.textPrimary(getContext()));
        tvTitle.setPadding(48, 48, 48, 32);
        root.addView(tvTitle);

        addSectionHeader(root, "APPEARANCE");

        LinearLayout nightCard = new LinearLayout(getContext());
        nightCard.setOrientation(LinearLayout.VERTICAL);
        nightCard.setBackgroundColor(ThemeHelper.surfaceColor(getContext()));
        nightCard.setPadding(48, 20, 48, 20);

        TextView tvNightLabel = new TextView(getContext());
        tvNightLabel.setText("Night Mode");
        tvNightLabel.setTextSize(15); tvNightLabel.setTypeface(null, Typeface.BOLD);
        tvNightLabel.setTextColor(ThemeHelper.textPrimary(getContext()));
        nightCard.addView(tvNightLabel);

        TextView tvNightSub = new TextView(getContext());
        tvNightSub.setText("Auto switches dark 8 PM - 7 AM");
        tvNightSub.setTextSize(12);
        tvNightSub.setTextColor(ThemeHelper.textSecondary(getContext()));
        tvNightSub.setPadding(0, 2, 0, 16);
        nightCard.addView(tvNightSub);

        LinearLayout toggleRow = new LinearLayout(getContext());
        toggleRow.setOrientation(LinearLayout.HORIZONTAL);
        toggleRow.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        int currentMode = ThemeHelper.getMode(getContext());
        final TextView[] btns = new TextView[3];
        String[] labels = {"Sun  Light", "Moon  Dark", "Auto"};

        for (int i = 0; i < 3; i++) {
            final int mode = i;
            TextView btn = new TextView(getContext());
            btn.setText(labels[i]); btn.setTextSize(13);
            btn.setGravity(android.view.Gravity.CENTER);
            btn.setPadding(0, 20, 0, 20);
            btn.setClickable(true); btn.setFocusable(true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(i == 0 ? 0 : 4, 0, i == 2 ? 0 : 4, 0);
            btn.setLayoutParams(lp);
            applyToggle(btn, i == currentMode, dark);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    ThemeHelper.applyAndRecreate(getActivity(), mode);
                }
            });
            btns[i] = btn;
            toggleRow.addView(btn);
        }
        nightCard.addView(toggleRow);
        root.addView(nightCard);
        addDivider(root);

        addSectionHeader(root, "JOURNAL");
        addRow(root, "Journal Settings", "Wake time, sleep time, reminders", new Runnable() {
            @Override public void run() { showJournalSettings(); }
        });
        addRow(root, "Manage Tags", "Add, view or remove your activity tags", new Runnable() {
            @Override public void run() {
                new ManageTagsFragment().show(
                    getActivity().getSupportFragmentManager(), "tags");
            }
        });

        addSectionHeader(root, "ACCOUNT");
        addRow(root, "Your Profile", "Display name", new Runnable() {
            @Override public void run() { showProfileDialog(); }
        });

        addSectionHeader(root, "INFO");
        addRow(root, "How to use", null, new Runnable() {
            @Override public void run() { showHowToUse(); }
        });
        addRow(root, "App version", "1.0.0  Activity Logger", null);

        return scroll;
    }

    private void applyToggle(TextView tv, boolean selected, boolean dark) {
        try {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(12f);
            bg.setColor(selected ? 0xFF26C6DA : (dark ? 0xFF2A2A2A : 0xFFF0F0F0));
            tv.setBackground(bg);
            tv.setTextColor(selected ? Color.WHITE : (dark ? 0xFFAAAAAA : 0xFF757575));
            tv.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        } catch (Exception ig) {}
    }

    private void addSectionHeader(LinearLayout root, String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text); tv.setTextSize(11); tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(ThemeHelper.textSecondary(getContext()));
        tv.setPadding(48, 28, 48, 8);
        root.addView(tv);
    }

    private void addRow(LinearLayout root, String title, String subtitle, final Runnable onClick) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(ThemeHelper.surfaceColor(getContext()));
        row.setPadding(48, 18, 48, 18);
        if (onClick != null) {
            row.setClickable(true); row.setFocusable(true);
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { onClick.run(); }
            });
        }
        LinearLayout info = new LinearLayout(getContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(title); tvTitle.setTextSize(15);
        tvTitle.setTextColor(ThemeHelper.textPrimary(getContext()));
        info.addView(tvTitle);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView tvSub = new TextView(getContext());
            tvSub.setText(subtitle); tvSub.setTextSize(12);
            tvSub.setTextColor(ThemeHelper.textSecondary(getContext()));
            tvSub.setPadding(0, 2, 0, 0);
            info.addView(tvSub);
        }
        row.addView(info);

        if (onClick != null) {
            TextView tvChev = new TextView(getContext());
            tvChev.setText(">"); tvChev.setTextSize(22);
            tvChev.setTextColor(ThemeHelper.textSecondary(getContext()));
            row.addView(tvChev);
        }
        root.addView(row);
        addDivider(root);
    }

    private void addDivider(LinearLayout root) {
        View div = new View(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(48, 0, 0, 0); div.setLayoutParams(lp);
        div.setBackgroundColor(ThemeHelper.dividerColor(getContext()));
        root.addView(div);
    }

    private void showJournalSettings() {
        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", 0);
        String[] wakeOpts   = {"5:00 AM","6:00 AM","7:00 AM","8:00 AM","9:00 AM","10:00 AM"};
        String[] sleepOpts  = {"9:00 PM","10:00 PM","11:00 PM","12:00 AM","1:00 AM"};
        String[] remindOpts = {"Every 30 min","Every 1 hour","Every 2 hours",
                               "Every 3 hours","Every 4 hours","Off"};
        int[]    remindMins = {30, 60, 120, 180, 240, 0};

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);
        layout.setBackgroundColor(ThemeHelper.surfaceColor(getContext()));

        layout.addView(makeLabel("Wake up time"));
        final android.widget.Spinner spWake = new android.widget.Spinner(getContext());
        spWake.setAdapter(new android.widget.ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_dropdown_item, wakeOpts));
        spWake.setSelection(prefs.getInt("wake_idx", 2));
        layout.addView(spWake);

        layout.addView(makeLabel("Sleep time"));
        final android.widget.Spinner spSleep = new android.widget.Spinner(getContext());
        spSleep.setAdapter(new android.widget.ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_dropdown_item, sleepOpts));
        spSleep.setSelection(prefs.getInt("sleep_idx", 1));
        layout.addView(spSleep);

        layout.addView(makeLabel("Reminder interval"));
        final android.widget.Spinner spRemind = new android.widget.Spinner(getContext());
        spRemind.setAdapter(new android.widget.ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_dropdown_item, remindOpts));
        spRemind.setSelection(prefs.getInt("remind_idx", 1));
        layout.addView(spRemind);

        new AlertDialog.Builder(getContext()).setTitle("Journal Settings").setView(layout)
            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int w) {
                    int ri = spRemind.getSelectedItemPosition();
                    prefs.edit()
                        .putInt("wake_idx",  spWake.getSelectedItemPosition())
                        .putInt("sleep_idx", spSleep.getSelectedItemPosition())
                        .putInt("remind_idx", ri).apply();
                    int mins = remindMins[ri];
                    if (mins > 0) ReminderReceiver.schedule(getContext(), mins);
                    else          ReminderReceiver.cancel(getContext());
                    Toast.makeText(getContext(),
                        mins > 0 ? "Reminder set" : "Reminders off",
                        Toast.LENGTH_SHORT).show();
                }
            }).setNegativeButton("Cancel", null).show();
    }

    private void showProfileDialog() {
        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", 0);
        final android.widget.EditText et = new android.widget.EditText(getContext());
        et.setHint("Your name"); et.setPadding(48, 32, 48, 16);
        et.setText(prefs.getString("user_name", ""));
        et.setTextColor(ThemeHelper.textPrimary(getContext()));
        et.setHintTextColor(ThemeHelper.textHint(getContext()));
        new AlertDialog.Builder(getContext()).setTitle("Your Name").setView(et)
            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int w) {
                    prefs.edit().putString("user_name",
                        et.getText().toString().trim()).apply();
                    Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                }
            }).setNegativeButton("Cancel", null).show();
    }

    private void showHowToUse() {
        new AlertDialog.Builder(getContext()).setTitle("How to use")
            .setMessage(
                "Journal\nTap any hour to log. Long-press to edit, star or delete.\n\n" +
                "Stars\nLong-press an entry to star it. Find starred in menu.\n\n" +
                "Analytics\nSee tag breakdown. Tap any tag to see its entries.\n\n" +
                "Manage Tags\nSettings > Manage Tags. Add custom tags with any color.\n\n" +
                "Study Planner\nMenu > Study Planner. Set schedule, add subjects and chapters, tap Generate.\n\n" +
                "Night Mode\nSettings > Night Mode. Auto switches dark at 8 PM.")
            .setPositiveButton("Got it", null).show();
    }

    private TextView makeLabel(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text); tv.setTextSize(13);
        tv.setTextColor(ThemeHelper.textSecondary(getContext()));
        tv.setPadding(0, 16, 0, 4);
        return tv;
    }
}
