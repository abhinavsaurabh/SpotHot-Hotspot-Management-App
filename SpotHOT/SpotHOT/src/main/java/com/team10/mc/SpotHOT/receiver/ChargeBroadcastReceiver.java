package com.team10.mc.SpotHOT.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;


import com.team10.mc.SpotHOT.Utils;
import com.team10.mc.SpotHOT.service.ServiceHelper;
import com.team10.mc.SpotHOT.service.TetheringService;

import static android.content.Intent.ACTION_POWER_CONNECTED;
import static android.content.Intent.ACTION_POWER_DISCONNECTED;
import static android.os.Build.VERSION.SDK_INT;
import static com.team10.mc.SpotHOT.TetherIntents.USB_OFF;
import static com.team10.mc.SpotHOT.TetherIntents.USB_ON;


public class ChargeBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "USB Broadcast Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ServiceHelper helper = new ServiceHelper(context);
        Intent usbIntent = null;
        //MyLog.i(TAG, intent.getAction());

        switch (intent.getAction()) {
            case ACTION_POWER_CONNECTED:
                usbIntent = new Intent(USB_ON);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                if (prefs.getBoolean("usb.internet.start.service", false) && !helper.isServiceRunning(TetheringService.class)) {
                    Intent serviceIntent = new Intent(context, TetheringService.class);
                    serviceIntent.putExtra("usb.on", true);
                    if (SDK_INT < Build.VERSION_CODES.O) {
                        context.startService(serviceIntent);
                    } else {
                        context.startForegroundService(serviceIntent);
                    }
                }
                break;
            case ACTION_POWER_DISCONNECTED:
                if (helper.isTetheringWiFi()) {
                    usbIntent = new Intent(USB_OFF);
                }
                break;
        }

        if (usbIntent != null) {
            Utils.broadcast(context, usbIntent);
        }
    }
}
