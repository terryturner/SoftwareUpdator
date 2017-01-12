package com.goldtek.sw.updater.Preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class MaintenancePreference extends PopupPreference {
    private List<MaintainItem> mMaintenance;
    private ListView mListView;

    public MaintenancePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MaintenancePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        mListView = new ListView(getContext());
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        mMaintenance = ConfigManager.getInstance().getMaintenance();
        mListView.setAdapter(new ArrayAdapter<MaintainItem>(getContext(), 0, mMaintenance){
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @Override
            public View getView(int position,View convertView,ViewGroup parent){
                View view = convertView==null ? inflater.inflate(R.layout.info_maintenance_item, null) : convertView;
                TextView tv = (TextView)view.findViewById(R.id.rowText);

                MaintainItem item = mMaintenance.get(position);
                StringBuilder msg = new StringBuilder();

                if (item.isApplicationClass()) {
                    XmlApplicationItem app = (XmlApplicationItem) item;

                    msg.append(String.format(getContext().getString(R.string.dialog_info_app_format),
                            app.getPackageName(), app.getVersionCode(),
                            GoldtekApplication.sDateFormat.format(app.getDeployTime()),
                            app.getUrl().toString(),
                            app.getAuthAccount(), app.getAuthPassword()));
                    tv.setText(msg);
                } else if (item.isSettingClass()) {
                    XmlSettingItem setting = (XmlSettingItem) item;

                    msg.append(String.format(getContext().getString(R.string.dialog_info_setting_format),
                            setting.getSender(), setting.getPassword()));
                    for (String address : setting.getReceivers()) {
                        msg.append(address + "\n");
                    }
                    tv.setText(msg.substring(0,msg.lastIndexOf("\n")));
                }
                return view;
            }
        });

        layout.addView(mListView);

        return layout;
    }
}
