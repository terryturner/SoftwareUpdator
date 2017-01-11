package com.goldtek.sw.updater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.goldtek.sw.updater.ScheduleService;
import com.goldtek.sw.updater.model.ReportHandler;

public class RestartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("terry", "onReceive : " + intent.getAction());

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) 
        {
            startSticky(context, "start from boot completed");
        }

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
            if (intent.getDataString().equals("package:com.goldtek.sw.updater")) {
                startSticky(context, "software updater is replaced");
            }
        }

        if (intent.getAction().equals("com.goldtek.sw.updater.ScheduleService.onDestroy")) {
            startSticky(context, "start from died");
        }

        if (intent.getAction().equals("com.goldtek.sw.updater.ScheduleService.onException")) {
            startSticky(context, "start from exception");
        }
    }

    private void startSticky(Context context, String msg) {
        Log.i("terry", msg);
        ReportHandler.getInstance().writeMessage(msg);
        Intent sticky = new Intent(context, ScheduleService.class);
        context.startService(sticky);
    }
}
