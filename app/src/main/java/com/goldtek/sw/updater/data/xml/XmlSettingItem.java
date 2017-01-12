package com.goldtek.sw.updater.data.xml;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terry on 2017/1/3.
 */

public final class XmlSettingItem extends MaintainItem {
    private List<String> mReceivers = new ArrayList();
    private String mSender = "";
    private String mPWD = "";

    public XmlSettingItem() {
        super(1);
    }

    public void addReceiver(String address) {
        mReceivers.add(address);
    }
    public void setSender(String address) { mSender = address; }
    public void setSenderPassword(String pwd) { mPWD = pwd; }

    public String getSender() { return mSender; }
    public String getPassword() { return mPWD; }
    public List<String> getReceivers() { return mReceivers; }

    public boolean isValidSender() {
        return mSender != null && mPWD != null && !mSender.isEmpty() && !mSender.isEmpty();
    }

    @Override
    public void debug() {
        Log.i("terry", "---SettingItem---");
        Log.i("terry", mSender + "  /  " + mPWD);
        for (String address : mReceivers) Log.i("terry", address);
        Log.i("terry", "---------------------");
    }
}
