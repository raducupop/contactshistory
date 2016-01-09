package com.contactshistory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings_screen);



        final CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("prefDisplayIcon");
        assert checkboxPref != null;
        checkboxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
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
    }


}
