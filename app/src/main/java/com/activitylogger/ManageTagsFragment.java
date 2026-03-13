package com.activitylogger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class ManageTagsFragment extends DialogFragment {

    private EntryDao     dao;
    private LinearLayout llTags;

    @Override public void onCreate(Bundle s) {
        super.onCreate(s);
        setStyle(DialogFragment.STYLE_NORMAL,
            android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle s) {
        dao = new EntryDao(getContext());
        ScrollView scroll = new ScrollView(getContext());
        scroll.setBackgroundColor(ThemeHelper.bgColor(getContext()));
        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 60);
        scroll.addView(root);

        LinearLayout topBar = new LinearLayout(getContext());
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        topBar.setBackgroundColor(ThemeHelper.surfaceColor(getContext()));
        topBar.setPadding(24, 0, 24, 0);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 56));

        TextView tvClose = new TextView(getContext());
        tvClose.setText("✕"); tvClose.setTextSize(20);
        tvClose.setTextColor(ThemeHelper.textSecondary(getContext()));
        tvClose.setPadding(16, 0, 32, 0);
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dismiss(); }
        });
        topBar.addView(tvClose);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText("Manage Tags"); tvTitle.setTextSize(18);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(ThemeHelper.textPrimary(getContext()));
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        topBar.addView(tvTitle);

        TextView tvAdd = new TextView(getContext());
        tvAdd.setText("+ Add"); tvAdd.setTextSize(14);
        tvAdd.setTextColor(0xFF26C6DA); tvAdd.setTypeface(null, Typeface.BOLD);
        tvAdd.setPadding(16, 0, 16, 0);
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showAddTagDialog(); }
        });
        topBar.addView(tvAdd);
        root.addView(topBar);

        TextView tvNote = new TextView(getContext());
        tvNote.setText("Long-press any tag to delete it. Tags used in entries cannot be deleted.");
        tvNote.setTextSize(12); tvNote.setTextColor(ThemeHelper.textSecondary(getContext()));
        tvNote.setPadding(48, 16, 48, 8); tvNote.setLineSpacing(4, 1f);
        root.addView(tvNote);

        llTags = new LinearLayout(getContext());
        llTags.setOrientation(LinearLayout.VERTICAL);
        root.addView(llTags);
        loadTags();
        return scroll;
    }

    private void loadTags() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                final List<Tag> tags = dao.getAllTags();
                if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() { renderTags(tags); }
                });
            }
        });
    }

    private void renderTags(final List<Tag> tags) {
        llTags.removeAllViews();
        for (final Tag tag : tags) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setBackgroundColor(ThemeHelper.surfaceColor(getContext()));
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rlp.setMargins(0, 1, 0, 0); row.setLayoutParams(rlp);
            row.setPadding(48, 18, 48, 18);

            View dot = new View(getContext());
            LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(18, 18);
            dp.setMargins(0, 0, 20, 0); dot.setLayoutParams(dp);
            try {
                GradientDrawable gd = new GradientDrawable();
                gd.setShape(GradientDrawable.OVAL);
                gd.setColor(Color.parseColor(tag.getColor()));
                dot.setBackground(gd);
            } catch (Exception ig) {}
            row.addView(dot);

            TextView tvName = new TextView(getContext());
            tvName.setText(tag.getName()); tvName.setTextSize(15);
            tvName.setTextColor(ThemeHelper.textPrimary(getContext()));
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tvName);

            TextView tvColor = new TextView(getContext());
            tvColor.setText(tag.getColor()); tvColor.setTextSize(11);
            tvColor.setTextColor(Color.WHITE); tvColor.setPadding(16, 6, 16, 6);
            try {
                GradientDrawable gd = new GradientDrawable();
                gd.setShape(GradientDrawable.RECTANGLE);
                gd.setCornerRadius(20f);
                gd.setColor(Color.parseColor(tag.getColor()));
                tvColor.setBackground(gd);
            } catch (Exception ig) {}
            row.addView(tvColor);

            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    confirmDeleteTag(tag); return true;
                }
            });
            llTags.addView(row);

            View div = new View(getContext());
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
            dlp.setMargins(48, 0, 0, 0); div.setLayoutParams(dlp);
            div.setBackgroundColor(ThemeHelper.dividerColor(getContext()));
            llTags.addView(div);
        }
        if (tags.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No tags yet. Tap + Add to create one.");
            empty.setTextSize(14); empty.setTextColor(ThemeHelper.textSecondary(getContext()));
            empty.setPadding(48, 24, 48, 8);
            llTags.addView(empty);
        }
    }

    private void showAddTagDialog() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 16);
        layout.setBackgroundColor(ThemeHelper.surfaceColor(getContext()));

        final EditText etName = new EditText(getContext());
        etName.setHint("Tag name  e.g. Reading");
        etName.setHintTextColor(ThemeHelper.textHint(getContext()));
        etName.setTextColor(ThemeHelper.textPrimary(getContext()));
        etName.setBackground(null);
        layout.addView(etName);

        TextView tvColorLabel = new TextView(getContext());
        tvColorLabel.setText("Pick a color"); tvColorLabel.setTextSize(13);
        tvColorLabel.setTextColor(ThemeHelper.textSecondary(getContext()));
        tvColorLabel.setPadding(0, 20, 0, 12);
        layout.addView(tvColorLabel);

        final String[] palette = {
            "#26C6DA","#4CAF50","#FFC107","#E91E63","#9C27B0",
            "#FF5722","#3F51B5","#795548","#00BCD4","#8BC34A",
            "#FF9800","#607D8B","#F44336","#2196F3","#009688"
        };
        final int[] picked = {0};
        final View[] dots  = new View[palette.length];

        for (int r = 0; r < 3; r++) {
            LinearLayout colorRow = new LinearLayout(getContext());
            colorRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rlp.setMargins(0, 0, 0, 8); colorRow.setLayoutParams(rlp);
            for (int c = 0; c < 5; c++) {
                final int idx = r * 5 + c;
                if (idx >= palette.length) break;
                final View d = new View(getContext());
                LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(0, 44, 1f);
                dlp.setMargins(0, 0, 8, 0); d.setLayoutParams(dlp);
                dots[idx] = d;
                updateDot(d, palette[idx], idx == 0);
                d.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        updateDot(dots[picked[0]], palette[picked[0]], false);
                        picked[0] = idx;
                        updateDot(d, palette[idx], true);
                    }
                });
                colorRow.addView(d);
            }
            layout.addView(colorRow);
        }

        new AlertDialog.Builder(getContext())
            .setTitle("New Tag").setView(layout)
            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int w) {
                    final String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Enter a tag name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final String color = palette[picked[0]];
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        @Override public void run() {
                            final long id = dao.insertTag(name, color);
                            if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Toast.makeText(getContext(),
                                        id > 0 ? "Tag added ✓" : "Name already exists",
                                        Toast.LENGTH_SHORT).show();
                                    if (id > 0) loadTags();
                                }
                            });
                        }
                    });
                }
            }).setNegativeButton("Cancel", null).show();
    }

    private void updateDot(View dot, String color, boolean selected) {
        try {
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(Color.parseColor(color));
            if (selected) gd.setStroke(4, Color.WHITE);
            dot.setBackground(gd);
            dot.setScaleX(selected ? 1.3f : 1f);
            dot.setScaleY(selected ? 1.3f : 1f);
        } catch (Exception ig) {}
    }

    private void confirmDeleteTag(final Tag tag) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                android.database.Cursor c = null;
                int count = 0;
                try {
                    c = DatabaseHelper.getInstance(getContext()).getReadableDatabase().rawQuery(
                        "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_ENTRIES +
                        " WHERE " + DatabaseHelper.COL_ENTRY_TAG_ID + "=?",
                        new String[]{String.valueOf(tag.getId())});
                    if (c != null && c.moveToFirst()) count = c.getInt(0);
                } finally { if (c != null) c.close(); }
                final int n = count;
                if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (n > 0) {
                            new AlertDialog.Builder(getContext())
                                .setTitle("Cannot delete")
                                .setMessage("\"" + tag.getName() + "\" is used by " + n +
                                    " entr" + (n == 1 ? "y" : "ies") +
                                    ".\nRe-tag those entries first.")
                                .setPositiveButton("OK", null).show();
                        } else {
                            new AlertDialog.Builder(getContext())
                                .setTitle("Delete tag?")
                                .setMessage("Delete \"" + tag.getName() + "\"? Cannot be undone.")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface d, int w) {
                                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                                            @Override public void run() {
                                                DatabaseHelper.getInstance(getContext())
                                                    .getWritableDatabase()
                                                    .delete(DatabaseHelper.TABLE_TAGS,
                                                        DatabaseHelper.COL_TAG_ID + "=?",
                                                        new String[]{String.valueOf(tag.getId())});
                                                loadTags();
                                            }
                                        });
                                    }
                                }).setNegativeButton("Cancel", null).show();
                        }
                    }
                });
            }
        });
    }
}
