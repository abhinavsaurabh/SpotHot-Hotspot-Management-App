package com.team10.mc.SpotHOT.receiver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;


import com.team10.mc.SpotHOT.service.ServiceHelper;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static com.team10.mc.SpotHOT.TetherIntents.CHANGE_NETWORK_STATE;


public class TetheringStateReceiver extends BroadcastReceiver {

    private final String TAG = "TetheringStateChange";

    @Override
    public void onReceive(Context context, Intent intent) {
        ServiceHelper helper = new ServiceHelper(context);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

    }



    private int getLayout(Intent intent) {

        switch (getState(intent)) {
            case WIFI_STATE_ENABLED:

                break;
            case WIFI_STATE_DISABLED:

                break;
        }

        return 0;
    }

    private int getState(Intent intent) {
        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
        return state % 10;
    }
}
