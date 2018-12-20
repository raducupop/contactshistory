package com.contactshistory;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dark_theme = sharedPrefs.getBoolean("prefDarkUI", false);
        if (dark_theme) {
            this.setTheme(R.style.AppThemeDark);
        }
        else {
            this.setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        catch (NullPointerException e){
            System.out.println("Error " + e.getMessage());
        }

        TextView link = findViewById(R.id.textView5);
        try {
            link.setMovementMethod(LinkMovementMethod.getInstance());
        }

        catch (Exception e){
           //
        }

        TextView link_lib1 = findViewById(R.id.lib1);
        try {
            link.setMovementMethod(LinkMovementMethod.getInstance());
        }

        catch (Exception e){
            //
        }


    }

}