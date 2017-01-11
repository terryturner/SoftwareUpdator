package com.goldtek.sw.updater.model;

import android.os.AsyncTask;
import android.util.Log;

import com.goldtek.sw.updater.data.Mail;
import com.goldtek.sw.updater.data.Response;

/**
 * Created by Terry on 2017/1/6.
 */

public class MailSender extends AsyncTask<Mail, String, Response> {
    @Override
    protected Response doInBackground(Mail... param) {
        Response result = new Response("");
        if (param.length == 1) {
            try {
                boolean i= param[0].send();
                if(i==true){
                    result.code = 1;
                }
                else
                {
                    result.code = 0;
                }

            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return result;
    }
}
