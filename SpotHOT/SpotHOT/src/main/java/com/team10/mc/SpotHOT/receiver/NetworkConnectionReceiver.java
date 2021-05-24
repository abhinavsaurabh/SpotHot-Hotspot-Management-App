package com.team10.mc.SpotHOT.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.team10.mc.SpotHOT.TetherIntents;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;


public class NetworkConnectionReceiver extends BroadcastReceiver {

    private final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentName = null;

        if (CONNECTIVITY_ACTION.equals(intent.getAction())) {

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                boolean connected = activeNetwork.isConnected();
                if (activeNetwork.getType() == TYPE_WIFI) {
                    intentName = connected ? TetherIntents.EVENT_WIFI_ON : TetherIntents.EVENT_WIFI_OFF;
                } else if (activeNetwork.getType() == TYPE_MOBILE) {
                    intentName = connected ? TetherIntents.EVENT_MOBILE_ON : TetherIntents.EVENT_MOBILE_OFF;
                }
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == TYPE_WIFI) {
                        intentName = TetherIntents.EVENT_WIFI_OFF;
                    } else if (networkInfo.getType() == TYPE_MOBILE) {
                        intentName = TetherIntents.EVENT_MOBILE_OFF;
                    }
                }
            }
        } else if (WIFI_AP_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

            if (WifiManager.WIFI_STATE_ENABLED == state % 10) {
                intentName = TetherIntents.EVENT_TETHER_ON;
            } else if (WifiManager.WIFI_STATE_DISABLED == state % 10) {
                intentName = TetherIntents.EVENT_TETHER_OFF;
            }
        }

        if (intentName != null) {
            Log.d("NCR", intentName);
            context.sendBroadcast(new Intent(intentName));
        }
    }
}
