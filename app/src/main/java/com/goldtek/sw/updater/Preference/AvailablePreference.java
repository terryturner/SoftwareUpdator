package com.goldtek.sw.updater.Preference;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.goldtek.sw.updater.R;
import com.goldtek.sw.updater.data.GetResponse;
import com.goldtek.sw.updater.data.PmRequest;
import com.goldtek.sw.updater.data.xml.MaintainItem;
import com.goldtek.sw.updater.data.xml.XmlApplicationItem;
import com.goldtek.sw.updater.model.ReportHandler;
import com.goldtek.sw.updater.model.ScheduleHandler;
import com.goldtek.sw.updater.model.XmlParser;
import com.goldtek.sw.updater.presenter.ConfigManager;
import com.goldtek.sw.updater.presenter.PackageManager;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terry on 2017/1/9.
 */

public class AvailablePreference extends PopupPreference implements AdapterView.OnItemClickListener, ScheduleHandler.Listener {
    private List<XmlApplicationItem> mApplications = new ArrayList<>();
    private ListView mListView;
    private ProgressDialog mProgress;
    private Toast mToastWord;

    private HandlerThread mExecuteThread = null;
    private ScheduleHandler mExecutor = null;

    public AvailablePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AvailablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        updateAvailableList();

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        mListView = new ListView(getContext());
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        mListView.setAdapter(new ArrayAdapter<XmlApplicationItem>(getContext(), 0, mApplications){
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @Override
            public View getView(int position,View convertView,ViewGroup parent){
                View view = convertView==null ? inflater.inflate(R.layout.info_update_item, null) : convertView;
                TextView tv = (TextView)view.findViewById(R.id.rowText);

                XmlApplicationItem app = mApplications.get(position);
                String msg = String.format("%s (%d)", app.getPackageName(), app.getVersionCode());
                tv.setText(msg);

                return view;
            }
        });
        mListView.setOnItemClickListener(this);

        layout.addView(mListView);

        return layout;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        mExecuteThread = new HandlerThread("executor");
        mExecuteThread.start();
        mExecutor = new ScheduleHandler(mExecuteThread.getLooper(), getContext());
        mExecutor.setListener(this);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        mExecutor.removeCallbacksAndMessages(null);
        mExecutor = null;
        mExecuteThread.quit();
        mExecuteThread.interrupt();
        mExecuteThread = null;
    }

    private void updateAvailableList() {
        mApplications.clear();
        for (MaintainItem item : ConfigManager.getInstance().getMaintenance()) {
            if (item.isApplicationClass()) {
                XmlApplicationItem app = (XmlApplicationItem) item;
                if (PackageManager.getInstance().isAvailableUpdate(app.getPackageName(), app.getVersionCode())
                        && app.isValidPackageName() && XmlParser.isValidUrl(app.getUrl().toString()))
                    mApplications.add(app);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BaseAdapter adapter = (BaseAdapter)parent.getAdapter();
        XmlApplicationItem app = (XmlApplicationItem)adapter.getItem(position);

        mProgress = ProgressDialog.show(getContext(),
                getContext().getString(R.string.dialog_update_title), getContext().getString(R.string.dialog_update_msg), true, false);
        Message.obtain(mExecutor, ScheduleHandler.GET_APK_FILE, app).sendToTarget();
    }

    @Override
    public void onPostExecute(GetResponse result) {
        Log.i("terry", result.Code + " : " + result.Request.FileName);
        if (result.isHttpOK()) {
            PmRequest request = new PmRequest(result.Request.getOption(ConfigManager.KEY_PACKAGE_NAME), result.FilePath);
            Message.obtain(mExecutor, ScheduleHandler.INSTALL_APK_FILE, request).sendToTarget();
        } else {
            mProgress.dismiss();

            showResult(String.format(getContext().getString(R.string.dialog_download_fail_format), result.Code, result.Request.getOption(ConfigManager.KEY_PACKAGE_NAME)));
            ReportHandler.getInstance().writeMessageFormat(R.string.msg_user_sync_fail_format, result.Code, result.Request.RequestURL);
        }
    }

    @Override
    public void onPackageInstall(String packageName, int returnCode) {
        ReportHandler.getInstance().writeMessageFormat(R.string.msg_user_install_format, returnCode, packageName);
        mProgress.dismiss();
        if (returnCode == 1)
            showResult(String.format(getContext().getString(R.string.dialog_install_success_format), packageName));
        else
            showResult(String.format(getContext().getString(R.string.dialog_install_fail_format), packageName, returnCode));
    }

    private void showResult(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(msg);
        builder.setNeutralButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });
        builder.show();
    }
}
