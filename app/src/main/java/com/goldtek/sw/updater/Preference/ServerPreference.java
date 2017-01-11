package com.goldtek.sw.updater.Preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.R;
import com.goldtek.sw.updater.data.xml.MaintainItem;
import com.goldtek.sw.updater.data.xml.XmlApplicationItem;
import com.goldtek.sw.updater.data.xml.XmlSettingItem;
import com.goldtek.sw.updater.presenter.ConfigManager;

import java.util.List;

/**
 * Created by Terry on 2017/1/3.
 */

public class ServerPreference extends PopupPreference {

    public ServerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ServerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        setDialogLayoutResource(R.layout.config_server);
        setPersistent(false);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ((EditText)view.findViewById(R.id.edit_server_address)).setText(ConfigManager.getInstance().getPrimaryServer());
        ((EditText)view.findViewById(R.id.edit_server_account)).setText(ConfigManager.getInstance().getPrimaryServerAccount());
        ((EditText)view.findViewById(R.id.edit_server_password)).setText(ConfigManager.getInstance().getPrimaryServerPassword());
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (which == DialogInterface.BUTTON_POSITIVE) {

            ConfigManager.getInstance().recordPrimaryServer(
                    ((EditText)getDialog().findViewById(R.id.edit_server_address)).getText().toString(),
                    ((EditText)getDialog().findViewById(R.id.edit_server_account)).getText().toString(),
                    ((EditText)getDialog().findViewById(R.id.edit_server_password)).getText().toString());
        }
    }
}
