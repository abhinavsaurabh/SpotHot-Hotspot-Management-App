package com.team10.mc.SpotHOT.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;


import com.team10.mc.SpotHOT.TetherIntents;
import com.team10.mc.SpotHOT.Utils;

import java.util.TimerTask;


class DataUsageTimerTask extends TimerTask {
    private static final String TAG = "DataUsageTimerTask";
    private final Context context;
    private final SharedPreferences prefs;

    public DataUsageTimerTask(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void run() {
        long lastReset = prefs.getLong("data.usage.removeAllData.timestamp", 0);
        long lastUpdate = prefs.getLong("data.usage.update.timestamp", 0);
        long lastBootTime = lastBootTime();


        if (lastReset == 0) {

            reset(ServiceHelper.getDataUsage());
        }


        if (prefs.getBoolean("data.limit.daily.reset", false) && !DateUtils.isToday(prefs.getLong("data.usage.removeAllData.timestamp", 0))) {

            reset(ServiceHelper.getDataUsage());
        }


        if (lastBootTime > lastUpdate) {
            //MyLog.i(TAG, "Adjust after the boot " + ServiceHelper.getDataUsage());
            long offset = prefs.getLong("data.usage.last.value", 0) + prefs.getLong("data.usage.removeAllData.value", 0);
            prefs.edit().putLong("data.usage.removeAllData.value", offset).apply();
            prefs.edit().putLong("data.usage.update.timestamp", System.currentTimeMillis()).apply();
        }

        prefs.edit().putLong("data.usage.last.value", ServiceHelper.getDataUsage()).apply();
        //MyLog.d("datausage" , ServiceHelper.getDataUsage() + " | " + prefs.getLong("data.usage.removeAllData.value", 0) + " | " + prefs.getLong("data.usage.last.value", 0));
        long usage = ServiceHelper.getDataUsage() + prefs.getLong("data.usage.removeAllData.value", 0);
        Intent onIntent = new Intent(TetherIntents.DATA_USAGE);
        onIntent.putExtra("value", usage);
        context.sendBroadcast(onIntent);
    }

    private void reset(long value) {
        Utils.resetDataUsageStat(prefs, -value, 0);
    }

    private long lastBootTime() {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime();
    }
}
