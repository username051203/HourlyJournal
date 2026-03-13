package com.activitylogger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class JournalSettingsFragment extends Fragment {

    private SharedPreferences prefs;

    private Spinner spWakeTime;
    private Spinner spSleepTime;
    private Spinner spReminderInterval;

    private static final String[] HOURS = {
        "12:00 AM","1:00 AM","2:00 AM","3:00 AM","4:00 AM","5:00 AM",
        "6:00 AM","7:00 AM","8:00 AM","9:00 AM","10:00 AM","11:00 AM",
        "12:00 PM","1:00 PM","2:00 PM","3:00 PM","4:00 PM","5:00 PM",
        "6:00 PM","7:00 PM","8:00 PM","9:00 PM","10:00 PM","11:00 PM"
    };

    private static final String[] INTERVALS = {
        "Every 30 minutes","Every 1 hour","Every 2 hours",
        "Every 3 hours","Every 4 hours","Off"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_journal_settings, container, false);
        prefs = getActivity().getSharedPreferences("prefs", 0);

        spWakeTime         = (Spinner) root.findViewById(R.id.sp_wake_time);
        spSleepTime        = (Spinner) root.findViewById(R.id.sp_sleep_time);
        spReminderInterval = (Spinner) root.findViewById(R.id.sp_reminder_interval);

        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item, HOURS);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item, INTERVALS);
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spWakeTime.setAdapter(hourAdapter);
        spSleepTime.setAdapter(new ArrayAdapter<>(
            getContext(), android.R.layout.simple_spinner_item, HOURS));
        spReminderInterval.setAdapter(intervalAdapter);

        // Restore saved values
        spWakeTime.setSelection(prefs.getInt("wake_hour", 9));
        spSleepTime.setSelection(prefs.getInt("sleep_hour", 2));
        spReminderInterval.setSelection(prefs.getInt("reminder_interval", 1));

        Button btnSave = (Button) root.findViewById(R.id.btn_save_settings);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                prefs.edit()
                    .putInt("wake_hour",          spWakeTime.getSelectedItemPosition())
                    .putInt("sleep_hour",          spSleepTime.getSelectedItemPosition())
                    .putInt("reminder_interval",   spReminderInterval.getSelectedItemPosition())
                    .apply();
                Toast.makeText(getContext(), "Settings saved!", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Back button
        root.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return root;
    }
}
