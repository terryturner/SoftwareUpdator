package com.goldtek.sw.updater.model;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Xml;
import android.webkit.URLUtil;

import com.goldtek.sw.updater.GoldtekApplication;
import com.goldtek.sw.updater.data.xml.XmlApplicationItem;
import com.goldtek.sw.updater.data.xml.XmlSettingItem;
import com.goldtek.sw.updater.data.xml.MaintainItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Terry on 2017/1/3.
 */

public class XmlParser {
    private static final String ns = null;
    private static final String TAG_Validate = "Validate";
    private static final String TAG_Setting = "Setting";
    private static final String TAG_MailFrom = "MailFrom";
    private static final String TAG_MailTo = "MailTo";
    private static final String TAG_mail = "mail";
    private static final String TAG_account = "account";
    private static final String TAG_password = "password";
    private static final String TAG_Application = "Application";
    private static final String TAG_packageName = "packageName";
    private static final String TAG_versionCode = "versionCode";
    private static final String TAG_deployTime = "deployTime";
    private static final String TAG_url = "url";


    public List<MaintainItem> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readValidate(parser);
        } finally {
            in.close();
        }
    }

    private List readValidate(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, TAG_Validate);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(TAG_Setting)) {
                entries.add(readSetting(parser));
            } else if (name.equals(TAG_Application)) {
                entries.add(readApplication(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private MaintainItem readSetting(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, TAG_Setting);
        XmlSettingItem item = new XmlSettingItem();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TAG_MailTo)) {
                for (String address : readMailTo(parser))
                    item.addReceiver(address);
            } else if (name.equals(TAG_MailFrom)) {
                readMailFrom(parser, item);
            } else {
                skip(parser);
            }
        }
        return item;
    }

    private List<String> readMailTo(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, TAG_MailTo);
        List mailList = new ArrayList();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TAG_mail)) {
                String value = parser.nextText();
                if (isValidEmail(value)) mailList.add(value);
            } else {
                skip(parser);
            }
        }
        return mailList;
    }

    private void readMailFrom(XmlPullParser parser, XmlSettingItem item) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, TAG_MailFrom);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TAG_account)) {
                String value = parser.nextText();
                if (isValidEmail(value)) item.setSender(value);
            } else if (name.equals(TAG_password)) {
                String value = parser.nextText();
                item.setSenderPassword(value);
            } else {
                skip(parser);
            }
        }
    }

    private MaintainItem readApplication(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, TAG_Application);
        XmlApplicationItem item = new XmlApplicationItem();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TAG_packageName)) {
                String value = parser.nextText();
                item.setPackageName(value);
            } else if (name.equals(TAG_versionCode)) {
                String value = parser.nextText();
                try {
                    item.setVersionCode(Integer.parseInt(value));
                } catch (NumberFormatException e) {}
            } else if (name.equals(TAG_deployTime)) {
                String value = parser.nextText();
                try {
                    Date date = GoldtekApplication.sDateFormat.parse(value);
                    item.setDeployTime(date);
                } catch (ParseException e) {}
            } else if (name.equals(TAG_url)) {
                String value = parser.nextText();
                if (isValidUrl(value)) item.setURL(Uri.parse(value));
            } else if (name.equals(TAG_account)) {
                String value = parser.nextText();
                item.setAuthAccount(value);
            } else if (name.equals(TAG_password)) {
                String value = parser.nextText();
                item.setAuthPassword(value);
            } else {
                skip(parser);
            }
        }
        return item;
    }


    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public final static boolean isValidUrl(String target) {
        return URLUtil.isHttpUrl(target) || URLUtil.isHttpsUrl(target);
    }

    public final static boolean isHttpUrl(String target) {
        return URLUtil.isHttpUrl(target);
    }

    public final static boolean isHttpsUrl(String target) {
        return URLUtil.isHttpsUrl(target);
    }
}
