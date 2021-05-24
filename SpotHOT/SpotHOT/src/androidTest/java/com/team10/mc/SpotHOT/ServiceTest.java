package com.team10.mc.SpotHOT;

import android.content.Intent;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import com.team10.mc.SpotHOT.service.TetheringService;


public class ServiceTest extends ServiceTestCase<TetheringService> {

    public ServiceTest() {
        super(TetheringService.class);
    }

    @SmallTest
    public void testOnCreate() throws Exception {
        Intent intent = new Intent(getContext(), TetheringService.class);
        startService(intent);
        assertNotNull(getService());

    }


}
