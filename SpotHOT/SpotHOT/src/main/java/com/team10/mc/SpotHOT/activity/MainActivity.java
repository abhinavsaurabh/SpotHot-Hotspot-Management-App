package com.team10.mc.SpotHOT.activity;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.team10.mc.SpotHOT.BuildConfig;
import com.team10.mc.SpotHOT.ListenerManager;
import com.team10.mc.SpotHOT.R;
import com.team10.mc.SpotHOT.TetherIntents;
import com.team10.mc.SpotHOT.Utils;
//import com.labs.dm.auto_tethering.activity.helpers.RegisterAddSimCardListenerHelper;
import com.team10.mc.SpotHOT.activity.helpers.RegisterSchedulerListenerHelper;
import com.team10.mc.SpotHOT.db.DBManager;
import com.team10.mc.SpotHOT.receiver.BootCompletedReceiver;
import com.team10.mc.SpotHOT.service.ServiceHelper;
import com.team10.mc.SpotHOT.service.TetheringService;

import java.text.Format;
import java.util.Date;
import java.util.Map;

import de.cketti.library.changelog.ChangeLog;

import static android.os.Process.killProcess;
import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_3G;
import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_KEEP_SERVICE;
import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_ON_STARTUP;
import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_TETHERING;
import static com.team10.mc.SpotHOT.AppProperties.IDLE_TETHERING_OFF_TIME;
import static com.team10.mc.SpotHOT.AppProperties.LATEST_VERSION;
import static com.team10.mc.SpotHOT.AppProperties.SSID;
import static com.team10.mc.SpotHOT.TetherIntents.CHANGE_CELL_FORM;
import static com.team10.mc.SpotHOT.TetherIntents.SERVICE_ON;
import static com.team10.mc.SpotHOT.TetherIntents.WIFI_DEFAULT_REFRESH;


