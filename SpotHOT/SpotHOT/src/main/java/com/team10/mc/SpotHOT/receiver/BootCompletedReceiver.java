package com.team10.mc.SpotHOT.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.team10.mc.SpotHOT.Utils;
import com.team10.mc.SpotHOT.activity.MainActivity;
import com.team10.mc.SpotHOT.service.TetheringService;

import static android.os.Build.VERSION.SDK_INT;


public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        final Intent serviceIntent = new Intent(context, TetheringService.class);
        int delay = Utils.strToInt(prefs.getString("activate.on.startup.delay", "0"));
        if (delay == 0) {
            if (SDK_INT < Build.VERSION_CODES.O) {
                context.startService(serviceIntent);
            } else {
                context.startForegroundService(serviceIntent);
            }
        } else {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent mStartActivity = new Intent(context, MainActivity.class);
            PendingIntent onPendingIntent = PendingIntent.getActivity(context, 123456, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);

            if (SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay * 1000L, onPendingIntent);
            } else if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay * 1000L, onPendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay * 1000L, onPendingIntent);
            }

        }
    }
}
