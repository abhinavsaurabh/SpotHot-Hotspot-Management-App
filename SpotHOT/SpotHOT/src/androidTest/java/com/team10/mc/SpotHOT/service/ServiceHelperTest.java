package com.team10.mc.SpotHOT.service;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ServiceHelperTest {

    private ServiceHelper helper;

    @Before
    public void before() {
        helper = new ServiceHelper(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void isWifiEnabled() {

        helper.enableWifi();
        assertTrue(helper.isWifiEnabled());
    }

    @Test
    public void btActive() {

        helper.setBluetoothStatus(true);
        assertTrue(helper.isBluetoothActive());
    }

    @Test
    public void batteryLevel() {
        assertTrue(helper.batteryLevel() > 0);
        assertTrue(helper.batteryLevel() <= 100);
    }

    @Test
    public void isPluggedToPower() {
        assertTrue(helper.isPluggedToPower());
    }

    @Test
    public void setWifiTethering() throws InterruptedException {
        helper.setWifiTethering(false, null);
        assertFalse(helper.isTetheringWiFi());
        helper.setWifiTethering(true, null);
        Thread.sleep(5000);
        assertTrue(helper.isTetheringWiFi());
    }

    @Test
    public void isConnectedtToInternet() {
        if (!helper.isWifiEnabled()) {
            helper.enableWifi();
        }
        assertTrue(helper.isConnectedtToInternet());
    }


}