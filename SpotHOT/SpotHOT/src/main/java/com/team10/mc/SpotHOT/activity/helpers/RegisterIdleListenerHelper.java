package com.team10.mc.SpotHOT.activity.helpers;

import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.team10.mc.SpotHOT.Utils;
import com.team10.mc.SpotHOT.activity.MainActivity;

import static com.team10.mc.SpotHOT.AppProperties.IDLE_3G_OFF_TIME;
import static com.team10.mc.SpotHOT.AppProperties.IDLE_TETHERING_OFF_TIME;


public class RegisterIdleListenerHelper extends AbstractRegisterHelper {

    public RegisterIdleListenerHelper(MainActivity activity) {
        super(activity);
    }

    @Override
    public void registerUIListeners() {
        getEditTextPreference(IDLE_TETHERING_OFF_TIME).setOnPreferenceChangeListener(changeListener);

        final PreferenceScreen connectedClients = getPreferenceScreen("idle.connected.clients");

        connectedClients.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                connectedClients.setTitle("Connected clients: " + Utils.connectedClients());
                return true;
            }
        });

    }
}
