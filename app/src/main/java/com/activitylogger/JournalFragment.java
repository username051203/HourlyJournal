package com.activitylogger;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JournalFragment extends Fragment {

    private EntryDao        dao;
    private HourSlotAdapter adapter;
    private List<Tag>       allTags     = new ArrayList<>();
    private long            currentDayMs;
    private boolean         searchMode  = false;
    private String          searchQuery = "";

    private LinearLayout   journalRoot;
    private RelativeLayout journalHeader;
    private View           headerCircle;
    private TextView       tvTime, tvGreeting, tvStreak, tvSectionLabel;
    private Button         btnPrev2, btnPrev1, btnToday;
    private ImageButton    btnCalendar, btnSearch;
    private EditText       etSearch;
    private View           searchBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle s) {
        View root = inflater.inflate(R.layout.fragment_journal, container, false);
        dao          = new EntryDao(getContext());
        currentDayMs = DateHelper.startOfDay(System.currentTimeMillis());

        journalRoot    = (LinearLayout)   root.findViewById(R.id.journal_root);
        journalHeader  = (RelativeLayout) root.findViewById(R.id.journal_header);
        headerCircle   =                  root.findViewById(R.id.view_header_circle);
        tvTime         = (TextView)       root.findViewById(R.id.tv_current_time);
        tvGreeting     = (TextView)       root.findViewById(R.id.tv_greeting);
        tvStreak       = (TextView)       root.findViewById(R.id.tv_streak);
        tvSectionLabel = (TextView)       root.findViewById(R.id.tv_section_label);
        btnPrev2       = (Button)         root.findViewById(R.id.btn_day_prev2);
        btnPrev1       = (Button)         root.findViewById(R.id.btn_day_prev1);
        btnToday       = (Button)         root.findViewById(R.id.btn_day_today);
        btnCalendar    = (ImageButton)    root.findViewById(R.id.btn_calendar);
        btnSearch      = (ImageButton)    root.findViewById(R.id.btn_search);
        etSearch       = (EditText)       root.findViewById(R.id.et_search);
        searchBar      =                  root.findViewById(R.id.search_bar);

        applyTheme();
        updateHeader();
        setupDateButtons();
        setupSearch();
        setupMenuButton(root);

        RecyclerView rv = (RecyclerView) root.findViewById(R.id.rv_hour_slots);
        adapter = new HourSlotAdapter(getContext());
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        adapter.setOnSlotClickListener(new HourSlotAdapter.OnSlotClickListener() {
            @Override public void onTap(HourSlot slot)       { showEntryDialog(slot); }
            @Override public void onLongPress(HourSlot slot) {
                if (slot.hasEntry()) showEntryOptions(slot);
                else showEntryDialog(slot);
            }
        });

        loadTagsThenSlots();
        return root;
    }

    private void applyTheme() {
        boolean dark = ThemeHelper.isDark(getContext());
        if (journalRoot   != null) journalRoot.setBackgroundColor(ThemeHelper.bgColor(getContext()));
        if (journalHeader != null) journalHeader.setBackgroundResource(
            dark ? R.drawable.bg_night_header : R.drawable.bg_journal_header);
        if (headerCircle  != null) headerCircle.setBackgroundResource(
            dark ? R.drawable.bg_moon_circle : R.drawable.bg_sun_circle);
        int headerText = dark ? 0xFFE0E0E0 : 0xFF212121;
        if (tvTime     != null) tvTime.setTextColor(headerText);
        if (tvGreeting != null) tvGreeting.setTextColor(headerText);
        if (tvStreak   != null) tvStreak.setTextColor(dark ? 0xFF9E9E9E : 0xFF757575);
        if (tvSectionLabel != null) tvSectionLabel.setTextColor(0xFF26C6DA);
        if (etSearch   != null) {
            etSearch.setTextColor(ThemeHelper.textPrimary(getContext()));
            etSearch.setHintTextColor(ThemeHelper.textHint(getContext()));
        }
    }

    private void updateHeader() {
        if (tvTime != null) tvTime.setText(DateHelper.currentTimeLabel());
        if (tvGreeting != null) tvGreeting.setText(DateHelper.greeting() + ",\n" +
            getActivity().getSharedPreferences("prefs", 0).getString("user_name", "Friend"));
        long today = DateHelper.startOfDay(System.currentTimeMillis());
        if (btnPrev2 != null) btnPrev2.setText(DateHelper.formatNavLabel(today - 2 * 86400_000L));
        if (btnPrev1 != null) btnPrev1.setText(DateHelper.formatNavLabel(today - 86400_000L));
        if (btnToday != null) btnToday.setText("Today");
    }

    private void setupDateButtons() {
        long today       = DateHelper.startOfDay(System.currentTimeMillis());
        final long prev1 = today - 86400_000L;
        final long prev2 = today - 2 * 86400_000L;
        if (btnPrev2 != null) btnPrev2.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { currentDayMs = prev2; highlightDay(0); reloadSlots(); }
        });
        if (btnPrev1 != null) btnPrev1.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { currentDayMs = prev1; highlightDay(1); reloadSlots(); }
        });
        if (btnToday != null) btnToday.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                currentDayMs = DateHelper.startOfDay(System.currentTimeMillis());
                highlightDay(2); reloadSlots();
            }
        });
        if (btnCalendar != null) btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showDatePicker(); }
        });
        highlightDay(2);
    }

    private void setupSearch() {
        if (btnSearch == null || etSearch == null || searchBar == null) return;
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                searchMode = !searchMode;
                searchBar.setVisibility(searchMode ? View.VISIBLE : View.GONE);
                if (!searchMode) { searchQuery = ""; etSearch.setText(""); reloadSlots(); }
                else etSearch.requestFocus();
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                searchQuery = s.toString().trim(); reloadSlots();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void highlightDay(int idx) {
        View root = getView();
        if (root == null) return;
        int[] ids = {R.id.btn_day_prev2, R.id.btn_day_prev1, R.id.btn_day_today};
        for (int i = 0; i < 3; i++) {
            Button b = (Button) root.findViewById(ids[i]);
            if (b == null) continue;
            b.setBackgroundResource(i == idx ? R.drawable.bg_pill_selected : R.drawable.bg_pill_unselected);
            b.setTextColor(i == idx ? Color.WHITE : ThemeHelper.textPrimary(getContext()));
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentDayMs);
        new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override public void onDateSet(DatePicker v, int y, int m, int d) {
                Calendar p = Calendar.getInstance();
                p.set(y, m, d, 0, 0, 0); p.set(Calendar.MILLISECOND, 0);
                currentDayMs = p.getTimeInMillis(); reloadSlots();
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupMenuButton(View root) {
        View btn = root.findViewById(R.id.btn_menu);
        if (btn != null) btn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).openDrawer();
            }
        });
    }

    private void loadTagsThenSlots() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                allTags = dao.getAllTags();
                reloadSlots();
            }
        });
    }

    private void reloadSlots() {
        if (searchMode && !searchQuery.isEmpty()) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                @Override public void run() {
                    final List<Entry> results = dao.searchByContent(searchQuery);
                    final List<HourSlot> slots = new ArrayList<>();
                    for (Entry e : results) {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(e.getTimestamp());
                        c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0);
                        slots.add(new HourSlot(DateHelper.formatEntryTime(e.getTimestamp()),
                            c.getTimeInMillis(), e));
                    }
                    if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() { adapter.setSlots(slots); }
                    });
                }
            });
            return;
        }
        final long dayMs = currentDayMs;
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                final List<Entry>    entries = dao.getEntriesForDate(
                    dayMs, dayMs + 86400_000L - 1);
                final List<HourSlot> slots   = DateHelper.buildHourSlots(dayMs, entries);
                if (getActivity() != null) getActivity().runOnUiThread(new Runnable() {
                    @Override public void run() { adapter.setSlots(slots); }
                });
            }
        });
    }

    private void showEntryDialog(final HourSlot slot) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 8);
        layout.setBackgroundColor(ThemeHelper.surfaceColor(getContext()));

        LinearLayout moodRow = new LinearLayout(getContext());
        moodRow.setOrientation(LinearLayout.HORIZONTAL);
        moodRow.setPadding(0, 0, 0, 16);
        String[] moods = {"😴","😐","😊","🔥"};
        final String[] selMood = {slot.hasEntry() && slot.getEntry().getMood() != null
            ? slot.getEntry().getMood() : ""};
        final TextView[] moodBtns = new TextView[moods.length];
        for (int i = 0; i < moods.length; i++) {
            final int idx = i;
            final TextView m = new TextView(getContext());
            m.setText(moods[i]); m.setTextSize(26);
            m.setGravity(android.view.Gravity.CENTER); m.setPadding(0, 8, 0, 8);
            float sc = moods[i].equals(selMood[0]) ? 1.4f : 1f;
            m.setScaleX(sc); m.setScaleY(sc);
            m.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            m.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    selMood[0] = moods[idx];
                    for (TextView mb : moodBtns) { mb.setScaleX(1f); mb.setScaleY(1f); }
                    m.setScaleX(1.4f); m.setScaleY(1.4f);
                }
            });
            moodBtns[i] = m; moodRow.addView(m);
        }
        layout.addView(moodRow);

        final EditText et = new EditText(getContext());
        et.setHint("What did you do this hour?");
        et.setHintTextColor(ThemeHelper.textHint(getContext()));
        et.setTextColor(ThemeHelper.textPrimary(getContext()));
        et.setBackgroundColor(ThemeHelper.surface2Color(getContext()));
        et.setPadding(20, 16, 20, 16); et.setMinLines(3);
        et.setGravity(android.view.Gravity.TOP);
        if (slot.hasEntry()) et.setText(slot.getEntry().getContent());
        layout.addView(et);

        TextView tvTagLabel = new TextView(getContext());
        tvTagLabel.setText("Tag"); tvTagLabel.setTextSize(12);
        tvTagLabel.setTextColor(ThemeHelper.textSecondary(getContext()));
        tvTagLabel.setPadding(0, 16, 0, 4);
        layout.addView(tvTagLabel);

        final Spinner spinner = new Spinner(getContext());
        List<String> tagNames = new ArrayList<>();
        for (Tag t : allTags) tagNames.add(t.getName());
        spinner.setAdapter(new ArrayAdapter<>(getContext(),
            android.R.layout.simple_spinner_dropdown_item, tagNames));
        if (slot.hasEntry() && slot.getEntry().getTagId() > 0)
            for (int i = 0; i < allTags.size(); i++)
                if (allTags.get(i).getId() == slot.getEntry().getTagId()) { spinner.setSelection(i); break; }
        layout.addView(spinner);

        AlertDialog.Builder b = new AlertDialog.Builder(getContext())
            .setTitle(slot.getLabel()).setView(layout)
            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int w) {
                    String content = et.getText().toString().trim();
                    if (content.isEmpty()) return;
                    long tagId = allTags.isEmpty() ? 0
                        : allTags.get(spinner.getSelectedItemPosition()).getId();
                    if (slot.hasEntry()) dao.deleteEntry(slot.getEntry().getId());
                    if (slot.getSlotStart() > System.currentTimeMillis()) {
                        dao.insertPlanned(content, tagId, selMood[0], slot.getSlotStart());
                    } else {
                        dao.insertEntryAt(content, tagId, selMood[0], slot.getSlotStart());
                    }
                    reloadSlots();
                }
            }).setNegativeButton("Cancel", null);
        if (slot.hasEntry())
            b.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int w) {
                    dao.deleteEntry(slot.getEntry().getId()); reloadSlots();
                }
            });
        b.show();
    }

    private void showEntryOptions(final HourSlot slot) {
        String star = slot.getEntry().isStarred() ? "Unstar" : "Star";
        new AlertDialog.Builder(getContext())
            .setItems(new String[]{"Edit", star, "Delete"},
                new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        if (which == 0) showEntryDialog(slot);
                        else if (which == 1) {
                            dao.toggleStar(slot.getEntry().getId(), !slot.getEntry().isStarred());
                            reloadSlots();
                        } else { dao.deleteEntry(slot.getEntry().getId()); reloadSlots(); }
                    }
                }).show();
    }
}
