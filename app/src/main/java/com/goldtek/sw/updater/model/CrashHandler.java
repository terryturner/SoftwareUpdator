package com.goldtek.sw.updater.model;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

/**
 * Created by Terry on 2016/12/30.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private Context mContext;

    private static CrashHandler INSTANCE;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        ReportHandler.getInstance().writeException(ex);

        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);

            mContext.sendBroadcast(new Intent("com.goldtek.sw.updater.ScheduleService.onException"));
        }
    }

    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }
//        final String msg = ex.getLocalizedMessage();
        final StackTraceElement[] stack = ex.getStackTrace();
        final String message = ex.getMessage();

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                /*
                String fileName = "crash-" + System.currentTimeMillis()  + ".log";
                File file = new File(Environment.getExternalStorageDirectory(), fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file,true);
                    fos.write(message.getBytes());
                    for (int i = 0; i < stack.length; i++) {
                        fos.write(stack[i].toString().getBytes());
                    }
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                }*/
                Looper.loop();
            }

        }.start();
        return true;
    }
}
