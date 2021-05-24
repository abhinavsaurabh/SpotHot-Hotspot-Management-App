package com.team10.mc.SpotHOT;


import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


@SmallTest
@RunWith(AndroidJUnit4.class)
public class UtilsTest {

    @Test
    public void connectedClients() {
        assertEquals(0, Utils.connectedClients());
    }





}