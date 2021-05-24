package com.team10.mc.SpotHOT;

import android.content.Context;
import android.content.Intent;

import com.team10.mc.SpotHOT.receiver.BootCompletedReceiver;
import com.team10.mc.SpotHOT.service.TetheringService;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class ExampleUnitTest {

    @Test
    public void dummyTest() {
        assertTrue(true);
    }

    @Test
    @Ignore
    public void testStartActivity() {
        BootCompletedReceiver receiver = mock(BootCompletedReceiver.class);
        Context context = mock(Context.class);
        TetheringService service = mock(TetheringService.class);

        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        receiver.onReceive(context, intent);
        verify(service, atLeastOnce());
    }
}