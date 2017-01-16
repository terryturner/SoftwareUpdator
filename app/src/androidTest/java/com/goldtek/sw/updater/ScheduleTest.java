package com.goldtek.sw.updater;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.goldtek.sw.updater.test.UnitTestActivity;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ScheduleTest {
    private static final String TAG = "terry";

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private boolean isServiceRunning(Class<?> serviceClass) {
        Context appContext = InstrumentationRegistry.getTargetContext();
        ActivityManager manager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testWithStartedService() throws TimeoutException {
        mServiceRule.startService(new Intent(InstrumentationRegistry.getTargetContext(), ScheduleService.class));
        assertTrue(isServiceRunning(ScheduleService.class));
    }

    @Test
    public void testWithBoundService() throws TimeoutException {
        mServiceRule.bindService(
                new Intent(InstrumentationRegistry.getTargetContext(), ScheduleService.class));
        assertTrue(isServiceRunning(ScheduleService.class));
    }

    @Test
    public void testException() throws TimeoutException, InterruptedException {
        IBinder binder = mServiceRule.bindService(
                new Intent(InstrumentationRegistry.getTargetContext(), ScheduleService.class));
        ScheduleService service = ((ScheduleService.LocalBinder) binder).getService();
        try {
            service.exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(2000);
        assertTrue(isServiceRunning(ScheduleService.class));
    }

}
