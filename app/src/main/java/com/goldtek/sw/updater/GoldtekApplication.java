package com.goldtek.sw.updater;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.goldtek.sw.updater.model.CrashHandler;

import java.text.SimpleDateFormat;

/**
 * Created by Terry on 2016/12/30.
 */

public class GoldtekApplication extends Application {
    public final static SimpleDateFormat sDateFormat =  new SimpleDateFormat("yyyy/MM/dd-HH:mm");
    private static Context mContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

//        int pid = android.os.Process.myPid();
//        long id = Thread.currentThread().getId();
//        Log.i("terry", "Application onCreate Pid " + pid + ", ThreadId " + id);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }

    public static Context getContext() {
        return mContext;
    }
}
