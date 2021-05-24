package com.team10.mc.SpotHOT.activity.helpers;

import android.preference.CheckBoxPreference;
import android.preference.Preference;

import com.team10.mc.SpotHOT.activity.MainActivity;


public class RegisterWiFiBlockListenerHelper extends AbstractRegisterHelper {

    protected RegisterWiFiBlockListenerHelper(MainActivity activity) {
        super(activity);
    }

    @Override
    public void registerUIListeners() {
        CheckBoxPreference block = getCheckBoxPreference("wifi.connected.block.tethering");
        block.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                return false;
            }
        });
    }
}
