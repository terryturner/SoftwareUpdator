package com.goldtek.sw.updater;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.goldtek.sw.updater.model.CrashHandler;
import com.goldtek.sw.updater.model.ReportHandler;
import com.goldtek.sw.updater.presenter.PackageManager;

import java.text.SimpleDateFormat;

/**
 * Created by Terry on 2016/12/30.
 */

public class GoldtekApplication extends Application {
    public final static String sFileXml = "main";
    public final static SimpleDateFormat sDateFormat =  new SimpleDateFormat("yyyy/MM/dd-HH:mm");
    private static Context mContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        startService(new Intent(this, ScheduleService.class));
//        int pid = android.os.Process.myPid();
//        long id = Thread.currentThread().getId();
//        Log.i("terry", "Application onCreate Pid " + pid + ", ThreadId " + id);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        int pid = android.os.Process.myPid();
        String msg = String.format(getString(R.string.msg_application_terminal_format), pid, PackageManager.getInstance().getApplicationInfo().versionCode);
        ReportHandler.getInstance().writeMessage(msg);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        int pid = android.os.Process.myPid();
        String msg = String.format(getString(R.string.msg_application_trim_mem_format), level, pid, PackageManager.getInstance().getApplicationInfo().versionCode);
        //ReportHandler.getInstance().writeMessage(msg);
    }

    public static Context getContext() {
        return mContext;
    }

    public static String getFilePath(String packageName) {
        return getContext().getFilesDir() + "/" + packageName;
    }

    public static String getXmlPath() {
        return getContext().getFilesDir() + "/" + sFileXml;
    }
}
