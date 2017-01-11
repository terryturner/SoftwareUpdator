package com.goldtek.sw.updater.data;

/**
 * Created by Terry on 2017/1/4.
 */

public class Response {
    public Response(String request) {
        fileName = request;
    }
    public String request;
    public String fileName;
    public String packageName;
    public int code;
    public String filePath;
}
