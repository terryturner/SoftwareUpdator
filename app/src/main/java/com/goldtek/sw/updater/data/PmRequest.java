package com.goldtek.sw.updater.data;

/**
 * Created by Terry on 2017/1/12.
 */

public class PmRequest {
    public String packageName;
    public String FilePath;

    public PmRequest(String name, String path) {
        packageName = name;
        FilePath = path;
    }
}
