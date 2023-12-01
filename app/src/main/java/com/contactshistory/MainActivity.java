package com.contactshistory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    SharedPreferences firstrun, deftab;
    public static Context appcontext;

    Toolbar toolbar;

    androidx.viewpager.widget.ViewPager pager;
    ViewPagerAdapter adapter;



    int numberOfTabs = 8;
    int numberOfTabs_compact = 5;

    boolean compact_ui = true;

    boolean permission_ok = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dark_theme = sharedPrefs.getBoolean("prefDarkUI", false);
        if (dark_theme) {
            setTheme(R.style.AppThemeDark);
        }
        else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        String [] titles = { getResources().getString(R.string.tab_all),
                getResources().getString(R.string.tab_recent),
                getResources().getString(R.string.tab_today),
                getResources().getString(R.string.tab_week),
                getResources().getString(R.string.tab_month),
                getResources().getString(R.string.tab_date),
                getResources().getString(R.string.tab_twodates),
                getResources().getString(R.string.tab_location)  };

        String [] titles_compact = { getResources().getString(R.string.tab_all),
                getResources().getString(R.string.tab_recent),
                getResources().getString(R.string.tab_date),
                getResources().getString(R.string.tab_twodates),
                getResources().getString(R.string.tab_location)  };

        // Creating The Toolbar and setting it as the Toolbar for the activity
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles for the Tabs and Number Of Tabs.

        compact_ui = sharedPrefs.getBoolean("prefCompactUI", true);

        if (compact_ui){
            adapter =  new ViewPagerAdapter(getSupportFragmentManager(),titles_compact, numberOfTabs_compact);
        }

        else {
            adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, numberOfTabs);
        }


        // Assigning ViewPager View and setting the adapter
        pager = findViewById(R.id.pager_);
        pager.setAdapter(adapter);

        // Assiging the Sliding Tab Layout View
        SlidingTabLayout tabs = findViewById(R.id.tabs);
        tabs.setDistributeEvenly(false); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

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

        firstrun = context.getSharedPreferences("firstRun_tutorial", 0); //0 = mode private. only this app can read these preferences

        deftab = context.getSharedPreferences("tabPrefes",0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // api >= M so must check for permissions
                checkPermission();

            } else {

                // api < M so permission already granted.

                Intent startServiceIntent = new Intent(this, HistoryService.class);
                this.startService(startServiceIntent);
                permission_ok = true;

                }

         // close if this activity it is started at device boot:

        boolean close;
        Intent intent = getIntent();
        close = intent.getBooleanExtra("START_TYPE", false);
        if (close && permission_ok) {
            finish();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
                    startActivity(intent);

            }
        });

        if(getFirstRun()){

            // Run only one time, at 1st start of the app.

            //Toast.makeText(getApplicationContext(), "Este prima data cand este deschisa aplicatia.", Toast.LENGTH_LONG).show();

            PreferenceManager.setDefaultValues(this, R.xml.settings_screen, false);

            SharedPreferences.Editor edit = deftab.edit();
            edit.putInt("default_tab", 0);
            edit.apply();

            Intent startTutorial = new Intent(MainActivity.this, TutorialActivity.class);
            MainActivity.this.startActivity(startTutorial);

            setNotFirst();
        }

