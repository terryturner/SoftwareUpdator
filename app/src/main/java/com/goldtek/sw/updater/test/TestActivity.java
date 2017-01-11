package com.goldtek.sw.updater.test;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.goldtek.sw.updater.R;
import com.goldtek.sw.updater.ScheduleService;
import com.goldtek.sw.updater.ScheduleService.LocalBinder;
import com.goldtek.sw.updater.data.Response;
import com.goldtek.sw.updater.data.xml.MaintainItem;
import com.goldtek.sw.updater.data.Mail;
import com.goldtek.sw.updater.model.HttpDownloader;
import com.goldtek.sw.updater.model.HttpsDownloader;
import com.goldtek.sw.updater.model.MailSender;
import com.goldtek.sw.updater.model.XmlParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

/**
 * Created by Terry on 2016/12/29.
 */

public class TestActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);

        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.stress).setOnClickListener(this);
        findViewById(R.id.config).setOnClickListener(this);
        findViewById(R.id.bind).setOnClickListener(this);
        findViewById(R.id.unbind).setOnClickListener(this);
        findViewById(R.id.parseXML).setOnClickListener(this);
        findViewById(R.id.download).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Intent sticky = new Intent(this, ScheduleService.class);
        switch (view.getId()) {
            case R.id.start:
                startService(sticky);
                break;
            case R.id.stop:
                stopService(sticky);
                break;
            case R.id.stress:
                idx = 0;
                h.removeCallbacksAndMessages(null);
                h.postDelayed(runSticky, 1000);
                break;
            case R.id.config:
                Intent intent=new Intent();
                intent.setComponent(new ComponentName("com.goldtek.sw.updater", "com.goldtek.sw.updater.LoginActivity"));
                startActivity(intent);
                break;
            case R.id.bind:
                if (!mBound) bindService(sticky, conn, Context.BIND_AUTO_CREATE);
                break;
            case R.id.unbind:
                if (mBound) {
                    unbindService(conn);
                    bindSticky = null;
                    mBound = false;

                    enableBindingButton(false);
                }
                break;
            case R.id.parseXML:
                //if (bindSticky != null) bindSticky.exception();
                testPrintXML();
                break;
            case R.id.download:
                //if (bindSticky != null) bindSticky.killProcess();
                new HttpDownloader(new HttpDownloader.IDownload() {
                    @Override
                    public void onProgressUpdate(int progress) {}

                    @Override
                    public void onPostExecute(Response result) {
                        Log.i("terry", result.fileName + " get code: " + result.code);
                    }
                }).execute("http://192.168.42.35/test_auth/main.xml", "terry:terryy", "main.xml");

                break;
        }
    }

    private void testPrintXML() {
        XmlParser parser = new XmlParser();
        try {
            List<MaintainItem> xml = parser.parse(getAssets().open("sample.xml"));
            for (MaintainItem item : xml) {
                item.debug();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enableBindingButton(final boolean enable) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //findViewById(R.id.exception).setEnabled(enable);
                //findViewById(R.id.killProcess).setEnabled(enable);
            }
        });
    }

    // Stress Test
    int idx = 0;
    Handler h = new Handler();
    Runnable runSticky = new Runnable() {
        @Override
        public void run() {
            Intent sticky = new Intent(getApplication(), ScheduleService.class);
            startService(sticky);
            if (idx < Integer.MAX_VALUE) {
                h.postDelayed(runSticky, 100);
            }
        }
    };

    // Bind Test
    private boolean mBound = false;
    private ScheduleService bindSticky;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocalBinder binder = (LocalBinder) iBinder;
            bindSticky = binder.getService();
            mBound = true;

            enableBindingButton(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bindSticky = null;
            mBound = false;

            enableBindingButton(false);
        }
    };


}
