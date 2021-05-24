package com.team10.mc.SpotHOT.service;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ServiceActionTest {

    @Test
    public void general() {
        assertTrue(ServiceAction.INTERNET_OFF.isInternet());
        assertFalse(ServiceAction.INTERNET_OFF.isOn());
        assertFalse(ServiceAction.INTERNET_OFF.isTethering());
    }
}