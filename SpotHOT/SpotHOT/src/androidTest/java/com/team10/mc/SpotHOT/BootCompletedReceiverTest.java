package com.team10.mc.SpotHOT;

import android.content.Intent;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import com.team10.mc.SpotHOT.receiver.BootCompletedReceiver;

import static org.mockito.Mockito.mock;


public class BootCompletedReceiverTest extends AndroidTestCase {
    private BootCompletedReceiver receiver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        receiver = new BootCompletedReceiver();
    }

    @SmallTest
    public void testStartActivity() {
        receiver = mock(BootCompletedReceiver.class);
        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        receiver.onReceive(getContext(), intent);

    }
}
