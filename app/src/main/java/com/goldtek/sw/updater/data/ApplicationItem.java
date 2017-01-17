package com.goldtek.sw.updater.data;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by Terry on 2017/1/16 0016.
 * For 3rd party used.
 */

public class ApplicationItem implements Parcelable{
    private String packageName = "";
    private int versionCode = -1;
    private long deployTime = 0;

    protected ApplicationItem(Parcel in) {
        packageName = in.readString();
        versionCode = in.readInt();
        deployTime  = in.readLong();
    }

    public ApplicationItem(String name, int code, long time) {
        packageName = name;
        versionCode = code;
        deployTime  = time;
    }

    public static final Creator<ApplicationItem> CREATOR = new Creator<ApplicationItem>() {
        @Override
        public ApplicationItem createFromParcel(Parcel in) {
            return new ApplicationItem(in);
        }

        @Override
        public ApplicationItem[] newArray(int size) {
            return new ApplicationItem[size];
        }
    };

    public String getPackageName() { return packageName; }
    public int getVersionCode() { return versionCode; }
    public long getDeployTime() { return deployTime; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(packageName);
        parcel.writeInt(versionCode);
        parcel.writeLong(deployTime);
    }
}
