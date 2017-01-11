package com.goldtek.sw.updater.Preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by Terry on 2017/1/3.
 */

public class PopupPreference extends DialogPreference {
    public PopupPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //super.setDialogLayoutResource(R.xml.prefs_dialog);
        //super.setDialogIcon(R.drawable.ic);
    }

    public PopupPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        //super.setDialogLayoutResource(R.xml.prefs_dialog);
        //super.setDialogIcon(R.drawable.ic);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        //persistBoolean(positiveResult);
    }
}
