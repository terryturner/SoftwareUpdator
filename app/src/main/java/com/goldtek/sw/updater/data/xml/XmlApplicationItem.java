package com.goldtek.sw.updater.data.xml;

import android.net.Uri;
import android.util.Log;

import com.goldtek.sw.updater.GoldtekApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Terry on 2017/1/3.
 */

public final class XmlApplicationItem extends MaintainItem {
    private String packageName = "";
    private int versionCode = -1;
    private Date deployTime = new Date();
    private Uri uri = Uri.parse("");

    public XmlApplicationItem() {
        super(2);
    }

    public void setPackageName(String name) { packageName = name; }
    public void setVersionCode(int ver) { versionCode = ver; }
    public void setDeployTime(Date date) { deployTime = date; }
    public void setURL(Uri link) { uri = link; }

    public String getPackageName() { return packageName; }
    public int getVersionCode() { return versionCode; }
    public Date getDeployTime() { return deployTime; }
    public Uri getUrl() { return uri; }
    public boolean isUpdater() {
        return GoldtekApplication.getContext().getPackageName().equals(packageName);
    }

    @Override
    public void debug() {
        Log.i("terry", "---ApplicationItem---");
        Log.i("terry", "name: " + packageName + " ver: " + versionCode);
        if (deployTime != null)
            Log.i("terry", "time: " + GoldtekApplication.sDateFormat.format(deployTime));
        Log.i("terry", "url: " + uri);
        Log.i("terry", "---***************---");
    }
}
