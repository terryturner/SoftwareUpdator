package com.goldtek.sw.updater.model;

import android.content.pm.IPackageInstallObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.R;
import com.goldtek.sw.updater.data.Response;
import com.goldtek.sw.updater.data.xml.XmlApplicationItem;
import com.goldtek.sw.updater.data.xml.MaintainItem;
import com.goldtek.sw.updater.presenter.ConfigManager;
import com.goldtek.sw.updater.presenter.PackageManager;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Terry on 2017/1/4.
 */

public class ScheduleHandler extends Handler implements HttpDownloader.IDownload {
    private static final int MINUTE2MILLI = 60000;

    public static final int POST_AUTO_UPDATE = 1;
    public static final int GET_XML_FILE = 2;
    public static final int PARSE_XML_FILE = 3;
    public static final int CHK_UPDATER_AVAILABLE = 4;
    public static final int GET_APK_FILE = 5;
    public static final int INSTALL_APK_FILE = 6;

    public interface Listener {
        void onPostExecute(Response result);
        void onPackageInstall(String packageName, int returnCode);
    }

    //private HttpDownloader.IDownload mDownloadCB = null;
//    public void setHttpDownloadCallback(HttpDownloader.IDownload cb) {
//        mDownloadCB = cb;
//    }
    private Listener mListener = null;
    public void setListener(Listener l) {
        mListener = l;
    }

    public ScheduleHandler(Looper looper) {
        super(looper);
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
        Response result;
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

                if (ip != null) {
                    Uri uri = Uri.parse(String.format(GoldtekApplication.getContext().getString(R.string.url_http_main_format), ip));
                    if (ConfigManager.getInstance().needPrimaryServerAuth())
                        new HttpDownloader(this).execute(uri.toString(), ConfigManager.getInstance().getPrimaryServerAuth(), "sample.xml");
                    else
                        new HttpDownloader(this).execute(uri.toString(), "sample.xml");
                }

                break;
            case PARSE_XML_FILE:
                XmlParser parser = new XmlParser();
                if (msg.obj != null && msg.obj instanceof Response) {
                    result = (Response) msg.obj;
                    file = new File(result.filePath);
                    try {
                        ConfigManager.getInstance().setMaintenance(parser.parse(new FileInputStream(file)));
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //TODO: error parse report
                break;
            case CHK_UPDATER_AVAILABLE:
                for (MaintainItem item : ConfigManager.getInstance().getMaintenance()) {
                    if (item instanceof XmlApplicationItem) {
                        XmlApplicationItem app = (XmlApplicationItem) item;
                        if (app.isUpdater() && PackageManager.getInstance().isAvailableUpdate(app.getPackageName(), app.getVersionCode())) {
                            if (XmlParser.isValidUrl(app.getUrl().toString()))
                                new ApkDownloader(this, app.getPackageName()).execute(app.getUrl().toString(), app.getPackageName() + ".apk");
                        }
                    }
                }
                break;
            case GET_APK_FILE:
                if (msg.obj != null && msg.obj instanceof XmlApplicationItem) {
                    XmlApplicationItem app = (XmlApplicationItem) msg.obj;
                    if (XmlParser.isValidUrl(app.getUrl().toString()))
                        new ApkDownloader(this, app.getPackageName()).execute(app.getUrl().toString(), app.getPackageName() + ".apk");
                }
                break;
            case INSTALL_APK_FILE:
                if (msg.obj != null && msg.obj instanceof Response) {
                    result = (Response) msg.obj;
                    file = new File(result.filePath);
                    try {
                        PackageManager.getInstance().installPackage(file, 0x00000002, result.packageName, mInstallObserver);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (mListener != null) mListener.onPackageInstall(result.packageName, android.content.pm.PackageManager.PERMISSION_DENIED);
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
    public void onPostExecute(Response result) {
        if (mListener != null) mListener.onPostExecute(result);
    }
}
