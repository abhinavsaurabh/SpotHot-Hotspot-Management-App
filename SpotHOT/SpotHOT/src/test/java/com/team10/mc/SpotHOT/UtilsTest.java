package com.team10.mc.SpotHOT;

import android.content.SharedPreferences;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UtilsTest {

    @Test
    public void testValidateTime() {
        assertTrue(Utils.validateTime("0:00"));
        assertTrue(Utils.validateTime("23:59"));
        assertFalse(Utils.validateTime("24:60"));
        assertFalse(Utils.validateTime("123:123"));
    }

    @Test
    public void testMaskToDays() {
        assertEquals("", Utils.maskToDays(0));
        assertEquals("Mon", Utils.maskToDays(1));
        assertEquals("Tue", Utils.maskToDays(2));
        assertEquals("Mon, Tue", Utils.maskToDays(3));
        assertEquals("Mon, Tue, Wed", Utils.maskToDays(7));
        assertEquals("Sun", Utils.maskToDays(64));
    }

  /*  @Test
    public void testFindPreferredDevices() {
        SharedPreferences prefs = mock(SharedPreferences.class);
        Map<String, ?> map = new HashMap<>();
       // List<String> devices = Utils.findPreferredDevices(prefs);
    }*/

    @Test
    public void testFindPreferredDevices() {
        SharedPreferences preferences = mock(SharedPreferences.class);
        Map map = new HashMap<>();
        map.put("bt.devices.ITEM1", "ITEM1");
        map.put("bt.devices.ITEM2", "ITEM2");
        map.put("bt.devices.ITEM3", "ITEM3");

        when(preferences.getAll()).thenReturn(map);
        when(preferences.getLong("bt.last.connect.ITEM1", 0)).thenReturn(100L);
        when(preferences.getLong("bt.last.connect.ITEM2", 0)).thenReturn(0L);
        when(preferences.getLong("bt.last.connect.ITEM3", 0)).thenReturn(10000L);
        List<String> list = Utils.findPreferredDevices(preferences);
        assertEquals("ITEM3", list.get(0));
        assertEquals("ITEM1", list.get(1));
        assertEquals("ITEM2", list.get(2));
    }

    @Test
    public void humanReadableByteCount() {
        assertEquals("1023B", Utils.humanReadableByteCount(1023));
        //assertEquals("2,0kB", Utils.humanReadableByteCount(2000));
        //assertEquals("15,6kB", Utils.humanReadableByteCount(16000));
    }

    @Test
    public void humanReadableDistance() {
        assertEquals("999m", Utils.humanReadableDistance(999));

    }

    @Test
    public void strToIntDefaultValue() {
        assertEquals(0, Utils.strToInt("nil"));
        assertEquals(1, Utils.strToInt("1"));
        assertEquals(2, Utils.strToInt("2", 1));
        assertEquals(0, Utils.strToInt(""));
        assertEquals(1, Utils.strToInt("", 1));
        assertEquals(0, Utils.strToInt(" "));
        assertEquals(1, Utils.strToInt(" ", 1));
        assertEquals(0, Utils.strToInt("a"));
        assertEquals(1, Utils.strToInt("a", 1));
    }
}