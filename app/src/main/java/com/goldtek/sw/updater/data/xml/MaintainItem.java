package com.goldtek.sw.updater.data.xml;

/**
 * Created by Terry on 2017/1/3.
 */

public abstract class MaintainItem {
    private int classID = 0;
    public MaintainItem(int id) { classID = id; }

    public int getClassID() {
        return classID;
    }
    public boolean isSettingClass() { return classID == 1; }
    public boolean isApplicationClass() { return classID == 2; }

    public abstract void debug();
}
