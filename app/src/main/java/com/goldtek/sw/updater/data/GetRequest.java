package com.goldtek.sw.updater.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Terry on 2017/1/12.
 */

public class GetRequest {
    public String RequestURL;
    public String FileName;
    protected Map<String, String> mMap = new HashMap<>();

    public GetRequest(String url, String dstFile){
        RequestURL = url;
        FileName = dstFile;
    }

    public void setOption(String key, String value) {
        mMap.put(key, value);
    }

    public String getOption(String key) {
        return mMap.get(key);
    }
}
