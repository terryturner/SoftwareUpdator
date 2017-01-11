package com.goldtek.sw.updater;


import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.goldtek.sw.updater.model.ReportHandler;
import com.goldtek.sw.updater.presenter.ConfigManager;
import com.goldtek.sw.updater.presenter.PackageManager;

import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

                if (listPreference.getValue().equals(stringValue) == false)
                    ConfigManager.getInstance().setConfig(preference.getKey(), stringValue);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }


            return true;
        }
    };

    private static Preference.OnPreferenceChangeListener sPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if (preference instanceof SwitchPreference && ((SwitchPreference) preference).isChecked() != (Boolean) value) {
                String stringValue = value.toString();
                ConfigManager.getInstance().setConfig(preference.getKey(), stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    private boolean mIsFooter = false;
    private ScheduleService mScheduler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setupActionBar();

        if (PackageManager.getInstance().getApplicationInfo() != null && mIsFooter)
            setTitle(getTitle() + " " + PackageManager.getInstance().getApplicationInfo().versionName);

        if (!isServiceRunning(ScheduleService.class)) {
            Intent sticky = new Intent(this, ScheduleService.class);
            startService(sticky);
        }

        if (mIsFooter) {
            Intent sticky = new Intent(this, ScheduleService.class);
            bindService(sticky, mScheduleConnection, Context.BIND_AUTO_CREATE);
        } else {
            setupActionBar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mIsFooter) {
            unbindService(mScheduleConnection);
            ReportHandler.getInstance().writeMessage(R.string.msg_user_logout);
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
        mIsFooter = true;
    }


    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if (header.id == R.id.com_goldtek_sw_updater_SyncNow) {
            if (mScheduler != null)
                mScheduler.sync();
            else {
                Intent sticky = new Intent(this, ScheduleService.class);
                bindService(sticky, mScheduleConnection, Context.BIND_AUTO_CREATE);

                Toast.makeText(this, "Error with background service!", Toast.LENGTH_SHORT).show();
            }
        } else if (header.id == R.id.com_goldtek_sw_updater_Logout) {
            finish();
        }
    }

    ServiceConnection mScheduleConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ScheduleService.LocalBinder binder = (ScheduleService.LocalBinder) iBinder;
            mScheduler = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };


    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || SettingPreferenceFragment.class.getName().equals(fragmentName)
                || InformationPreferenceFragment.class.getName().equals(fragmentName);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SettingPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_settings);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            findPreference(ConfigManager.KEY_UPDATE_AUTO).setOnPreferenceChangeListener(sPreferenceChangeListener);
            bindPreferenceSummaryToValue(findPreference(ConfigManager.KEY_SYNC_TIME));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class InformationPreferenceFragment extends PreferenceFragment implements ConfigManager.Observer {
        private Handler mHandler = new Handler();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_information);
            setHasOptionsMenu(true);

            findPreference(ConfigManager.KEY_LAST_SYNC).setSummary(ConfigManager.getInstance().getLastSyncTime(GoldtekApplication.sDateFormat));
            findPreference(ConfigManager.KEY_LAST_SUCCESS).setSummary(ConfigManager.getInstance().getLastSuccessTime(GoldtekApplication.sDateFormat));

            //TODO: receive Package Install event, update this preference
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ConfigManager.getInstance().addObserver(this);
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            ConfigManager.getInstance().removeObserver(this);
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onMaintainChange() {}

        @Override
        public void onConfigChange(String key, String value) {
            switch (key) {
                case ConfigManager.KEY_LAST_SYNC:
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            findPreference(ConfigManager.KEY_LAST_SYNC).setSummary(ConfigManager.getInstance().getLastSyncTime(GoldtekApplication.sDateFormat));
                            findPreference(ConfigManager.KEY_LAST_SUCCESS).setSummary(ConfigManager.getInstance().getLastSuccessTime(GoldtekApplication.sDateFormat));
                        }
                    }, 1000);
                    break;
            }
        }
    }


}
