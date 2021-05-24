package com.team10.mc.SpotHOT.db;

import com.team10.mc.SpotHOT.db.WiFiTethering.SECURITY_TYPE;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class WiFiTetheringTest {
    @Test
    public void name() {
        assertEquals(SECURITY_TYPE.WPAPSK, SECURITY_TYPE.valueOf("WPAPSK"));
        assertEquals(SECURITY_TYPE.WPA2PSK, SECURITY_TYPE.valueOf("WPA2PSK"));
        assertEquals(SECURITY_TYPE.OPEN, SECURITY_TYPE.valueOf("OPEN"));
    }

    @Test
    public void code() {
        assertEquals(4, SECURITY_TYPE.WPA2PSK.getCode());
    }
}