package com.team10.mc.SpotHOT.db;

import com.team10.mc.SpotHOT.BuildConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DBManagerTest {
    private DBManager db;

    @Before
    public void setUp() {
        db = DBManager.getInstance(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() {
        db.removeAllData();
        db.close();
    }

    @Test
    public void shouldAddCron() {
        Cron cron = new Cron(9, 0, 12, 0, 0, 0);
        long res = db.addOrUpdateCron(cron);
        assertTrue(res > 0);
    }

    @Test
    public void shouldGetCrons() {
        Cron cron = new Cron(9, 0, 12, 0, 0, 0);
        long id = db.addOrUpdateCron(cron);

        assertEquals(1, db.getCrons().size());
        assertEquals(id, db.getCrons().get(0).getId());
    }

    @Test
    public void checkEmptyDB() {
        assertTrue(db.getCrons().isEmpty());

        assertEquals(null, db.getCron(1));
        assertEquals(null, db.getWifiTethering(1));
    }
}