package com.goldtek.sw.updater;

import android.app.ActivityManager;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.goldtek.sw.updater.test.TestActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SoftwareUpdaterTest {
    private static final String TAG = "terry";

    @Rule
    public ActivityTestRule<TestActivity> mActivityRule = new ActivityTestRule<>(
            TestActivity.class);

    @Test
    public void useAppContext() throws Exception {
        Log.i(TAG, "---useAppContext---");
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.goldtek.sw.updater", appContext.getPackageName());

    }

    @Test
    public void useStartStopButton() throws Exception {
        Log.i(TAG, "---useStartButton---");
        onView(withId(R.id.start)).perform(click());
        Thread.sleep(3000);
        assertTrue(isServiceRunning(ScheduleService.class));

        Log.i(TAG, "---useStopButton---");
        onView(withId(R.id.stop)).perform(click());
        Thread.sleep(3000);
        assertTrue(isServiceRunning(ScheduleService.class));
    }

//    @Test
//    public void useExceptionButton() throws Exception {
//        Log.i(TAG, "---useExceptionButton---");
//        onView(withId(R.id.exception)).perform(click());
//        Thread.sleep(3000);
//        assertTrue(isServiceRunning(ScheduleService.class));
//    }


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
}
