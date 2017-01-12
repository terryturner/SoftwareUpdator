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
    private String auth_account = null;
    private String auth_password = null;

    public XmlApplicationItem() {
        super(2);
    }

    public void setPackageName(String name) { packageName = name; }
    public void setVersionCode(int ver) { versionCode = ver; }
    public void setDeployTime(Date date) { deployTime = date; }
    public void setURL(Uri link) { uri = link; }
    public void setAuthAccount(String account) { auth_account = account; }
    public void setAuthPassword(String pwd) { auth_password = pwd; }

    public String getPackageName() { return packageName; }
    public int getVersionCode() { return versionCode; }
    public Date getDeployTime() { return deployTime; }
    public Uri getUrl() { return uri; }
    public String getAuthAccount() { return auth_account; }
    public String getAuthPassword() { return auth_password; }
    public String getAuth() {
        return auth_account + ":" + auth_password;
    }

    public boolean isUpdater() {
        return GoldtekApplication.getContext().getPackageName().equals(packageName);
    }

    public boolean isValidPackageName() {
        return packageName != null && !packageName.isEmpty();
    }

    public boolean needAuthentication() {
        return auth_account != null && auth_password != null && !auth_account.isEmpty() && !auth_password.isEmpty();
    }

    @Override
    public void debug() {
        Log.i("terry", "---ApplicationItem---");
        Log.i("terry", "name: " + packageName + " ver: " + versionCode);
        if (deployTime != null)
            Log.i("terry", "time: " + GoldtekApplication.sDateFormat.format(deployTime));
        if (needAuthentication())
            Log.i("terry", String.format("auth url: %s , acc: %s:%s", uri, auth_account, auth_password));
        else
            Log.i("terry", String.format("basic url: %s", uri));
        Log.i("terry", "---------------------");
        Log.i("terry", " ");
    }
}
