package com.team10.mc.SpotHOT.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.team10.mc.SpotHOT.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DataUsageTimerTaskTest {

    private Context context;
    private SharedPreferences prefs;
    private DataUsageTimerTask task;

    @Before
    public void setUps() {
        context = ShadowApplication.getInstance().getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        task = new DataUsageTimerTask(context);
    }

    @Test
    public void firstUsage() {
        //GIVEN
        prefs.edit().clear().commit();
        //WHEN
        task.run();
        //THEN
        assertEquals(0, prefs.getLong("data.usage.last.value", 0));
        assertEquals(0, prefs.getLong("data.usage.removeAllData.value", 0));
        assertTrue(prefs.getLong("data.usage.removeAllData.timestamp", 0) > 0);
        assertTrue(prefs.getLong("data.usage.update.timestamp", 0) > 0);
    }

    @Test
    public void afterRestart() {
        //GIVEN
        prefs.edit().clear().commit();
        prefs.edit().putBoolean("data.limit.daily.reset", true).commit();
        prefs.edit().putLong("data.usage.removeAllData.value", 1000L).commit();
        prefs.edit().putLong("data.usage.last.value", 2000L).commit();
        prefs.edit().putLong("data.usage.removeAllData.timestamp", System.currentTimeMillis() - 24 * 60 * 60 * 1000).commit();

        //WHEN
        task.run();

        //THEN
        assertEquals(0, prefs.getLong("data.usage.last.value", 0));
        assertEquals(0, prefs.getLong("data.usage.removeAllData.value", 0));
        assertTrue(prefs.getLong("data.usage.removeAllData.timestamp", 0) > 0);
        assertTrue(prefs.getLong("data.usage.update.timestamp", 0) > 0);
    }


    @Test
    public void general() {
        //GIVEN
        prefs.edit().clear().commit();
        prefs.edit().putLong("data.usage.removeAllData.value", 1000L).commit();
        prefs.edit().putLong("data.usage.last.value", 0L).commit();
        prefs.edit().putLong("data.usage.update.timestamp", System.currentTimeMillis()).commit();
        prefs.edit().putLong("data.usage.removeAllData.timestamp", System.currentTimeMillis()).commit();

        //WHEN
        task.run();
        task.run();

        //THEN
        assertEquals(0L, prefs.getLong("data.usage.last.value", 0));
        assertEquals(1000L, prefs.getLong("data.usage.removeAllData.value", 0));
    }

}