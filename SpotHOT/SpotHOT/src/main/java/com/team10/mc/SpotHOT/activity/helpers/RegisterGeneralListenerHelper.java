package com.team10.mc.SpotHOT.activity.helpers;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;


import com.team10.mc.SpotHOT.R;
import com.team10.mc.SpotHOT.TetherIntents;
import com.team10.mc.SpotHOT.activity.MainActivity;
import com.team10.mc.SpotHOT.receiver.BootCompletedReceiver;
import com.team10.mc.SpotHOT.service.ServiceHelper;
import com.team10.mc.SpotHOT.service.TetheringService;

import static com.team10.mc.SpotHOT.AppProperties.ACTIVATE_KEEP_SERVICE;
import static com.team10.mc.SpotHOT.AppProperties.RETURN_TO_PREV_STATE;
import static com.team10.mc.SpotHOT.AppProperties.SSID;
import static com.team10.mc.SpotHOT.activity.MainActivity.ON_CHANGE_SSID;


public class RegisterGeneralListenerHelper extends AbstractRegisterHelper {
    private final ServiceHelper serviceHelper;

    public RegisterGeneralListenerHelper(MainActivity activity) {
        super(activity);
        this.serviceHelper = new ServiceHelper(activity);
    }

    @Override
    public void registerUIListeners() {
        Preference.OnPreferenceChangeListener revertStateCheckBoxListener = new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue) {

                    Toast toast = Toast.makeText(activity, "Once application has been closed tethering and internet connection state will be restored to state before open this application", Toast.LENGTH_LONG);
                    TextView v = toast.getView().findViewById(android.R.id.message);
                    if (v != null) {
                        v.setGravity(Gravity.CENTER);
                        v.setPadding(12, 12, 12, 12);
                    }
                    toast.show();
                }
                return true;
            }
        };

        PreferenceScreen editSSID = getPreferenceScreen(SSID);
        editSSID.setOnPreferenceChangeListener(changeListener);

        CheckBoxPreference revertStateCheckBox = getCheckBoxPreference(RETURN_TO_PREV_STATE);
       // revertStateCheckBox.setOnPreferenceChangeListener(revertStateCheckBoxListener);

        final CheckBoxPreference activationStartup = getCheckBoxPreference("activate.on.startup");
        final ComponentName componentName = new ComponentName(activity, BootCompletedReceiver.class);
        int state = activity.getPackageManager().getComponentEnabledSetting(componentName);

        activationStartup.setChecked(state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

        activationStartup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int state = activity.getPackageManager().getComponentEnabledSetting(componentName);

                if (state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED && state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
                    activity.getPackageManager().setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                    Toast.makeText(activity, R.string.on_startup_enable, Toast.LENGTH_LONG).show();
                } else {
                    activity.getPackageManager().setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    Toast.makeText(activity, R.string.on_startup_disable, Toast.LENGTH_LONG).show();
                }

                return true;
            }
        });

        CheckBoxPreference keepServiceCheckBox = getCheckBoxPreference(ACTIVATE_KEEP_SERVICE);
        keepServiceCheckBox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue) {
                    startService();
                }
                return true;
            }
        });

       // CheckBoxPreference roamingCheckBox = getCheckBoxPreference("activate.on.roaming");


        EditTextPreference batteryLevelValue = getEditTextPreference("usb.off.battery.lvl.value");
        batteryLevelValue.setOnPreferenceChangeListener(changeListener);
        batteryLevelValue.getEditText().setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});

        Preference p = activity.findPreference(SSID);
        p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    activity.startActivityForResult(preference.getIntent(), ON_CHANGE_SSID);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(activity, R.string.cannot_load_wifihotspot, Toast.LENGTH_LONG).show();
                    //MyLog.e("Hotspot", ex);
                }
                return true;
            }
        });

        EditTextPreference delay = getEditTextPreference("activate.on.startup.delay");
        delay.setOnPreferenceChangeListener(changeListener);

        final CheckBoxPreference tetheringOn = getCheckBoxPreference("activate.tethering");
        tetheringOn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                activity.sendBroadcast(new Intent(tetheringOn.isChecked() ? TetherIntents.TETHER_ON : TetherIntents.TETHER_OFF));
                return false;
            }
        });



        final CheckBoxPreference startService = getCheckBoxPreference("usb.internet.start.service");






    }

    private void startService() {
        if (!serviceHelper.isServiceRunning(TetheringService.class)) {
            Intent serviceIntent = new Intent(activity, TetheringService.class);
            serviceIntent.putExtra("runFromActivity", true);
            activity.startService(serviceIntent);
        }
    }
}
