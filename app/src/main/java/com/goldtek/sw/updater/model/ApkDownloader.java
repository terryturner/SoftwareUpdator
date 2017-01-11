package com.goldtek.sw.updater.model;

import com.goldtek.sw.updater.data.Response;

/**
 * Created by Terry on 2017/1/4.
 */

public class ApkDownloader extends HttpDownloader {
    private String mPackageName;
    public ApkDownloader(IDownload listener, String packageName) {
        super(listener);
        mPackageName = packageName;
    }

    @Override
    protected void onPostExecute(Response result)
    {
        result.packageName = mPackageName;
        listener.onPostExecute(result);
    }
}
