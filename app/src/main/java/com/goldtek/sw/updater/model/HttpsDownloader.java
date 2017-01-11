package com.goldtek.sw.updater.model;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.data.Response;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

public class HttpsDownloader extends HttpDownloader {

    public HttpsDownloader(IDownload listener) {
        super(listener);
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected Response doInBackground(String... param) {
        Response result = new Response("");
        //requestWithoutCA(result);
        //saveFileWithoutCA(result);
        result = saveFileWithoutCAWithAuth(result, param);

        return result;
    }

    /**
     * Print https input stream without CA
     */
    private void requestWithoutCA(Response result) {
        try {

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[] { new FreeX509Manager() }, new SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection
                    .setDefaultHostnameVerifier(new FreeHostnameVerifier());

            //URL url = new URL("https://certs.cac.washington.edu/CAtest/");
            URL url = new URL("https://192.168.42.35/sample.xml");
            HttpURLConnection urlConnection = (HttpURLConnection) url
                    .openConnection();

            InputStream in = urlConnection.getInputStream();
            // 取得输入流，并使用Reader读取
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));

            String lines;
            while ((lines = reader.readLine()) != null) {
                //Log.i("terry", lines);
            }
            reader.close();
            // 断开连接
            urlConnection.disconnect();
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

    /**
     * Save https input stream to a file without CA
     */
    private Response saveFileWithoutCA(Response result) {
        URL url = null;
        try {
            result.request = "https://192.168.42.35/sample.xml";
            url = new URL(result.request);
        } catch (MalformedURLException e) {
            result.code = ERROR_MalformedURLException;
            return result;
        }

        //requestWithoutCA(result);
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            result.code = ERROR_NoSuchAlgorithmException;
            return result;
        }

        try {
            sc.init(null, new TrustManager[] { new FreeX509Manager() }, new SecureRandom());
        } catch (KeyManagementException e) {
            result.code = ERROR_KeyManagementException;
            return result;
        }

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new FreeHostnameVerifier());
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            result.code = ERROR_openConnection;
            return result;
        }

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);


        // Input stream
        InputStream input = null;
        BufferedReader reader = null;
        try {
            input = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException e) {
            result.code = ERROR_getInputStream;
            connection.disconnect();
            return result;
        }

        // Output stream
        String downloadPath = GoldtekApplication.getContext().getFilesDir() + "/https.xml";
        PrintWriter output = null;
        try {
            output = new PrintWriter(new FileWriter(downloadPath));

            String l;
            while ((l = reader.readLine()) != null) {
                output.println(l);
            }

            output.close();
            reader.close();
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
     * Save https input stream to a file without CA but with basic authentication
     */
    private Response saveFileWithoutCAWithAuth(Response result, String... param) {
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

        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            result.code = ERROR_NoSuchAlgorithmException;
            return result;
        }

        try {
            sc.init(null, new TrustManager[] { new FreeX509Manager() }, new SecureRandom());
        } catch (KeyManagementException e) {
            result.code = ERROR_KeyManagementException;
            return result;
        }

        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new FreeHostnameVerifier());
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
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

        // Input stream
        InputStream input = null;
        BufferedReader reader = null;
        try {
            input = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException e) {
            result.code = ERROR_getInputStream;
            connection.disconnect();
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

        // Output stream
        PrintWriter output = null;
        try {
            output = new PrintWriter(new FileWriter(downloadPath));
            String line;
            long total = 0;

            while ((line = reader.readLine()) != null) {
                total += line.length();
                //publishProgress("" + (int) ((total * 100) / lengthOfFile));
                output.println(line);
            }

            output.close();
            reader.close();
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

    private class FreeHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }

    private class FreeX509Manager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)

                throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }
}