package com.goldtek.sw.updater.data;

import java.net.HttpURLConnection;

/**
 * Created by Terry on 2017/1/4.
 */

public class GetResponse {
    public int Code;
    public String FilePath;
    public GetRequest Request;

    public GetResponse(GetRequest request) {
        Request = request;
    }
    public boolean isHttpOK() {
        return Code == HttpURLConnection.HTTP_OK;
    }
}
