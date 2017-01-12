package com.goldtek.sw.updater.model;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.data.GetRequest;
import com.goldtek.sw.updater.data.GetResponse;

public class HttpDownloader extends AsyncTask<GetRequest, String, GetResponse> {
    public final static int ERROR_MalformedURLException = 1;
    public final static int ERROR_openConnection = 2;
    public final static int ERROR_connection = 3;
    public final static int ERROR_getResponseCode = 4;
    public final static int ERROR_getInputStream = 5;
    public final static int ERROR_saveFile = 6;
    public final static int ERROR_NoSuchAlgorithmException = 7;
    public final static int ERROR_KeyManagementException = 8;

    public final static String KEY_AUTH = "loginPassword";
    protected final static String TAG = "HTTP_Download";

    public interface IDownload {
        void onProgressUpdate(int progress);
        void onPostExecute(GetResponse result);
    }

    protected IDownload listener = null;
    public HttpDownloader(IDownload listener) {
        this.listener = listener;
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected GetResponse doInBackground(GetRequest... param) {
        if (param.length != 1) return new GetResponse(null);

        GetRequest request = param[0];
        GetResponse response = new GetResponse(request);

        int count;

        String loginPassword = request.getOption(KEY_AUTH);
        response.FilePath = GoldtekApplication.getContext().getFilesDir() + "/" + request.FileName;

        URL url = null;
        try {
            url = new URL(request.RequestURL);
        } catch (MalformedURLException e) {
            response.Code = ERROR_MalformedURLException;
            return response;
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            response.Code = ERROR_openConnection;
            return response;
        }

        if (loginPassword != null) {
            String encode = new String(Base64.encode(loginPassword.getBytes(), Base64.NO_WRAP | Base64.URL_SAFE));
            connection.setRequestProperty ("Authorization", "Basic " + encode);
        }
        //connection.setRequestProperty("Accept-Encoding", "identity");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        try {
            connection.connect();
        } catch (IOException e) {
            response.Code = ERROR_connection;
            return response;

        }

        try {
            response.Code = connection.getResponseCode();
        } catch (IOException e) {
            response.Code = ERROR_getResponseCode;
            connection.disconnect();
            return response;
        }

        if (!response.isHttpOK()) {
            Log.i(TAG, "fail code: " + response.Code);
            connection.disconnect();
            return response;
        }

        // this will be useful so that you can show a tipical 0-100% progress bar
        int lengthOfFile = connection.getContentLength();

        // download the file
        InputStream input = null;
        try {
            input = new BufferedInputStream(connection.getInputStream(), 8192);
        } catch (IOException e) {
            response.Code = ERROR_getInputStream;
            connection.disconnect();
            return response;
        }

        // Output stream
        OutputStream output = null;
        try {
            output = new FileOutputStream(response.FilePath);

            byte data[] = new byte[8192];
            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                //publishProgress("" + (int) ((total * 100) / lengthOfFile));

                output.write(data, 0, count);
            }

            output.flush();

            output.close();
            input.close();

        } catch (IOException e) {
            response.Code = ERROR_saveFile;
            return response;
        } finally {
            connection.disconnect();
        }

        return response;

    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(String... progress) {
        //Log.i("terry", "progress: " + progress[0] + "%");
        if (listener != null)
            listener.onProgressUpdate(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(GetResponse result)
    {
        if (listener != null)
            listener.onPostExecute(result);
    }

}