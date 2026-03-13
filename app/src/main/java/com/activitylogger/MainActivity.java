package com.activitylogger;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_STORAGE = 101;

    private ImageView    iconJournal, iconAnalytics, iconSettings;
    private TextView     tvNavJournal, tvNavAnalytics, tvNavSettings;
    private LinearLayout tabJournal, tabAnalytics, tabSettings;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashLogger.init(this);
        ThemeHelper.apply(this);
        setContentView(R.layout.activity_main);

        iconJournal   = (ImageView)   findViewById(R.id.icon_journal);
        iconAnalytics = (ImageView)   findViewById(R.id.icon_analytics);
        iconSettings  = (ImageView)   findViewById(R.id.icon_settings);
        tvNavJournal  = (TextView)    findViewById(R.id.tv_nav_journal);
        tvNavAnalytics= (TextView)    findViewById(R.id.tv_nav_analytics);
        tvNavSettings = (TextView)    findViewById(R.id.tv_nav_settings);
        tabJournal    = (LinearLayout)findViewById(R.id.tab_journal);
        tabAnalytics  = (LinearLayout)findViewById(R.id.tab_analytics);
        tabSettings   = (LinearLayout)findViewById(R.id.tab_settings);
        drawerLayout  = (DrawerLayout)findViewById(R.id.drawer_layout);

        View bottomNav = findViewById(R.id.bottom_nav);
        if (bottomNav != null) bottomNav.setBackgroundColor(ThemeHelper.navBg(this));

        View drawerContent = findViewById(R.id.nav_drawer);
        if (drawerContent != null) {
            drawerContent.setBackgroundColor(ThemeHelper.surfaceColor(this));
            applyDrawerColors();
        }

        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        TextView tvUser = (TextView) findViewById(R.id.tv_drawer_username);
        if (tvUser != null) tvUser.setText("Hello, " + prefs.getString("user_name", "Friend"));

        if (!prefs.getBoolean("reminder_scheduled", false)) {
            prefs.edit().putBoolean("reminder_scheduled", true).apply();
            ReminderReceiver.schedule(this, 60);
        }

        tabJournal.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { loadFragment(new JournalFragment(), 0); }
        });
        tabAnalytics.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { loadFragment(new AnalyticsFragment(), 1); }
        });
        tabSettings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { loadFragment(new SettingsFragment(), 2); }
        });

        View ds = findViewById(R.id.drawer_study_planner);
        if (ds != null) ds.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                drawerLayout.closeDrawers();
                new StudyPlannerFragment().show(getSupportFragmentManager(), "study");
            }
        });
        View dstar = findViewById(R.id.drawer_starred);
        if (dstar != null) dstar.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { drawerLayout.closeDrawers(); showStarred(); }
        });
        View dexp = findViewById(R.id.drawer_export);
        if (dexp != null) dexp.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { drawerLayout.closeDrawers(); requestExport(); }
        });
        View dlogs = findViewById(R.id.drawer_logs);
        if (dlogs != null) dlogs.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { drawerLayout.closeDrawers(); showCrashLog(); }
        });

        if (savedInstanceState == null) loadFragment(new JournalFragment(), 0);
    }

    private void applyDrawerColors() {
        int text = ThemeHelper.textPrimary(this);
        int hint = ThemeHelper.textSecondary(this);
        int div  = ThemeHelper.dividerColor(this);
        setTv(R.id.drawer_study_planner, text); setTv(R.id.drawer_starred, text);
        setTv(R.id.drawer_export, text);        setTv(R.id.drawer_logs, hint);
        setDiv(R.id.drawer_div1, div); setDiv(R.id.drawer_div2, div);
        setDiv(R.id.drawer_div3, div); setDiv(R.id.drawer_div4, div);
    }

    private void setTv(int id, int color) {
        View v = findViewById(id);
        if (v instanceof TextView) ((TextView)v).setTextColor(color);
    }
    private void setDiv(int id, int color) {
        View v = findViewById(id); if (v != null) v.setBackgroundColor(color);
    }

    private void loadFragment(Fragment fragment, int index) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.view_pager, fragment).commit();
        updateNav(index);
    }

    private void updateNav(int index) {
        int teal = 0xFF26C6DA;
        int grey = ThemeHelper.isDark(this) ? 0xFF757575 : 0xFF9E9E9E;
        iconJournal.setColorFilter(index == 0 ? teal : grey);
        iconAnalytics.setColorFilter(index == 1 ? teal : grey);
        iconSettings.setColorFilter(index == 2 ? teal : grey);
        tvNavJournal.setTextColor(index == 0 ? teal : grey);
        tvNavAnalytics.setTextColor(index == 1 ? teal : grey);
        tvNavSettings.setTextColor(index == 2 ? teal : grey);
    }

    public void openDrawer() {
        if (drawerLayout != null) drawerLayout.openDrawer(android.view.Gravity.START);
    }

    private void requestExport() {
        if (Build.VERSION.SDK_INT >= 29) {
            doExport();
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            doExport();
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] results) {
        if (req == REQ_STORAGE && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) doExport();
        else Toast.makeText(this, "Storage permission denied", Toast.LENGTH_LONG).show();
    }

    private void doExport() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                try {
                    List<Entry> all = new EntryDao(MainActivity.this).getAllEntries();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat tf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    StringBuilder sb = new StringBuilder("Date,Time,Tag,Mood,Starred,Content\n");
                    for (Entry e : all) {
                        Date dt = new Date(e.getTimestamp());
                        sb.append(csv(df.format(dt))).append(",")
                          .append(csv(tf.format(dt))).append(",")
                          .append(csv(e.getTagName())).append(",")
                          .append(csv(e.getMood())).append(",")
                          .append(e.isStarred() ? "yes" : "no").append(",")
                          .append(csv(e.getContent())).append("\n");
                    }
                    File downloads = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
                    if (!downloads.exists()) downloads.mkdirs();
                    String fname = "journal_" + new SimpleDateFormat("yyyyMMdd_HHmm",
                        Locale.getDefault()).format(new Date()) + ".csv";
                    File f = new File(downloads, fname);
                    FileWriter fw = new FileWriter(f); fw.write(sb.toString()); fw.close();
                    final String path = f.getAbsolutePath();
                    final int count = all.size();
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Export complete")
                                .setMessage(count + " entries saved to:\nDownloads/" +
                                    new File(path).getName())
                                .setPositiveButton("OK", null)
                                .setNegativeButton("Share", new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface d, int w) {
                                        Intent i = new Intent(Intent.ACTION_SEND);
                                        i.setType("text/csv");
                                        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
                                        startActivity(Intent.createChooser(i, "Share CSV"));
                                    }
                                }).show();
                        }
                    });
                } catch (final Exception ex) {
                    CrashLogger.log(MainActivity.this, "doExport", ex);
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(MainActivity.this,
                                "Export failed: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private String csv(String s) {
        if (s == null) return "";
        return "\"" + s.replace("\"", "\"\"").replace("\n", " ") + "\"";
    }

    private void showStarred() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                final List<Entry> starred = new EntryDao(MainActivity.this).getStarredEntries();
                runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (starred.isEmpty()) {
                            Toast.makeText(MainActivity.this,
                                "No starred entries yet.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                        StringBuilder sb = new StringBuilder();
                        for (Entry e : starred) {
                            sb.append(sdf.format(new Date(e.getTimestamp())));
                            if (e.getMood() != null && !e.getMood().isEmpty())
                                sb.append("  ").append(e.getMood());
                            sb.append("\n").append(e.getContent()).append("\n\n");
                        }
                        EditText et = new EditText(MainActivity.this);
                        et.setText(sb.toString().trim()); et.setTextSize(13f);
                        et.setTextColor(ThemeHelper.textPrimary(MainActivity.this));
                        et.setBackgroundColor(ThemeHelper.surfaceColor(MainActivity.this));
                        et.setFocusable(false); et.setTextIsSelectable(true);
                        et.setPadding(48, 32, 48, 32);
                        ScrollView sv = new ScrollView(MainActivity.this); sv.addView(et);
                        new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Starred (" + starred.size() + ")")
                            .setView(sv).setPositiveButton("Close", null).show();
                    }
                });
            }
        });
    }

    private void showCrashLog() {
        EditText et = new EditText(this);
        et.setText(CrashLogger.readLog(this)); et.setTextSize(11f);
        et.setTextColor(ThemeHelper.textPrimary(this));
        et.setBackgroundColor(ThemeHelper.surface2Color(this));
        et.setFocusable(false); et.setTextIsSelectable(true); et.setPadding(32, 24, 32, 24);
        ScrollView sv = new ScrollView(this); sv.addView(et);
        new AlertDialog.Builder(this).setTitle("Crash Log").setView(sv)
            .setPositiveButton("Close", null)
            .setNegativeButton("Clear", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int w) {
                    CrashLogger.clearLog(MainActivity.this);
                    Toast.makeText(MainActivity.this, "Cleared", Toast.LENGTH_SHORT).show();
                }
            }).show();
    }
}
