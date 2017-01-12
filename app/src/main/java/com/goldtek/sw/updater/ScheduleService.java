package com.goldtek.sw.updater;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.goldtek.sw.updater.data.GetResponse;
import com.goldtek.sw.updater.data.PmRequest;
import com.goldtek.sw.updater.model.ReportHandler;
import com.goldtek.sw.updater.model.ScheduleHandler;
import com.goldtek.sw.updater.presenter.ConfigManager;
import com.goldtek.sw.updater.presenter.PackageManager;
import com.goldtek.sw.updater.receiver.ReceiverManager;

import java.net.HttpURLConnection;

/**
 * Created by Terry on 2016/12/29.
 */

public class ScheduleService extends Service implements ScheduleHandler.Listener, ConfigManager.Observer {
    private static final String TAG = "ScheduleService";
    private static final int MINUTE2MILLI = 60000;

    private final IBinder mBinder = new LocalBinder();
    private final ReceiverManager mReceiver = new ReceiverManager(this);

    private Handler mPoller = null;
    private HandlerThread mPollThread = null;
    private HandlerThread mExecuteThread = null;
    private ScheduleHandler mExecutor = null;

    private Toast mToastWord;

    public class LocalBinder extends Binder {
        public ScheduleService getService() {
            return ScheduleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long id = Thread.currentThread().getId();
        Log.i(TAG, "Received startId " + startId +  " ThreadId " + id + " : hashCode " + ScheduleService.this.hashCode());
        if (!mReceiver.isReceiverRegistered(checkAlive)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            //mReceiver.registerReceiver(checkAlive, filter);
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int pid = android.os.Process.myPid();
        long id = Thread.currentThread().getId();
        Log.i(TAG, "onCreate Pid " + pid + ", ThreadId " + id + " : hashCode " + ScheduleService.this.hashCode());
        String msg = String.format(getString(R.string.msg_schedule_start_format), pid, PackageManager.getInstance().getApplicationInfo().versionCode);
        ReportHandler.getInstance().writeMessage(msg);

        mPollThread = new HandlerThread("polling");
        mPollThread.start();
        mPoller = new Handler(mPollThread.getLooper());

        mExecuteThread = new HandlerThread("executor");
        mExecuteThread.start();
        mExecutor = new ScheduleHandler(mExecuteThread.getLooper(), this);
        mExecutor.setListener(this);

        PackageManager.getInstance();
        ConfigManager.getInstance().queryConfig();
        ConfigManager.getInstance().addObserver(this);

        if (ConfigManager.getInstance().isAutoUpdate()) {
            mExecutor.sendEmptyMessage(ScheduleHandler.GET_XML_FILE);
            mPoller.postDelayed(AutoUpdate, ConfigManager.getInstance().getSyncFrequency() * MINUTE2MILLI);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("com.goldtek.sw.updater.ScheduleService.onDestroy"));
        mReceiver.unregisterReceiver(checkAlive);

        ConfigManager.getInstance().removeObserver(this);

        mPoller.removeCallbacksAndMessages(null);
        mPoller = null;
        mPollThread.quit();
        mPollThread.interrupt();
        mPollThread = null;
        mExecutor.removeCallbacksAndMessages(null);
        mExecutor = null;
        mExecuteThread.quit();
        mExecuteThread.interrupt();
        mExecuteThread = null;
    }

    @Override
    public void onPostExecute(final GetResponse result) {
        Log.i("terry", result.Code + " : " + result.Request.FileName);

        if (mToastWord != null) mToastWord.cancel();
        if (result.Request.FileName.equals("sample.xml")) {
            Message.obtain(mExecutor, ScheduleHandler.PARSE_XML_FILE, result).sendToTarget();
            ConfigManager.getInstance().recordSyncTime(result.isHttpOK());

            mToastWord = Toast.makeText(getApplicationContext(), result.isHttpOK() ? "Sync now" : "Sync Fail", Toast.LENGTH_SHORT);
            mToastWord.show();

            if (!result.isHttpOK()) {
                ReportHandler.getInstance().writeMessageFormat(R.string.msg_schedule_sync_fail_format, result.Code, result.Request.RequestURL);
                //TODO: send report via mail due to sync fail xxx times
            }
        } else if (result.Request.getOption(ConfigManager.KEY_PACKAGE_NAME).equals(getPackageName())) {
            if (result.isHttpOK()) {
                PmRequest request = new PmRequest(getPackageName(), result.FilePath);
                Message.obtain(mExecutor, ScheduleHandler.INSTALL_APK_FILE, request).sendToTarget();
            } else
                ReportHandler.getInstance().writeMessageFormat(R.string.msg_schedule_sync_fail_format, result.Code, result.Request.RequestURL);
        }

    }

    @Override
    public void onPackageInstall(String packageName, int returnCode) {
        ReportHandler.getInstance().writeMessageFormat(R.string.msg_schedule_install_format, returnCode, packageName);
    }


    @Override
    public void onMaintainChange() {
        Message.obtain(mExecutor, ScheduleHandler.CHK_UPDATER_AVAILABLE).sendToTarget();
        //TODO: check other app update available
    }

    @Override
    public void onConfigChange(String key, String value) {
        switch (key) {
            case ConfigManager.KEY_UPDATE_AUTO:
                mPoller.removeCallbacks(AutoUpdate);
                if (ConfigManager.getInstance().isAutoUpdate()) {
                    mPoller.postDelayed(AutoUpdate, ConfigManager.getInstance().getSyncFrequency()*MINUTE2MILLI);
                }
                break;
            case ConfigManager.KEY_SYNC_TIME:
                mPoller.removeCallbacks(AutoUpdate);
                if (ConfigManager.getInstance().isAutoUpdate()) {
                    mPoller.postDelayed(AutoUpdate, ConfigManager.getInstance().getSyncFrequency()*MINUTE2MILLI);
                }
                break;
        }
    }

    public void setAutoUpdate(boolean enabled) {
        if (enabled) mExecutor.removeCallbacksAndMessages(null);
        else mExecutor.sendEmptyMessage(ScheduleHandler.POST_AUTO_UPDATE);
    }

    public void modifyFrequency() {
        mExecutor.removeCallbacksAndMessages(null);
        mExecutor.sendEmptyMessage(ScheduleHandler.POST_AUTO_UPDATE);
    }

    public void sync() {
        mExecutor.sendEmptyMessage(ScheduleHandler.GET_XML_FILE);
    }

    public void exception() {
//        int pid = android.os.Process.myPid();
//        long id = Thread.currentThread().getId();
//        Log.i(TAG, "exception Pid " + pid + ", ThreadId " + id + " : hashCode " + ScheduleService.this.hashCode());

        int[] array = new int[2];
        array[2] = 0;
    }

    Runnable AutoUpdate = new Runnable() {
        @Override
        public void run() {
            if (ConfigManager.getInstance().isAutoUpdate()) {
                sync();
                mPoller.postDelayed(AutoUpdate, ConfigManager.getInstance().getSyncFrequency()*MINUTE2MILLI);
            }
        }
    };

    BroadcastReceiver checkAlive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//        int pid = android.os.Process.myPid();
//        long id = Thread.currentThread().getId();
//        Log.i(TAG, "checkAlive " + pid + ", ThreadId " + id + " : hashCode " + ScheduleService.this.hashCode());
        }
    };

}
