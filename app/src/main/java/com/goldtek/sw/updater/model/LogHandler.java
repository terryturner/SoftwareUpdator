package com.goldtek.sw.updater.model;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Terry on 2017/1/9.
 */

public abstract class LogHandler {
    abstract void writeLog(StringBuffer buffer);
    protected SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    protected void checkFile(String path) {
        File file = new File(path);

        if (file.length() > 20000000) {
            file.delete();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    protected String getCurrentTime() {
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    protected String addSeparationLine(int length) {
        StringBuffer separationLine = new StringBuffer();

        for (int i = 0; i < length; i++) {
            separationLine.append("=");
        }

        return separationLine.toString();
    }

    protected String addLineWrapSymbol(int count) {
        StringBuffer lineWrapSymbol = new StringBuffer();

        for (int i = 0; i < count; i++) {
            lineWrapSymbol.append("\n");
        }

        return lineWrapSymbol.toString();
    }
}
