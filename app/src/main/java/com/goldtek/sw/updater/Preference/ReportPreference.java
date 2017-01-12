package com.goldtek.sw.updater.Preference;

import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.R;
import com.goldtek.sw.updater.data.Mail;
import com.goldtek.sw.updater.data.xml.MaintainItem;
import com.goldtek.sw.updater.data.xml.XmlSettingItem;
import com.goldtek.sw.updater.model.MailSender;
import com.goldtek.sw.updater.model.ReportHandler;
import com.goldtek.sw.updater.presenter.ConfigManager;
import com.goldtek.sw.updater.presenter.PackageManager;

import java.util.Date;

/**
 * Created by Terry on 2017/1/9.
 */

public class ReportPreference extends PopupPreference implements DialogInterface.OnClickListener {
    public ReportPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ReportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();
        String msg = String.format(getContext().getString(R.string.dialog_log_handle_format), ReportHandler.getInstance().humanReadableByteCount());
        setDialogMessage(msg);
        return view;
    }

    @Override
    public void onClick(DialogInterface dialog, int which){
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (PackageManager.getInstance().getApplicationInfo() != null) {
                String msg = String.format(getContext().getString(R.string.msg_user_report_format), PackageManager.getInstance().getApplicationInfo().versionCode);
                ReportHandler.getInstance().writeMessage(msg);
            }
            XmlSettingItem setting = null;
            for (MaintainItem item : ConfigManager.getInstance().getMaintenance()) {
                if (item.isSettingClass()) {
                    setting = (XmlSettingItem) item;
                    break;
                }
            }

            if (setting.isValidSender()) {
                Mail mail = new Mail(setting.getSender(), setting.getPassword());
                mail.set_from(setting.getSender());
                mail.set_subject("User Report");
                mail.setBody(GoldtekApplication.sDateFormat.format(new Date(System.currentTimeMillis())));
                mail.set_to(setting.getReceivers().toArray(new String[0]));
                try {
                    mail.addAttachment(ReportHandler.getInstance().getPath(), "log.txt");
                    mail.addAttachment(GoldtekApplication.getContext().getFilesDir()+"/sample.xml", "main.xml");
                } catch (Exception e) {}
                new MailSender().execute(mail);
            } else {
                Toast.makeText(getContext(), "Report fail", Toast.LENGTH_LONG).show();
                ReportHandler.getInstance().writeMessage(String.format(getContext().getString(R.string.msg_user_report_without_sender_format), setting.getSender(), setting.getPassword()));
            }

        }else if(which == DialogInterface.BUTTON_NEGATIVE){
            ReportHandler.getInstance().clearFile();
        }
    }
}
