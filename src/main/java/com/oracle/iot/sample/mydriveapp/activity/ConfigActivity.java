package com.oracle.iot.sample.mydriveapp.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.oracle.iot.sample.mydriveapp.R;

public class ConfigActivity extends Activity implements
        OnSharedPreferenceChangeListener, OnPreferenceClickListener {

    private PreferenceFragment mPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferenceFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mPreferenceFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        mPreferenceFragment.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        mPreferenceFragment.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_filter);
        }
    }
}
