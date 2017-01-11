package com.goldtek.sw.updater.model;

import android.os.AsyncTask;
import android.util.Log;

import com.goldtek.sw.updater.GoldtekApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Terry on 2017/1/9.
 */

public class ReportHandler extends LogHandler {
    private final static String FILE_NAME = "report.log";
    private final static String PATH = GoldtekApplication.getContext().getFilesDir().getPath();

    private static ReportHandler sInstance = new ReportHandler();
    public static ReportHandler getInstance() { return sInstance; }

    private ReportHandler() {
        checkFile(PATH+"/"+FILE_NAME);
    }

    public String getPath() {
        return PATH+"/"+FILE_NAME;
    }

    public long getSize() {
        File file = new File(PATH+"/"+FILE_NAME);
        if (file.exists()) return file.length();
        else return 0;
    }

    public void clearFile() {
        File file = new File(PATH+"/"+FILE_NAME);
        file.delete();
    }

    public void writeMessageFormat(int resId, Object... args) {
        String msg = GoldtekApplication.getContext().getString(resId);
        if (msg != null) writeMessage(String.format(msg, args));
    }

    public void writeMessage(int resId) {
        String msg = GoldtekApplication.getContext().getString(resId);
        if (msg != null) writeMessage(msg);
    }

    public void writeMessage(String msg) {
        StringBuffer message = new StringBuffer();

        message.append(msg);
        message.append(addLineWrapSymbol(1));

        new ReportTask().execute(message);
    }

    public void writeException(Throwable ex) {
        StringBuffer message = new StringBuffer();

        message.append("Exception: " + ex.getMessage());
        message.append(addLineWrapSymbol(1));
        message.append(addLineWrapSymbol(1));

        StackTraceElement[] elements = ex.getStackTrace();

        for (int i = 0; i < elements.length; i++) {
            message.append(elements[i].toString());
            message.append(addLineWrapSymbol(1));
        }

        new ReportTask().execute(message);
    }

    @Override
    void writeLog(StringBuffer buffer) {
        try {
            FileWriter dataFile = null;
            dataFile = new FileWriter(PATH+"/"+FILE_NAME, true);

            StringBuffer log = new StringBuffer();

            log.append(addLineWrapSymbol(1));
            log.append("Date " + getCurrentTime());
            log.append(addLineWrapSymbol(1));
            log.append(buffer.toString());
            log.append(addLineWrapSymbol(1));
            log.append(addSeparationLine(70));
            log.append(addLineWrapSymbol(1));

            BufferedWriter input = new BufferedWriter(dataFile);
            input.write(log.toString());
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ReportTask extends AsyncTask<StringBuffer, Integer, String> {
        @Override
        protected String doInBackground(StringBuffer... params) {
            if (params[0] != null) {
                writeLog(params[0]);
            }
            return "";
        }
    }
}
