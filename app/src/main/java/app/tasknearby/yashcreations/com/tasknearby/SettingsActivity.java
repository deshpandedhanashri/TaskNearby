package app.tasknearby.yashcreations.com.tasknearby;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.tasknearby.yashcreations.com.tasknearby.service.FusedLocationService;

/**
 * Created by Yash on 01/05/15.
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        TextView toolbarTV = (TextView) toolbar.findViewById(R.id.settings_title_toolbar);
        toolbarTV.setText(getString(R.string.action_settings));
        toolbarTV.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Raleway-SemiBold.ttf"));

        getFragmentManager().beginTransaction().add(R.id.contentFrame, new SettingsFragment(), "F_TAG").commit();
    }


    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            ListPreference mUnitPreference = (ListPreference) getPreferenceManager().findPreference(getString(R.string.pref_units_key));
            ListPreference mAccuracyPreference = (ListPreference) getPreferenceManager().findPreference(getString(R.string.pref_accuracy_key));
            RingtonePreference mRingtonePreference = (RingtonePreference) getPreferenceManager().findPreference(getString(R.string.pref_tone_key));

            bindPreferenceSummaryToValue(mUnitPreference);
            bindPreferenceSummaryToValue(mAccuracyPreference);
            bindPreferenceSummaryToValue(mRingtonePreference);

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list (since they have separate labels/values).
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
                if (preference.getKey().equals(getString(R.string.pref_accuracy_key))) {
                    Intent serviceIntent = new Intent(getActivity(), FusedLocationService.class);
                    getActivity().stopService(serviceIntent);
                    String appStatus = PreferenceManager.getDefaultSharedPreferences(getActivity())
                            .getString(getString(R.string.pref_status_key),getString(R.string.pref_status_default));
                    if(appStatus.equals("enabled"))
                        getActivity().startService(serviceIntent);
                }
            } else if (preference instanceof RingtonePreference) {
                try {
                    Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse((String) newValue));
                    String summary = ringtone.getTitle(getActivity());
                    preference.setSummary(summary);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary(stringValue);
            }
            return true;

        }
        /**
         * Attaches a listener so the summary is always updated with the preference value.
         * Also fires the listener once, to initialize the summary (so it shows up before the value
         * is changed.)
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(this);

            // Trigger the listener immediately with the preference's
            // current value.
            String defaultValue = "";
            if(preference instanceof RingtonePreference)
                defaultValue = Settings.System.DEFAULT_ALARM_ALERT_URI.getPath();

            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), defaultValue));
        }
    }

}
