package com.goldtek.sw.updater.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.R;
import com.goldtek.sw.updater.data.xml.MaintainItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Terry on 2017/1/3.
 */

public class ConfigManager {
    public final static String KEY_LAST_SUCCESS = "last_success_time";
    public final static String KEY_LAST_SYNC = "last_sync_time";

    public final static String KEY_UPDATE_AUTO = "update_auto";
    public final static String KEY_SYNC_TIME = "sync_frequency";
    public final static String KEY_PRIMARY_SERVER_URL = "primary_server_url";
    public final static String KEY_PRIMARY_SERVER_ACCOUNT = "primary_server_account";
    public final static String KEY_PRIMARY_SERVER_PASSWORD = "primary_server_password";


    private static final ConfigManager sInstance = new ConfigManager(GoldtekApplication.getContext());

    public static ConfigManager getInstance() {
        return sInstance;
    }

    private Context mContext;
    private boolean mIsEnableAuto;
    private String mSyncTime;
    private String mPrimaryServerURL;
    private String mPrimaryServerAccount;
    private String mPrimaryServerPwd;
    private long mLastSyncTime;
    private long mLastSyncSuccessTime;

    private List<Observer> mObservers = new ArrayList<>();
    private List<MaintainItem> mMaintenanceList = new ArrayList<>();

    public interface Observer {
        void onMaintainChange();
        void onConfigChange(String key, String value);
    }

    private ConfigManager(Context context) {
        mContext = context;
        queryConfig();
    }

    public void setMaintenance(List<MaintainItem> items) {
        mMaintenanceList = items;
        for(Observer cb : mObservers) cb.onMaintainChange();
    }

    public List<MaintainItem> getMaintenance() {
        return mMaintenanceList;
    }

    public void addObserver(Observer cb) {
        if (!mObservers.contains(cb)) mObservers.add(cb);
    }

    public void removeObserver(Observer cb) {
        if (mObservers.contains(cb)) mObservers.remove(cb);
    }

    public void queryConfig() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        mLastSyncTime = settings.getLong(KEY_LAST_SYNC, 0);
        mLastSyncSuccessTime = settings.getLong(KEY_LAST_SUCCESS, 0);
        mIsEnableAuto = settings.getBoolean(KEY_UPDATE_AUTO, true);
        mSyncTime = settings.getString(KEY_SYNC_TIME, "60");
        mPrimaryServerURL = settings.getString(KEY_PRIMARY_SERVER_URL, GoldtekApplication.getContext().getString(R.string.pref_default_server));
        mPrimaryServerAccount = settings.getString(KEY_PRIMARY_SERVER_ACCOUNT, "");
        mPrimaryServerPwd = settings.getString(KEY_PRIMARY_SERVER_PASSWORD, "");
    }

    public void recordSyncTime(boolean success) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        mLastSyncTime = System.currentTimeMillis();
        settings.edit().putLong(KEY_LAST_SYNC, mLastSyncTime).apply();
        if (success) {
            mLastSyncSuccessTime = mLastSyncTime;
            settings.edit().putLong(KEY_LAST_SUCCESS, mLastSyncTime).apply();
        }

        for(Observer cb : mObservers) cb.onConfigChange(KEY_LAST_SYNC, String.valueOf(success));
    }

    public void recordPrimaryServer(String url, String account, String pwd) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (url != null) {
            mPrimaryServerURL = url;
            settings.edit().putString(KEY_PRIMARY_SERVER_URL, url).apply();
        }
        if (account != null) {
            mPrimaryServerAccount = account;
            settings.edit().putString(KEY_PRIMARY_SERVER_ACCOUNT, account).apply();
        }
        if (pwd != null) {
            mPrimaryServerPwd = pwd;
            settings.edit().putString(KEY_PRIMARY_SERVER_PASSWORD, pwd).apply();
        }
        for(Observer cb : mObservers) cb.onConfigChange(KEY_PRIMARY_SERVER_URL, mPrimaryServerURL);
    }

    public void setConfig(String key, String value) {
        switch (key) {
            case KEY_UPDATE_AUTO:
                mIsEnableAuto = Boolean.parseBoolean(value);
                break;
            case KEY_SYNC_TIME:
                mSyncTime = value;
                break;
            case KEY_PRIMARY_SERVER_URL:
                mPrimaryServerURL = value;
                break;
        }
        for(Observer cb : mObservers) cb.onConfigChange(key, value);
    }

    public long getLastSuccessTime() {
        return mLastSyncSuccessTime;
    }

    public long getLastSyncTime() {
        return mLastSyncTime;
    }

    public String getLastSuccessTime(String format) {
        String date = new SimpleDateFormat(format).format(new Date(mLastSyncSuccessTime));
        return date;
    }

    public String getLastSyncTime(String format) {
        String date = new SimpleDateFormat(format).format(new Date(mLastSyncTime));
        return date;
    }

    public String getLastSuccessTime(SimpleDateFormat formatter) {
        String date = formatter.format(new Date(mLastSyncSuccessTime));
        return date;
    }

    public String getLastSyncTime(SimpleDateFormat formatter) {
        String date = formatter.format(new Date(mLastSyncTime));
        return date;
    }

    public boolean isAutoUpdate() {
        return mIsEnableAuto;
    }

    public int getSyncFrequency() {
        int time = 60;
        try {
            time = Integer.parseInt(mSyncTime);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            return time;
        }
    }

    public String getPrimaryServer() {
        Matcher matcher = IP_ADDRESS.matcher(mPrimaryServerURL);
        if (matcher.matches()) {
            return mPrimaryServerURL;
        }
        else return GoldtekApplication.getContext().getString(R.string.pref_default_server);
    }

    public String getPrimaryServerAccount() {
        return mPrimaryServerAccount;
    }

    public String getPrimaryServerPassword() {
        return mPrimaryServerPwd;
    }

    public String getPrimaryServerAuth() {
        return mPrimaryServerAccount + ":" +mPrimaryServerPwd;
    }

    public boolean needPrimaryServerAuth() { return (mPrimaryServerAccount != null && mPrimaryServerPwd != null); }

    private static final Pattern IP_ADDRESS
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))");

}
