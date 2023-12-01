package com.contactshistory;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivityWrapper extends AppCompatActivity {

    androidx.appcompat.widget.Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dark_theme = sharedPrefs.getBoolean("prefDarkUI", false);
        if (dark_theme) {
            setTheme(R.style.AppThemeDark);
        }
        else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_toolbar_activity);
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();


    }

    @Override
    public void onBackPressed()
    {
        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }

}