package com.goldtek.sw.updater.model;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.data.Response;

public class HttpDownloader extends AsyncTask<String, String, Response> {
    public final static int ERROR_MalformedURLException = 1;
    public final static int ERROR_openConnection = 2;
    public final static int ERROR_connection = 3;
    public final static int ERROR_getResponseCode = 4;
    public final static int ERROR_getInputStream = 5;
    public final static int ERROR_saveFile = 6;
    public final static int ERROR_NoSuchAlgorithmException = 7;
    public final static int ERROR_KeyManagementException = 8;

    protected final static String TAG = "HTTP_Download";

    public interface IDownload {
        void onProgressUpdate(int progress);
        void onPostExecute(Response result);
    }

    protected IDownload listener = null;
    public HttpDownloader(IDownload listener) {
        this.listener = listener;
    }


    /**
     * Downloading file in background thread
     * */
    @Override
    protected Response doInBackground(String... param) {
        Response result = new Response("");

        int count;
        String loginPassword = (param.length == 3) ? param[1] : null;
        result.fileName = (param.length == 3) ? param[2] : param[1];
        String downloadPath = GoldtekApplication.getContext().getFilesDir() + "/" + result.fileName;
        result.filePath = downloadPath;

        URL url = null;
        try {
            result.request = param[0];
            url = new URL(result.request);
        } catch (MalformedURLException e) {
            result.code = ERROR_MalformedURLException;
            return result;
        }

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            result.code = ERROR_openConnection;
            return result;
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
            result.code = ERROR_connection;
            return result;

        }

        try {
            result.code = connection.getResponseCode();
        } catch (IOException e) {
            result.code = ERROR_getResponseCode;
            connection.disconnect();
            return result;
        }

        if (result.code != HttpURLConnection.HTTP_OK) {
            Log.i(TAG, "fail code: " + result.code);
            connection.disconnect();
            return result;
        }

        // this will be useful so that you can show a tipical 0-100% progress bar
        int lengthOfFile = connection.getContentLength();

        // download the file
        InputStream input = null;
        try {
            input = new BufferedInputStream(connection.getInputStream(), 8192);
        } catch (IOException e) {
            result.code = ERROR_getInputStream;
            connection.disconnect();
            return result;
        }

        // Output stream
        //String downloadPath = Environment.getExternalStorageDirectory() + "/Download/" + result.fileName;
        OutputStream output = null;
        try {
            output = new FileOutputStream(downloadPath);

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

        } catch (FileNotFoundException e) {
            result.code = ERROR_saveFile;
            return result;
        } catch (IOException e) {
            result.code = ERROR_saveFile;
            return result;
        } finally {
            connection.disconnect();
        }

        return result;

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
    protected void onPostExecute(Response result)
    {
        if (listener != null)
            listener.onPostExecute(result);
    }

}