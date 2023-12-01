package com.contactshistory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        boolean dark_theme = sharedPrefs.getBoolean("prefDarkUI", false);
        if (dark_theme) {
            getActivity().setTheme(R.style.AppThemeDark);
        } else {
            getActivity().setTheme(R.style.AppTheme);
        }

        addPreferencesFromResource(R.xml.settings_screen);

        final SwitchPreference notify_pref = (SwitchPreference) getPreferenceManager().findPreference("prefDisplayIcon");
        assert notify_pref != null;
        notify_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object prefDisplayIcon) {
                if (prefDisplayIcon.toString().equals("true")) {
                    Intent startServiceIntent = new Intent(MainActivity.getAppContext(), HistoryService.class);
                    MainActivity.getAppContext().startService(startServiceIntent);
                } else {
                    Intent startServiceIntent = new Intent(MainActivity.getAppContext(), HistoryService.class);
                    MainActivity.getAppContext().startService(startServiceIntent);

                }
                return true;

            }
        });

        final SwitchPreference theme_pref = (SwitchPreference) getPreferenceManager().findPreference("prefDarkUI");
        assert theme_pref != null;
        theme_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object prefDarkUI) {
                if (prefDarkUI.toString().equals("true")) {
                    // Toast.makeText(getActivity().getApplicationContext()," TRUE", Toast.LENGTH_LONG).show();
                    getActivity().setTheme(R.style.AppThemeDark);
                    getActivity().recreate();

                } else {
                    // Toast.makeText(getActivity().getApplicationContext(), " FALSE", Toast.LENGTH_LONG).show();
                    getActivity().setTheme(R.style.AppTheme);
                    getActivity().recreate();
                }
                return true;
            }
        });

    }


}
