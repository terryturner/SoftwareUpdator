package com.goldtek.sw.updater.test;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.view.View;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.R;
import com.goldtek.sw.updater.ScheduleService;
import com.goldtek.sw.updater.ScheduleService.LocalBinder;
import com.goldtek.sw.updater.data.GetRequest;
import com.goldtek.sw.updater.data.GetResponse;
import com.goldtek.sw.updater.data.xml.MaintainItem;
import com.goldtek.sw.updater.model.HttpDownloader;
import com.goldtek.sw.updater.model.HttpsDownloader;
import com.goldtek.sw.updater.model.XmlParser;


import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Created by Terry on 2016/12/29.
 */

public class UnitTestActivity extends Activity implements View.OnClickListener {
    private boolean testResult = false;

    public boolean isSuccess() { return testResult; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unit_test);

        findViewById(R.id.parseAssetXML).setOnClickListener(this);
        findViewById(R.id.downloadHttp).setOnClickListener(this);
        findViewById(R.id.downloadHttpAuth).setOnClickListener(this);
        findViewById(R.id.downloadHttps).setOnClickListener(this);
        findViewById(R.id.downloadHttpsAuth).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.parseAssetXML:
                testPrintXML();
                break;
            case R.id.downloadHttp:
                downloadHttp("http://192.168.42.35/sample.xml", null, GoldtekApplication.sFileXml);
                break;
            case R.id.downloadHttpAuth:
                downloadHttp("http://192.168.42.35/test_auth/sample.xml", "terry:test", GoldtekApplication.sFileXml);
                break;
            case R.id.downloadHttps:
                downloadHttps("https://192.168.42.35/sample.xml", null, GoldtekApplication.sFileXml);
                break;
            case R.id.downloadHttpsAuth:
                downloadHttps("https://192.168.42.35/test_auth/sample.xml", "terry:test", GoldtekApplication.sFileXml);
                break;
        }
    }


    public void testPrintXML() {
        testResult = false;

        XmlParser parser = new XmlParser();
        try {
            List<MaintainItem> xml = parser.parse(getAssets().open("sample.xml"));
            for (MaintainItem item : xml) {
                item.debug();
            }
            testResult = true;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadHttp(String url, String auth, String file) {
        testResult = false;

        GetRequest request = new GetRequest(url, file);
        if (auth != null) request.setOption(HttpDownloader.KEY_AUTH, auth);
        new HttpDownloader(new HttpDownloader.IDownload() {
            @Override
            public void onProgressUpdate(int progress) {}

            @Override
            public void onPostExecute(GetResponse result) {
                //Log.i("terry", result.FilePath + " get code: " + result.Code);
                if (result.isHttpOK()) testResult = true;
            }
        }).execute(request);
    }

    public void downloadHttps(String url, String auth, String file) {
        testResult = false;

        GetRequest request = new GetRequest(url, file);
        if (auth != null) request.setOption(HttpDownloader.KEY_AUTH, auth);
        new HttpsDownloader(new HttpsDownloader.IDownload() {
            @Override
            public void onProgressUpdate(int progress) {}

            @Override
            public void onPostExecute(GetResponse result) {
                //Log.i("terry", result.FilePath + " get code: " + result.Code);
                if (result.isHttpOK()) testResult = true;
            }
        }).execute(request);
    }




}