public class MainActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int ON_CHANGE_SSID = 1, ON_CHANGE_SCHEDULE = 2;
    private static final int NOTIFICATION_ID = 1234;
    private SharedPreferences prefs;
    private ServiceHelper serviceHelper;
    private BroadcastReceiver receiver;
    private DBManager db;
    private ListenerManager listenerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DBManager.getInstance(getApplicationContext());
        addPreferencesFromResource(R.xml.preferences);
        serviceHelper = new ServiceHelper(getApplicationContext());
        loadPrefs();
        checkIfNotlocked();
        registerListeners();
        registerReceivers();
        onStartup();
    }





    private void registerReceivers() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (TetherIntents.EXIT.equals(intent.getAction())) {
                    exitApp();
                } else if (TetherIntents.CLIENTS.equals(intent.getAction())) {
                    final PreferenceScreen connectedClients = (PreferenceScreen) findPreference("idle.connected.clients");
                    connectedClients.setTitle("Connected clients: " + intent.getIntExtra("value", 0));
                } else if (TetherIntents.DATA_USAGE.equals(intent.getAction())) {
                    final PreferenceScreen dataUsage = (PreferenceScreen) findPreference("data.limit.counter");
                    Format dateFormat = DateFormat.getDateFormat(getApplicationContext());
                    Format timeFormat = DateFormat.getTimeFormat(getApplicationContext());
                    Date date = new Date(prefs.getLong("data.usage.removeAllData.timestamp", 0));
                    dataUsage.setSummary(String.format("%s from %s %s", Utils.humanReadableByteCount(intent.getLongExtra("value", 0)), dateFormat.format(date), timeFormat.format(date)));
                } else if (TetherIntents.UNLOCK.equals(intent.getAction())) {
                    NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nMgr.cancel(NOTIFICATION_ID);

                    PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference("source.activation");
                    final ListAdapter listAdapter = preferenceScreen.getRootAdapter();
                    PreferenceScreen category = (PreferenceScreen) findPreference("data.limit");

                    final int itemsCount = listAdapter.getCount();
                    int itemNumber;
                    for (itemNumber = 0; itemNumber < itemsCount; ++itemNumber) {
                        if (listAdapter.getItem(itemNumber).equals(category)) {
                            preferenceScreen.onItemClick(null, null, itemNumber, 0);
                            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                            context.sendBroadcast(it);
                            Toast.makeText(MainActivity.this, "Please uncheck the property 'Data usage limit on' to unlock!", Toast.LENGTH_LONG).show();
                            break;
                        }
                    }
                }  else if (WIFI_DEFAULT_REFRESH.equals(intent.getAction())) {
                    Preference p = findPreference(SSID);
                    p.setSummary(prefs.getString("default.wifi.network", serviceHelper.getTetheringSSID()));
                    if (serviceHelper.isTetheringWiFi()) {
                        serviceHelper.setWifiTethering(false, null);
                        serviceHelper.setWifiTethering(true, Utils.getDefaultWifiConfiguration(getApplicationContext(), prefs));
                    } else {
                        serviceHelper.setWifiTethering(true, Utils.getDefaultWifiConfiguration(getApplicationContext(), prefs));
                        serviceHelper.setWifiTethering(false, null);
                    }
                    Toast.makeText(getApplicationContext(), "Default WiFi Network has been changed to " + p.getSummary(), Toast.LENGTH_LONG).show();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(TetherIntents.EXIT);
        filter.addAction(TetherIntents.CLIENTS);
        filter.addAction(TetherIntents.DATA_USAGE);
        filter.addAction(TetherIntents.UNLOCK);
        filter.addAction(TetherIntents.WIFI_DEFAULT_REFRESH);
        filter.addAction(CHANGE_CELL_FORM);
        registerReceiver(receiver, filter);
    }

    private void registerListeners() {
        listenerManager = new ListenerManager(this);
        listenerManager.registerAll();

        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            Preference p = findPreference(entry.getKey());

            switch (entry.getKey()) {
                case IDLE_TETHERING_OFF_TIME:
                case "usb.off.battery.lvl.value":
                case "data.limit.value":
                case "activate.on.startup.delay":
                    p.setSummary((CharSequence) entry.getValue());
                    p.getEditor().apply();
                    break;
                case "temp.value.stop":
                case "temp.value.start":
                    if ("temp.value.start".equals(p.getKey())) {
                        p.setSummary("When temp. returns to: " + entry.getValue() + " °C");
                    } else if ("temp.value.stop".equals(p.getKey())) {
                        p.setSummary("When temp. higher than: " + entry.getValue() + " °C");
                    }
                    p.getEditor().apply();
                    break;
                case SSID:
                    p.setSummary(serviceHelper.getTetheringSSID());
                    p.getEditor().apply();
                    break;
            }
        }
    }


    private void checkIfNotlocked() {
        final ComponentName componentName = new ComponentName(this, BootCompletedReceiver.class);
        int state = getPackageManager().getComponentEnabledSetting(componentName);

        if (state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED && state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && !prefs.getBoolean("autostart.blocked.donotremind", false)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.warning)
                    .setMessage("Startup application on system boot is currently blocked and therefore service cannot run properly.\n\nDo you want to enable this setting?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final CheckBoxPreference activationStartup = (CheckBoxPreference) findPreference("activate.on.startup");
                            activationStartup.setChecked(true);
                            getPackageManager().setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                            Toast.makeText(getApplicationContext(), R.string.on_startup_enable, Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNeutralButton(R.string.donot_remind, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefs.edit().putBoolean("autostart.blocked.donotremind", true).apply();
                        }
                    })
                    .setNegativeButton(R.string.no, null
                    ).show();
        }
    }



    private void prepareScheduleList() {
        listenerManager.getHelper(RegisterSchedulerListenerHelper.class).prepare();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_v10_main, menu);
        }
        return true;
    }



    private void loadPrefs() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == ON_CHANGE_SSID) {
            if (resCode == android.app.Activity.RESULT_OK) {
                Preference p = findPreference(SSID);
                p.setSummary(serviceHelper.getTetheringSSID());
            }
        }
        if (reqCode == ON_CHANGE_SCHEDULE) {
            if (resCode == android.app.Activity.RESULT_OK) {
                prepareScheduleList();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        startService();
        prefs.edit().putString(SSID, serviceHelper.getTetheringSSID()).apply();
        loadPrefs();
        prepareScheduleList();
    }

    private void startService() {


        if (!serviceHelper.isServiceRunning(TetheringService.class)) {
            Intent serviceIntent = new Intent(this, TetheringService.class);
            serviceIntent.putExtra("runFromActivity", true);
            startService(serviceIntent);
        }
    }

    private void onStartup() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int version = Integer.parseInt(prefs.getString(LATEST_VERSION, "0"));

                if (version == 0) {
                    /** First start after installation **/
                    prefs.edit().putBoolean(ACTIVATE_3G, false).apply();
                    prefs.edit().putBoolean(ACTIVATE_TETHERING, false).apply();

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.warning)
                                .setMessage(getString(R.string.initial_prompt))
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        prefs.edit().putBoolean(ACTIVATE_3G, true).apply();
                                        prefs.edit().putBoolean(ACTIVATE_TETHERING, true).apply();
                                    }
                                })
                                .setNegativeButton(R.string.no, null)
                                .show();
                    } else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.warning)
                                .setMessage(getString(R.string.initial_prompt_lollipop))
                                .setPositiveButton(R.string.close, null)
                                .show();
                    }
                    prefs.edit().putString(LATEST_VERSION, String.valueOf(BuildConfig.VERSION_CODE)).apply();
                }

                if (version < BuildConfig.VERSION_CODE) {
                    /** First start after update **/
                    try {
                        ChangeLog cl = new ChangeLog(MainActivity.this);
                        if (cl.isFirstRun()) {
                            cl.getLogDialog().show();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(MainActivity.this, "Failed to load changelog", Toast.LENGTH_LONG).show();
                        //MyLog.e("ChangeLog", ex);
                    }
                    prefs.edit().putString(LATEST_VERSION, String.valueOf(BuildConfig.VERSION_CODE)).apply();
                } else if (version == BuildConfig.VERSION_CODE) {
                    /** Another execution **/
                }
            }
        });

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        if (preference instanceof PreferenceScreen) {
            initializeActionBar((PreferenceScreen) preference);
        }

        return false;
    }

    private void initializeActionBar(PreferenceScreen preferenceScreen) {
        final Dialog dialog = preferenceScreen.getDialog();

        if (dialog != null) {
            View homeBtn = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && dialog.getActionBar() != null) {
                dialog.getActionBar().setDisplayHomeAsUpEnabled(true);
                homeBtn = dialog.findViewById(android.R.id.home);
            }

            if (homeBtn != null) {
                View.OnClickListener dismissDialogClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                };

                ViewParent homeBtnContainer = homeBtn.getParent();

                if (homeBtnContainer instanceof FrameLayout) {
                    ViewGroup containerParent = (ViewGroup) homeBtnContainer.getParent();

                    if (containerParent instanceof LinearLayout) {
                        containerParent.setOnClickListener(dismissDialogClickListener);
                    } else {
                        ((FrameLayout) homeBtnContainer).setOnClickListener(dismissDialogClickListener);
                    }
                } else {
                    homeBtn.setOnClickListener(dismissDialogClickListener);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                startActivity(new Intent(this, AboutActivity.class));
                return false;
            case R.id.action_reset:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.warning)
                        .setMessage(getString(R.string.reset_prompt))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs.edit().clear().apply();
                                prefs.edit().putString(LATEST_VERSION, String.valueOf(BuildConfig.VERSION_CODE)).apply();
                                db.removeAllData();
                                prepareScheduleList();
                                restartApp();
                            }
                        })
                        .setNegativeButton(R.string.no, null).show();
                return true;
            case R.id.action_exit:
                if (prefs.getBoolean(ACTIVATE_KEEP_SERVICE, true)) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.warning)
                            .setMessage(R.string.prompt_onexit)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    exitApp();
                                }
                            })
                            .setNegativeButton(R.string.no, null).show();
                } else {
                    exitApp();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void restartApp() {
        try {
            Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), 123456, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, mPendingIntent);
        } finally {
            finish();
        }
    }

    private void exitApp() {
        try {
            Intent serviceIntent = new Intent(this, TetheringService.class);
            stopService(serviceIntent);
            moveTaskToBack(true);
            killProcess(android.os.Process.myPid());
            System.exit(1);
        } finally {
            if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 21) {
                finishAffinity();
            } else if (Build.VERSION.SDK_INT >= 21) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case ACTIVATE_3G:
                sendBroadcast(new Intent(SERVICE_ON));
                break;
            case ACTIVATE_TETHERING:
                sendBroadcast(new Intent(SERVICE_ON));
                break;
            case ACTIVATE_ON_STARTUP: {
                ((CheckBoxPreference) findPreference(key)).setChecked(sharedPreferences.getBoolean(key, false));
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(receiver);
            listenerManager.unregisterAll();
            db.close();
        } finally {
            super.onDestroy();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 12345: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        restartApp();
                    } else {
                        exitApp();
                    }
                }
            }
        }
    }
}