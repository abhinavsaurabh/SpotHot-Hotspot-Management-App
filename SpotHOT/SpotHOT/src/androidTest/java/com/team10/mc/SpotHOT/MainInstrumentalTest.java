package com.team10.mc.SpotHOT;

import android.content.Context;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.team10.mc.SpotHOT.service.ServiceHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class MainInstrumentalTest {

    private final Context context = InstrumentationRegistry.getContext();
    private final ServiceHelper helper = new ServiceHelper(context);






    @Test
    public void name() {
        helper.setMobileDataEnabled(true);
        assertTrue(helper.isConnectedtToInternet());
        helper.setMobileDataEnabled(false);
        assertFalse(helper.isConnectedtToInternet());
    }
}
