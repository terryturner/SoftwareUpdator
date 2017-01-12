package com.goldtek.sw.updater.model;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.R;
import com.goldtek.sw.updater.data.GetRequest;
import com.goldtek.sw.updater.data.PmRequest;
import com.goldtek.sw.updater.data.Protocol;
import com.goldtek.sw.updater.data.GetResponse;
import com.goldtek.sw.updater.data.xml.XmlApplicationItem;
import com.goldtek.sw.updater.data.xml.MaintainItem;
import com.goldtek.sw.updater.presenter.ConfigManager;
import com.goldtek.sw.updater.presenter.PackageManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Terry on 2017/1/4.
 */

public class ScheduleHandler extends Handler implements HttpDownloader.IDownload {
    private static final int MINUTE2MILLI = 60000;
    private static final String XML_FILE = "sample.xml";

    public static final int POST_AUTO_UPDATE = 1;
    public static final int GET_XML_FILE = 2;
    public static final int PARSE_XML_FILE = 3;
    public static final int CHK_UPDATER_AVAILABLE = 4;
    public static final int GET_APK_FILE = 5;
    public static final int INSTALL_APK_FILE = 6;

    public interface Listener {
        void onPostExecute(GetResponse result);
        void onPackageInstall(String packageName, int returnCode);
    }

    private Context mContext;
    private Listener mListener = null;
    public void setListener(Listener l) {
        mListener = l;
    }

    public ScheduleHandler(Looper looper, Context context) {
        super(looper);
        mContext = context;
    }

    private IPackageInstallObserver mInstallObserver = new IPackageInstallObserver.Stub() {
        @Override
        public void packageInstalled(String packageName, int returnCode) throws RemoteException {
            if (returnCode != 1) {
                //TODO: error report
            } else {

            }
            if (mListener != null) mListener.onPackageInstall(packageName, returnCode);
        }
    };

    @Override
    public void handleMessage(Message msg) {
        GetResponse result;
        File file;
        switch (msg.what) {
            case POST_AUTO_UPDATE:
                ConfigManager.getInstance().queryConfig();
                if (ConfigManager.getInstance().isAutoUpdate()) {
                    int nextTime = ConfigManager.getInstance().getSyncFrequency();
                    sendEmptyMessageDelayed(GET_XML_FILE, nextTime * MINUTE2MILLI);
                }
                break;

            case GET_XML_FILE:
                ConfigManager.getInstance().queryConfig();
                String ip = ConfigManager.getInstance().getPrimaryServer();
                Protocol protocol = ConfigManager.getInstance().getPrimaryProtocol();

                if (ip != null) {
                    GetRequest request;
                    switch (protocol) {
                        case HTTP:
                            if (ConfigManager.getInstance().needPrimaryServerAuth()) {
                                request = new GetRequest(String.format(mContext.getString(R.string.url_http_auth_xml_format), ip), XML_FILE);
                                request.setOption(HttpDownloader.KEY_AUTH, ConfigManager.getInstance().getPrimaryServerAuth());
                            } else {
                                request = new GetRequest(String.format(mContext.getString(R.string.url_http_xml_format), ip), XML_FILE);
                            }
                            new HttpDownloader(this).execute(request);
                            break;
                        case HTTPS:
                            if (ConfigManager.getInstance().needPrimaryServerAuth()) {
                                request = new GetRequest(String.format(mContext.getString(R.string.url_https_auth_xml_format), ip), XML_FILE);
                                request.setOption(HttpDownloader.KEY_AUTH, ConfigManager.getInstance().getPrimaryServerAuth());
                            } else {
                                request = new GetRequest(String.format(mContext.getString(R.string.url_https_xml_format), ip), XML_FILE);
                            }
                            new HttpsDownloader(this).execute(request);
                            break;
                    }
                }

                break;
            case PARSE_XML_FILE:
                XmlParser parser = new XmlParser();
                if (msg.obj != null && msg.obj instanceof GetResponse) {
                    result = (GetResponse) msg.obj;
                    file = new File(result.FilePath);
                    try {
                        ConfigManager.getInstance().setMaintenance(parser.parse(new FileInputStream(file)));
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        //TODO: send mail while parse exception
                        ReportHandler.getInstance().writeMessage(String.format(mContext.getString(R.string.msg_schedule_parse_fail_format), ConfigManager.getInstance().getLastSuccessTime(GoldtekApplication.sDateFormat)));
                        ReportHandler.getInstance().writeException(e);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHK_UPDATER_AVAILABLE:
                for (MaintainItem item : ConfigManager.getInstance().getMaintenance()) {
                    if (item instanceof XmlApplicationItem) {
                        XmlApplicationItem app = (XmlApplicationItem) item;
                        if (app.isUpdater() && PackageManager.getInstance().isAvailableUpdate(app.getPackageName(), app.getVersionCode())) {
                            Message.obtain(this, GET_APK_FILE, app).sendToTarget();
                        }
                    }
                }
                break;
            case GET_APK_FILE:
                if (msg.obj != null && msg.obj instanceof XmlApplicationItem) {
                    XmlApplicationItem app = (XmlApplicationItem) msg.obj;
                    if (app.isValidPackageName()) {
                        GetRequest request = new GetRequest(app.getUrl().toString(), app.getPackageName() + ".apk");
                        request.setOption(ConfigManager.KEY_PACKAGE_NAME, app.getPackageName());
                        if (app.needAuthentication())
                            request.setOption(HttpDownloader.KEY_AUTH, app.getAuth());

                        if (XmlParser.isHttpUrl(app.getUrl().toString())) {
                            new HttpDownloader(this).execute(request);
                        } else if (XmlParser.isHttpsUrl(app.getUrl().toString())) {
                            new HttpsDownloader(this).execute(request);
                        }
                    }
                }
                break;
            case INSTALL_APK_FILE:
                if (msg.obj != null && msg.obj instanceof PmRequest) {
                    file = new File(((PmRequest) msg.obj).FilePath);

                    try {
                        PackageManager.getInstance().installPackage(file, 0x00000002, ((PmRequest) msg.obj).packageName, mInstallObserver);
                    } catch (Exception e) {
                        if (mListener != null) mListener.onPackageInstall(((PmRequest) msg.obj).packageName,
                                android.content.pm.PackageManager.PERMISSION_DENIED);
                    }
                }
                break;
            default:
                break;
        }
        super.handleMessage(msg);
    }

    @Override
    public void onProgressUpdate(int progress) {}

    @Override
    public void onPostExecute(GetResponse result) {
        if (mListener != null) mListener.onPostExecute(result);
    }


}
