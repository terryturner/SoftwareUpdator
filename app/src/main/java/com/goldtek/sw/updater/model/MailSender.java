package com.goldtek.sw.updater.model;

import android.os.AsyncTask;

import com.goldtek.sw.updater.data.Mail;
import com.goldtek.sw.updater.data.GetResponse;

/**
 * Created by Terry on 2017/1/6.
 */

public class MailSender extends AsyncTask<Mail, String, GetResponse> {
    @Override
    protected GetResponse doInBackground(Mail... param) {
        GetResponse result = new GetResponse(null);
        if (param.length == 1) {
            try {
                boolean i= param[0].send();
                if(i==true){
                    result.Code = 1;
                }
                else
                {
                    result.Code = 0;
                }

            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return result;
    }
}