//
//        if (ContextCompat.checkSelfPermission(MainActivity.getAppContext(),
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//
//                // request the permission
//
//            Toast.makeText(getAppContext(),"no permission", Toast.LENGTH_SHORT);
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        104);
//
//        } else {
//
//            // Permission has already been granted
//
//
//        }

        SharedPreferences.Editor edit = deftab.edit();
        edit.putInt("default_tab", 0);
        edit.apply();
    }

    private void checkPermission() {
        if (    ContextCompat.checkSelfPermission(MainActivity.getAppContext(), Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.getAppContext(), Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.getAppContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.getAppContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)

           {

               // checkLocationEnabled();    DO NOT CHECK IF LOCATION IS ENABLED, AT APP STARTUP

               checkNetworkEnabled();

               checkInstall();

               Intent startServiceIntent = new Intent(this, HistoryService.class);
               this.startService(startServiceIntent);

               //Toast.makeText(getAppContext(),"start service", Toast.LENGTH_LONG).show();

               permission_ok = true;
               }

               else
                  {
                     ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.GET_ACCOUNTS,
                            Manifest.permission.WRITE_CONTACTS,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE

                              },

                    1000);
                  }

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

        return super.onOptionsItemSelected(item);
    }

    public static Context getAppContext(){
        return appcontext;
    }

    public boolean getFirstRun() {

        return firstrun.getBoolean("firstRun", true);

    }


    private void checkLocationEnabled(){

        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (locationProviders == null || locationProviders.equals("")) {

            final AlertDialog.Builder location_err = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);

            final View checkBoxView = View.inflate(this, R.layout.checkbox, null);


            location_err.setTitle(getResources().getString(R.string.warning_location_title))
                    .setMessage(getResources().getString(R.string.warning_location_text))
                    .setView(checkBoxView)
                    .setPositiveButton(getResources().getString(R.string.warning_location_positive_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Boolean checkBoxResult = false;
                            CheckBox dontShowAgain = checkBoxView.findViewById(R.id.do_not_show);
                            if (dontShowAgain.isChecked())
                                checkBoxResult = true;
                            SharedPreferences settings = getSharedPreferences("location_dialog_do_not_show",0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("skipLocationDialog", checkBoxResult);
                            editor.commit();

                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                        }
                    })
                    .setNeutralButton(getResources().getString(R.string.warning_location_neutral_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Boolean checkBoxResult = false;
                            CheckBox dontShowAgain = checkBoxView.findViewById(R.id.do_not_show);
                            if (dontShowAgain.isChecked())
                                checkBoxResult = true;
                            SharedPreferences settings = getSharedPreferences("location_dialog_do_not_show",0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("skipLocationDialog", checkBoxResult);
                            editor.commit();
                            editor.commit();

                        }
                    });

            location_err.setIcon(R.drawable.ic_warning_black_24dp);

            SharedPreferences settings = getSharedPreferences("location_dialog_do_not_show", 0);
            Boolean skip = settings.getBoolean("skipLocationDialog", false);
            if (!skip) location_err.show();

        }
    }

    private void checkNetworkEnabled(){

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi_con = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo data_con = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


        if (!wifi_con.isConnected() && !data_con.isConnected())
        {

            final View checkBoxView = View.inflate(this, R.layout.checkbox, null);

            final AlertDialog.Builder connection_err = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            connection_err.setTitle(getResources().getString(R.string.warning_conn_title))
                    .setMessage(getResources().getString(R.string.warning_conn_text))
                    .setView(checkBoxView)
                    .setPositiveButton(getResources().getString(R.string.warning_conn_positive_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Boolean checkBoxResult = false;
                            CheckBox dontShowAgain = checkBoxView.findViewById(R.id.do_not_show);
                            if (dontShowAgain.isChecked())
                                checkBoxResult = true;
                            SharedPreferences settings = getSharedPreferences("net_dialog_do_not_show",0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("skipNetDialog", checkBoxResult);
                            editor.commit();

                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));

                        }
                    })

                    .setNegativeButton(getResources().getString(R.string.warning_conn_negative_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            Boolean checkBoxResult = false;
                            CheckBox dontShowAgain = checkBoxView.findViewById(R.id.do_not_show);
                            if (dontShowAgain.isChecked())
                                checkBoxResult = true;
                            SharedPreferences settings = getSharedPreferences("net_dialog_do_not_show",0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("skipNetDialog", checkBoxResult);
                            editor.commit();

                            startActivity(new Intent(Settings.ACTION_SETTINGS));

                        }
                    })

                    .setNeutralButton(getResources().getString(R.string.warning_conn_neutral_btn), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {


                            Boolean checkBoxResult = false;
                            CheckBox dontShowAgain = checkBoxView.findViewById(R.id.do_not_show);
                            if (dontShowAgain.isChecked())
                                checkBoxResult = true;
                            SharedPreferences settings = getSharedPreferences("net_dialog_do_not_show",0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("skipNetDialog", checkBoxResult);
                            editor.commit();
                            editor.commit();

                        }
                    });

            connection_err.setIcon(R.drawable.ic_warning_black_24dp);
            SharedPreferences settings = getSharedPreferences("net_dialog_do_not_show", 0);
            Boolean skip = settings.getBoolean("skipNetDialog", false);
            if (!skip) connection_err.show();

        }
    }


    private void checkInstall(){

        PackageManager m = getPackageManager();
        String installdir = getPackageName();
        PackageInfo p = null;
        try {
            p = m.getPackageInfo(installdir, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        try{
            if (installdir!=null){
                installdir = p.applicationInfo.sourceDir;
            }
        }
        catch (java.lang.NullPointerException E){
            //
        }

        if (p!=null) {

            if (installdir.startsWith("/data/"))

                Log.d("contacts_history", "system");

            else {
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

                                } catch (ActivityNotFoundException e) {

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

        }

    }

    public void setNotFirst() {
        SharedPreferences.Editor edit = firstrun.edit();
        edit.putBoolean("firstRun", false);
        edit.apply();
    }

    @SuppressLint("RestrictedApi")
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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1000: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED
                        && grantResults[4] == PackageManager.PERMISSION_GRANTED
                        && grantResults[5] == PackageManager.PERMISSION_GRANTED
                        ) {

                    // checkLocationEnabled();
                    checkNetworkEnabled();
                    checkInstall();

                    Intent startServiceIntent = new Intent(this, HistoryService.class);
                    this.startService(startServiceIntent);


                    // Assigning ViewPager View and setting the adapter
                    pager = findViewById(R.id.pager_);
                    pager.setAdapter(adapter);

                    // Assiging the Sliding Tab Layout View
                    SlidingTabLayout tabs = findViewById(R.id.tabs);
                    tabs.setDistributeEvenly(false); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

                    // Setting Custom Color for the Scroll bar indicator of the Tab View
                    tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                        @Override
                        public int getIndicatorColor(int position) {
                            return getResources().getColor(R.color.tabsScrollColor);
                        }
                    });

                    // Setting the ViewPager For the SlidingTabsLayout
                    tabs.setViewPager(pager);

                    permission_ok = true;



                    //Toast.makeText(getAppContext(), "Permissions granted.", Toast.LENGTH_SHORT).show();

                } else {

                    View coordinatorLayout = findViewById(R.id.coordinatorLayout);
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, getResources().getString(R.string.snack_permissions_nok), Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(Color.YELLOW)
                            .setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Uri packageUri = Uri.fromParts( "package", getApplicationContext().getPackageName(), null );
                                    Intent applicationDetailsSettingsIntent = new Intent();
                                    applicationDetailsSettingsIntent.setAction( Settings.ACTION_APPLICATION_DETAILS_SETTINGS );
                                    applicationDetailsSettingsIntent.setData( packageUri );
                                    applicationDetailsSettingsIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                    getApplicationContext().startActivity( applicationDetailsSettingsIntent );
                                }
                            });
                    TextView snakckTextView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                    View snack = snackbar.getView();
                    snack.setBackgroundColor(getResources().getColor(R.color.snackbar));
                    snakckTextView.setTextColor(Color.WHITE);

                    snackbar.show();

                }
            }

        }

            // other 'case' lines to check for other
            // permissions this app might request.
    }

}