package com.contactshistory;

import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    SharedPreferences runpref, deftab;
    public static Context appcontext;


    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    
    int NumberOfTabs = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String [] titles = { getResources().getString(R.string.tab_recent),
                getResources().getString(R.string.tab_today),
                getResources().getString(R.string.tab_week),
                getResources().getString(R.string.tab_month),
                getResources().getString(R.string.tab_date),
                getResources().getString(R.string.tab_twodates),
                getResources().getString(R.string.tab_location)  };

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles for the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),titles,NumberOfTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        appcontext = getApplicationContext();
        Context context = this.getApplicationContext();
        
        runpref = context.getSharedPreferences("firstRunPrefs", 0); //0 = mode private. only this app can read these preferences
        deftab = context.getSharedPreferences("tabPrefes",0);

        Intent startServiceIntent = new Intent(this, HistoryService.class);
        this.startService(startServiceIntent);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                startActivity(intent);
            }
        });



        if(getFirstRun()){

            // se executa o singura data, la prima rulare a aplicatiei

            //Toast.makeText(getApplicationContext(), "Este prima data cand este deschisa aplicatia.", Toast.LENGTH_LONG).show();

            PreferenceManager.setDefaultValues(this, R.xml.settings_screen, false);

            SharedPreferences.Editor edit = deftab.edit();
            edit.putInt("default_tab", 0);
            edit.commit();

            setNotFirst();
        }

        PackageManager m = getPackageManager();
        String installdir = getPackageName();
        PackageInfo p = null;
        try {
            p = m.getPackageInfo(installdir, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        installdir = p.applicationInfo.sourceDir;


        if (installdir.startsWith("/data/"))

            Log.d("contacts_history", "systen");

        else
        {
            Log.d("contacts_history", "extern");

            final AlertDialog.Builder storage_err = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            storage_err.setTitle(getResources().getString(R.string.warning_install_title))
                    .setMessage(getResources().getString(R.string.warning_install_text))
                    .setPositiveButton(getResources().getString(R.string.warning_install_positive_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {


                            String packageName = "com.contactshistory";

                            try {

                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + packageName));
                                startActivity(intent);

                            } catch ( ActivityNotFoundException e ) {

                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                startActivity(intent);

                            }

                        }
                    })
                    .setNeutralButton(getResources().getString(R.string.warning_install_neutral_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            storage_err.setIcon(R.drawable.ic_warning_black_24dp);
            storage_err.show();

        }


        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (locationProviders == null || locationProviders.equals("")) {


            final AlertDialog.Builder location_err = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            location_err.setTitle(getResources().getString(R.string.warning_location_title))
                    .setMessage(getResources().getString(R.string.warning_location_text))
                    .setPositiveButton(getResources().getString(R.string.warning_location_positive_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                        }
                    })
                    .setNeutralButton(getResources().getString(R.string.warning_location_neutral_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            location_err.setIcon(R.drawable.ic_warning_black_24dp);
            location_err.show();

        }

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi_con = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo data_con = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (!wifi_con.isConnected() && !data_con.isConnected())
        {

            final AlertDialog.Builder connection_err = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            connection_err.setTitle(getResources().getString(R.string.warning_conn_title))
                    .setMessage(getResources().getString(R.string.warning_conn_text))
                    .setPositiveButton(getResources().getString(R.string.warning_conn_positive_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

                        }
                    })

                    .setNegativeButton(getResources().getString(R.string.warning_conn_negative_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            startActivity(new Intent(Settings.ACTION_SETTINGS));

                        }
                    })

                    .setNeutralButton(getResources().getString(R.string.warning_conn_neutral_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

            connection_err.setIcon(R.drawable.ic_warning_black_24dp);
            connection_err.show();

        }

        SharedPreferences.Editor edit = deftab.edit();
        edit.putInt("default_tab", 0);
        edit.commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    public static Context getAppContext(){
        return appcontext;
    }

    public boolean getFirstRun() {
        return runpref.getBoolean("firstRun", true);
    }

    public void setNotFirst() {
        SharedPreferences.Editor edit = runpref.edit();
        edit.putBoolean("firstRun", false);
        edit.commit();
    }


    @Override
    public boolean onKeyUp(int keycode, KeyEvent e) {
        switch (keycode) {
            case KeyEvent.KEYCODE_MENU:
                if ( getSupportActionBar() != null ) {
                    getSupportActionBar().openOptionsMenu();

                    return true;
                }
        }

        return super.onKeyUp(keycode, e);
    }
}