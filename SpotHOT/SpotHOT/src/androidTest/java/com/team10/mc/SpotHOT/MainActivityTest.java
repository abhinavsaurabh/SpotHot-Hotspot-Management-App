package com.team10.mc.SpotHOT;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import com.team10.mc.SpotHOT.activity.MainActivity;


public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MainActivity activity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    @SmallTest
    public void testPreconditions() throws Exception {
        assertNotNull(activity);
    }

    @SmallTest
    public void testActivityHasFocus() {

    }
}
