package com.team10.mc.SpotHOT.activity.helpers;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.Preference;
import com.team10.mc.SpotHOT.R;
import com.team10.mc.SpotHOT.TetherIntents;
import com.team10.mc.SpotHOT.Utils;
import com.team10.mc.SpotHOT.activity.MainActivity;
import com.team10.mc.SpotHOT.service.ServiceHelper;



public class
RegisterDataLimitListenerHelper extends AbstractRegisterHelper {

    public RegisterDataLimitListenerHelper(MainActivity activity) {
        super(activity);
    }

    @Override
    public void registerUIListeners() {
        getPreferenceScreen("data.limit.reset").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.warning)
                        .setMessage("Do you want to reset data usage counter?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Utils.resetDataUsageStat(prefs, -ServiceHelper.getDataUsage(), 0);
                                Intent intent = new Intent(TetherIntents.DATA_USAGE);
                                activity.sendBroadcast(intent);
                            }
                        })
                        .setNegativeButton(R.string.no, null
                        ).show();

                return true;
            }
        });

        getEditTextPreference("data.limit.value").setOnPreferenceChangeListener(changeListener);
    }

}
