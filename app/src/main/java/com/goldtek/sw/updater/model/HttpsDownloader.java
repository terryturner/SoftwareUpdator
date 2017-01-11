package com.goldtek.sw.updater.model;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.data.Response;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsDownloader extends AsyncTask<String, String, Response> {
    public final static int ERROR_MalformedURLException = 1;
    public final static int ERROR_openConnection = 2;
    public final static int ERROR_connection = 3;
    public final static int ERROR_getResponseCode = 4;
    public final static int ERROR_getInputStream = 5;
    public final static int ERROR_saveFile = 6;

    public interface IDownload {
        void onProgressUpdate(int progress);
        void onPostExecute(Response result);
    }

    protected IDownload listener = null;
    public HttpsDownloader(IDownload listener) {
        this.listener = listener;
    }

    private final static String TAG = "HTTP_Download";

    /**
     * Downloading file in background thread
     * */
    @Override
    protected Response doInBackground(String... param) {
        Response result = new Response("");

        int count;
        String loginPassword = (param.length == 3) ? param[1] : null;
        result.fileName = (param.length == 3) ? param[2] : param[1];
        URL url = null;
        try {
            result.request = param[0];
            url = new URL(param[0]);
        } catch (MalformedURLException e) {
            result.code = ERROR_MalformedURLException;
            return result;
        }

        requestWithoutCA(result);



        //result.filePath = downloadPath;
        return result;

    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(String... progress) {
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

    public void requestWithoutCA(Response result) {
        try {

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[] { new MyTrustManager() }, new SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection
                    .setDefaultHostnameVerifier(new MyHostnameVerifier());

            //URL url = new URL("https://certs.cac.washington.edu/CAtest/");
            URL url = new URL("https://192.168.42.35/sample.xml");
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();

            InputStream in = urlConnection.getInputStream();
            // 取得输入流，并使用Reader读取
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));

            Log.i("terry", "=============================");
            Log.i("terry", "Contents of get request");
            Log.i("terry", "=============================");
            String lines;
            while ((lines = reader.readLine()) != null) {
                Log.i("terry", lines);
            }
            reader.close();
            // 断开连接
            urlConnection.disconnect();
            Log.i("terry", "=============================");
            Log.i("terry", "Contents of get request ends");
            Log.i("terry", "=============================");
            result.code = 200;
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result.code = ERROR_getResponseCode;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result.code = ERROR_getResponseCode;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result.code = ERROR_getResponseCode;
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            result.code = ERROR_getResponseCode;
        }
    }


    private class MyHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            return true;
        }

    }

    private class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // TODO Auto-generated method stub
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)

                throws CertificateException {
            // TODO Auto-generated method stub
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }

    }
}