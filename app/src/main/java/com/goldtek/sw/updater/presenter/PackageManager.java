package com.goldtek.sw.updater.presenter;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.util.Log;

import com.goldtek.sw.updater.GoldtekApplication;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Terry on 2017/1/3.
 */

public class PackageManager {
    private static PackageManager sInstance = new PackageManager(GoldtekApplication.getContext());
    public static PackageManager getInstance() {
        return sInstance;
    }

    private android.content.pm.PackageManager mAndroidPM = null;
    private List<PackageInfo> mPackageList = new ArrayList<>();

    private PackageManager(Context context) {
        mAndroidPM = context.getPackageManager();
        queryPackageList();
    }

    public void queryPackageList() {
        mPackageList = mAndroidPM.getInstalledPackages(0);
    }

    public boolean isInstalled(String packageName) {
        for(PackageInfo info : mPackageList) {
            if (info.packageName.equals(packageName)) return true;
        }
        return false;
    }

    public int getInstalledVersionCode(String packageName) {
        for(PackageInfo info : mPackageList) {
            if (info.packageName.equals(packageName)) {
                return info.versionCode;
            }
        }
        return -1;
    }

    public boolean isAvailableUpdate(String packageName, int versionCode) {
        int installedCode = getInstalledVersionCode(packageName);
        return (installedCode > 0) && (versionCode > installedCode);
    }

    public void installPackage(File file, int flag, String packageName, IPackageInstallObserver observer) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        if (file == null || !file.exists() || packageName == null)
            return;

        Method md = PackageManager.class.getDeclaredMethod(
                "installPackage", new Class[] { android.net.Uri.class,
                        android.content.pm.IPackageInstallObserver.class,
                        int.class, java.lang.String.class });
        md.invoke(mAndroidPM, new Object[] { Uri.fromFile(file), observer, flag, packageName });
    }

    public PackageInfo getApplicationInfo() {
        try {
            return mAndroidPM.getPackageInfo(GoldtekApplication.getContext().getPackageName(), 0);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
