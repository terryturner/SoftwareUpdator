package com.goldtek.sw.updater;

import android.app.ActivityManager;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.goldtek.sw.updater.test.TestActivity;
import com.goldtek.sw.updater.test.UnitTestActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class UnitTest {
    private static final String TAG = "terry";

    @Rule
    public ActivityTestRule<UnitTestActivity> mActivityRule = new ActivityTestRule<>(UnitTestActivity.class);

    @Test
    public void parseAssetXML() throws Exception {
        onView(withId(R.id.parseAssetXML)).perform(click());
        assertTrue(mActivityRule.getActivity().isSuccess());
    }

    @Test
    public void downloadHttp() throws Exception {
        onView(withId(R.id.downloadHttp)).perform(click());
        assertTrue(mActivityRule.getActivity().isSuccess());
    }

    @Test
    public void downloadHttpAuth() throws Exception {
        onView(withId(R.id.downloadHttpAuth)).perform(click());
        assertTrue(mActivityRule.getActivity().isSuccess());
    }

    @Test
    public void downloadHttps() throws Exception {
        onView(withId(R.id.downloadHttps)).perform(click());
        assertTrue(mActivityRule.getActivity().isSuccess());
    }

    @Test
    public void downloadHttpsAuth() throws Exception {
        onView(withId(R.id.downloadHttpsAuth)).perform(click());
        assertTrue(mActivityRule.getActivity().isSuccess());
    }
}
