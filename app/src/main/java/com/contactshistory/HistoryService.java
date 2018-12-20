package com.contactshistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.content.ContentResolver;
import android.content.ContentProviderOperation;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.provider.ContactsContract.CommonDataKinds;
import android.widget.Toast;

import static android.location.LocationManager.*;

public class HistoryService extends Service {


    Cursor c, observer;
    public ContentObserver obs;
    int n = 0, na = 0;
    int contacts_count = 0;
    ArrayList<String> contacte = new ArrayList<>();
    ArrayList<String> contacte_a = new ArrayList<>();
    ArrayList<String> temp = new ArrayList<>();
    String info = null;
    Boolean locationFound = false;
    TelephonyManager tm;
    CallNumberReceiver numberReceiver = new CallNumberReceiver();

    int exit = 0;
    int edit = 1;

    @Override
    public void onCreate() {

        super.onCreate();

        try {


            //Toast.makeText(getApplicationContext(),"serv creat! ", Toast.LENGTH_LONG).show();

            Intent startServiceIntent = new Intent(this.getBaseContext(), HistoryService.class);
            this.getBaseContext().startService(startServiceIntent);
            c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);

            assert c != null;
            n = c.getCount();
            contacte.clear();
            String id;

            if (c.getCount() > 0) {

                c.moveToFirst();
                do {
                    id = c.getString(c.getColumnIndex(ContactsContract.RawContacts._ID));
                    contacte.add(id);
                } while (c.moveToNext());
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Contacts History stopped. Check if all permissions are granted.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStart(intent, startId);

        try {
            //Toast.makeText(getApplicationContext(),"serv pornit! ", Toast.LENGTH_LONG).show();

            toggleIcon();

            tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

            tm.listen(numberReceiver, PhoneStateListener.LISTEN_CALL_STATE);

            final DBAdapter db = new DBAdapter(this);

            observer = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);

            obs = new ContentObserver(new Handler()) {

                @Override
                public void onChange(boolean selfChange) {


                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    Boolean ok_to_notify = false;

                    Intent launchMain = new Intent(getApplicationContext(),
                            MainActivity.class);
                    launchMain.setAction("android.intent.action.MAIN");
                    launchMain.addCategory("android.intent.category.LAUNCHER");

                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, launchMain, PendingIntent.FLAG_UPDATE_CURRENT);

                    Date date = Calendar.getInstance().getTime();
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    final String data_azi = formatter.format(date);

                    Cursor ca = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);

                    Cursor full_contacts_after = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                    assert full_contacts_after != null;
                    int contacts_count_after = full_contacts_after.getCount();


                    assert ca != null;
                    na = ca.getCount();
                    String id_contact;
                    contacte_a.clear();

                    if (ca.getCount() > 0) {
                        ca.moveToFirst();
                        do {
                            id_contact = ca.getString(ca.getColumnIndex(ContactsContract.RawContacts._ID));
                            contacte_a.add(id_contact);
                        } while (ca.moveToNext());

                    }

                    if ((na > n) && (contacts_count != contacts_count_after)) {

                        edit = 1;

                        ok_to_notify = true;

                        temp = contacte_a;
                        temp.removeAll(contacte);
                        db.open();
                        db.insertContact(temp.toString(), data_azi, getLocation().get(0), getLocation().get(1));
                        db.close();
                        //Toast.makeText(getApplicationContext(), temp.toString() + " Adaugat in Contacts History \n\n" + data_azi + "\n" + getLocation(), Toast.LENGTH_SHORT).show();


                        String cid = temp.toString();
                        String cidd = cid.substring(1, cid.length() - 1);


                        if (sharedPrefs.getBoolean("prefReadData", true)) {

                            //Log.v("ReadData","Pref active");
                            ContentResolver cr = getContentResolver();
                            Cursor read = getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, null, null, null);

                            assert read != null;
                            if (read.getCount() > 0) {
                                read.moveToFirst();
                                //Log.v("ReadData","All cursor > 0");

                                int count = 0;

                                do {

                                    //String id = read.getString(read.getColumnIndex(ContactsContract.Contacts._ID));
                                    String lookupid = read.getString(read.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));

                                    if ((lookupid.contentEquals(cidd)) && (count == 0)) {
                                        count = 1;

                                        //Log.d("ReadData","Contact found");

                                        String noteWhere = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                                        String[] noteWhereParams = new String[]{cidd,
                                                CommonDataKinds.Note.CONTENT_ITEM_TYPE};
                                        Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
                                        //Log.d("ReadData","Notes count for contact: "+String.valueOf(noteCur.getCount()));

                                        assert noteCur != null;
                                        if (noteCur.moveToFirst()) {

                                            db.open();

                                            do {

                                                String note = noteCur.getString(noteCur.getColumnIndex(CommonDataKinds.Note.NOTE));

                                                //Log.d("ReadData", note);

                                                if (note.contains("Contacts History Data")) {

                                                    String[] note_data = note.split("\\|");

                                                    if ((note_data[1].length() > 0) && (note_data[2].length() > 0) && (note_data[3].length() > 0)) {
                                                        //Log.d("ReadData", "\n " + temp.toString() + " " + note_data[1] + " " + note_data[3] + " " + note_data[2]);
                                                        db.insertContact(temp.toString(), note_data[1], note_data[3], note_data[2]);

                                                    }

                                                }

                                            } while (noteCur.moveToNext());

                                            db.close();

                                        }
                                        noteCur.close();
                                    }
                                } while (read.moveToNext());

                            }
                            read.close();


                        }
                        if (sharedPrefs.getBoolean("prefWriteData", true)) {

                            writeToContact(cidd, data_azi, getLocation().get(0), getLocation().get(1));
                        }

                        edit = 1;
                    }

                    //if ( (na < n) && ( contacts_count != contacts_count_after)) {
                    if ((na < n)) {
                        temp = contacte;
                        temp.removeAll(contacte_a);

                        db.open();
                        db.deleteContact(temp.toString());
                        db.close();

                        info = "Contact sters.";
                        //Toast.makeText(getApplicationContext(),"Sters din Contacts History", Toast.LENGTH_SHORT).show();
                    }


                    if ((na == n) && (edit == 1)) {

                        ArrayList<String> old_id = new ArrayList<>();
                        ArrayList<String> new_id = new ArrayList<>();

                        old_id.addAll(contacte);
                        new_id.addAll(contacte_a);

                        old_id.removeAll(contacte_a);
                        new_id.removeAll(contacte);

                        if (old_id.size() > 0) {
                            //Toast.makeText(getApplicationContext(),"Contact editat in Contacts History", Toast.LENGTH_SHORT).show();
                            info = "Contact changed.";

                            db.open();
                            db.updateContact(old_id.toString(), new_id.toString());
                            //db.insertContact(new_id.toString(),data_azi,getLocation().get(0),getLocation().get(1));
                            db.close();

                        }


                    }


                    contacte.clear();
                    contacte.addAll(contacte_a);
                    n = ca.getCount();
                    ca.close();
                    full_contacts_after.close();

                    if (ok_to_notify && (sharedPrefs.getBoolean("prefDisplayIconContact", true))) {


                        NotificationManager iconUpdate = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


                        if (locationFound) {
                            NotificationCompat.Builder location_notification = new NotificationCompat.Builder(getApplicationContext(), "notify_002");
                            location_notification.setAutoCancel(true);
                            location_notification.setContentTitle(getResources().getString(R.string.service_icon_contact));
                            location_notification.setContentText(info);
                            location_notification.setSmallIcon(R.drawable.ic_social_person_add);
                            location_notification.setContentIntent(pendingIntent);
                            location_notification.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                            location_notification.setOngoing(false);
                            location_notification.setDefaults(0);
                            location_notification.setChannelId("notify_002");

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel channel = new NotificationChannel("notify_002", getResources().getString(R.string.notification_new),
                                        NotificationManager.IMPORTANCE_LOW);
                                channel.setShowBadge(false);
                                iconUpdate.createNotificationChannel(channel);
                            }

                            iconUpdate.notify(65151, location_notification.build());
                        } else {

                            NotificationCompat.Builder no_location_notification = new NotificationCompat.Builder(getApplicationContext(), "notify_002");
                            no_location_notification.setAutoCancel(true);
                            no_location_notification.setContentTitle(getResources().getString(R.string.service_icon_contact));
                            no_location_notification.setContentText(info);
                            no_location_notification.setSmallIcon(R.drawable.ic_location_off_white_24dp);
                            no_location_notification.setContentIntent(pendingIntent);
                            no_location_notification.setOngoing(false);
                            no_location_notification.setDefaults(0);
                            no_location_notification.setChannelId("notify_002");


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel channel = new NotificationChannel("notify_002", getResources().getString(R.string.notification_new),
                                        NotificationManager.IMPORTANCE_LOW);
                                channel.setShowBadge(false);
                                iconUpdate.createNotificationChannel(channel);
                            }

                            iconUpdate.notify(65152, no_location_notification.build());
                        }

                    }

     /*             DBAdapter db_tmp = new DBAdapter(getBaseContext());
                    db_tmp.open();
                    Cursor db_ids =  db_tmp.getAllContacts();


                    ArrayList<String> list_purge = new ArrayList<>();

                    ArrayList<String> x = new ArrayList<>();
                    ArrayList<String> y = new ArrayList<>();

                    Toast.makeText(getApplicationContext(),"In DB: "+String.valueOf(db_ids.getCount()), Toast.LENGTH_SHORT).show();
                    Cursor all_ids = getBaseContext().getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);

                    if (db_ids.getCount()>0)
                    {
                        if(db_ids.moveToFirst())
                        {
                            //Toast.makeText(getApplicationContext(),"DB", Toast.LENGTH_SHORT).show();
                            do{

                                String id_db = db_ids.getString(0);

                                String id_db_ok = id_db.substring(1, id_db.length()-1);

                                x.add(id_db_ok);

                            }while (db_ids.moveToNext());
                        }
                    }

                    if(all_ids.getCount()>0)
                    {
                        if (all_ids.moveToFirst()){

                            do {

                                String id_provider_row = all_ids.getString(all_ids.getColumnIndex(ContactsContract.RawContacts._ID));

                                y.add(id_provider_row);

                            }while (all_ids.moveToNext());
                        }
                    }

                    all_ids.close();
                    db_ids.close();

                    int gasit = 0;

                    for (int i=0;i<x.size();i++){
                           gasit = 0;
                           for (int j=0; j<y.size(); j++) {

                               try {
                                    Long a = Long.valueOf(x.get(i));
                                    Long b = Long.valueOf(y.get(j));

                                    if (a.equals(b)) gasit = 1;

                               } catch (NumberFormatException e) {
                                   e.printStackTrace();
                               }

                           }

                        if ( gasit == 0) list_purge.add(x.get(i));

                    }

                    if (list_purge.size()>0){

                        for (int i=0;i<list_purge.size();i++){
                            Toast.makeText(getApplicationContext(),"In purge: "+String.valueOf(list_purge.size()), Toast.LENGTH_SHORT).show();
                            db_tmp.deleteContact(list_purge.get(i));
                        }
                    }

                    db_tmp.close();*/

                }

                @Override
                public boolean deliverSelfNotifications() {
                    return false;
                }
            };

            observer.registerContentObserver(obs);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Contacts History stopped. Check if all permissions are granted.", Toast.LENGTH_LONG).show();
        }

        return (START_STICKY);
    }

/*
    public String idToRawId(String LookupKey)
    {
        String rawid="";
        Cursor cursor_raw = getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, null, null, null);

        if (cursor_raw.getCount() > 0)
        {

            cursor_raw.moveToFirst();
            do {
                String idcontact = cursor_raw.getString(cursor_raw.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
                String idrawcontact = cursor_raw.getString(cursor_raw.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));


                if (idcontact.contentEquals(LookupKey))
                {
                    rawid = idrawcontact;

                }

            } while (cursor_raw.moveToNext());
            //cursor_raw.close();
        }

        return rawid;
    }*/

    public void writeToContact(String ID, String Date, String Geo, String Location) {

        if (ID.length() > 0) {

            ArrayList<ContentProviderOperation> ops =
                    new ArrayList<>();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, ID)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.Data.DATA1, "Contacts History Data |" + Date + "|" + Location + "|" + Geo)
                    .build());
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public ArrayList<String> getLocation() {

        locationFound = false;

        ArrayList<String> result = new ArrayList<>();
        Location location = null;

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo dcon = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected() || dcon.isConnected()) {

            final LocationManager locationManager;
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            final String provider = NETWORK_PROVIDER;

            LocationListener myLocationListener = new LocationListener() {
                public void onLocationChanged(Location location) {


                    Log.d("contactshistory_service", "on location changed - update ok");


                }

                public void onProviderDisabled(String provider) {

                }

                public void onProviderEnabled(String provider) {
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
            };

            for (int i = 1; i <= 10; i++)    //  refresh location
            {
                Log.d("contactshistory_service", "before update ...");

                try {
                    locationManager.requestSingleUpdate(provider, myLocationListener, null);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                Log.d("contactshistory_service", "after request ...");
            }


            try {
                location = locationManager.getLastKnownLocation(provider);
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            locationManager.removeUpdates(myLocationListener);

            Geocoder geocoder;

            List<Address> user;
            double lat;
            double lng;

            result.add("null");
            result.add("null");
            
            if (location == null){

                //Toast.makeText(getApplicationContext(),"Locatia nu a putut sa fie determinata",Toast.LENGTH_SHORT).show();
                info = getResources().getString(R.string.service_no_location);

                String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                if (locationProviders == null || locationProviders.equals("")) {

                    //Toast.makeText(getApplicationContext(),"Fara acces la locatie.", Toast.LENGTH_SHORT).show();
                    info = getResources().getString(R.string.warning_location_title);

                }

            }
            else
            {

                geocoder = new Geocoder(getApplicationContext());
                result.clear();
                info = getResources().getString(R.string.service_no_location);

                try {
                    user = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    lat=user.get(0).getLatitude();
                    lng=user.get(0).getLongitude();

                    result.add(lat+"#"+lng);

                    if (user.size() > 0) {
                        Address address = user.get(0);

                        String addressText = String.format("%s, %s",
                                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : address.getLocality(),
                                address.getCountryName());
                        result.add(addressText);

                        info = addressText;
                        locationFound = true;

                    }
                    else
                    {
                        String addressText = "null";
                        result.add(addressText);

                    }


                }catch (Exception e) {
                    e.printStackTrace();
                    result.clear();
                    result.add("null");
                    result.add("null");
                    return result;

                }


            }
            return result;

        }

        else {
            //Toast.makeText(getApplicationContext(),"Fara conexiune de date.", Toast.LENGTH_SHORT).show();
            info = getResources().getString(R.string.service_no_connexion);
            result.clear();
            result.add("null");
            result.add("null");
        }

        String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (locationProviders == null || locationProviders.equals("")) {

            //Toast.makeText(getApplicationContext(),"Fara acces la locatie.", Toast.LENGTH_SHORT).show();
            info = getResources().getString(R.string.warning_location_title);
        }
        return result;
    }

    public void toggleIcon(){

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Intent launchMain = new Intent(getApplicationContext(), MainActivity.class);
        launchMain .setAction("android.intent.action.MAIN");
        launchMain .addCategory("android.intent.category.LAUNCHER");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, launchMain, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager iconMain = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder main_status_icon = new NotificationCompat.Builder(getApplicationContext(), "notify_001");
        main_status_icon.setAutoCancel(false);
        main_status_icon.setContentTitle(getResources().getString(R.string.service_icon_firstline));
        main_status_icon.setContentText(getResources().getString(R.string.service_icon_secondline));
        main_status_icon.setSmallIcon(R.mipmap.ic_launcher);
        main_status_icon.setContentIntent(pendingIntent);
        main_status_icon.setDefaults(0);
        main_status_icon.setChannelId("notify_001");
        main_status_icon.setPriority(NotificationCompat.PRIORITY_MIN);
        main_status_icon.setOngoing(true);
        main_status_icon.setWhen(0);
        //main_status_icon.setShowWhen(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel("notify_001", getResources().getString(R.string.notification_main),
                    NotificationManager.IMPORTANCE_MIN);
            channel.setShowBadge(false);
            iconMain.createNotificationChannel(channel);
        }

        if ( sharedPrefs.getBoolean("prefDisplayIcon", true) )
        {
            iconMain.notify(65150, main_status_icon.build());
        }

        else
        {
            iconMain.cancel(65150);
        }

        if (exit==1)
        {
            iconMain.cancel(65150);
        }
    }


    @Override
    public void onDestroy() {

        observer.unregisterContentObserver(obs);

        super.onDestroy();
        Intent startServiceIntent = new Intent(this.getBaseContext(), HistoryService.class);

        // TRY TO RESTART SERVICE ON SERVICE STOP
        try {
            this.getBaseContext().startService(startServiceIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Toast.makeText(getApplicationContext(),"serv inchis! ", Toast.LENGTH_LONG).show();

        /*	Notification notification_service_stop = new Notification(R.drawable.icon_status_red, "Contacts History",0);
        	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        	notification_service_stop.setLatestEventInfo(this, "Contacts History nu functioneaza.", "Noile contacte nu vor fi stocate in Contacts History.\nService-ul pe care se bazeaza aplicatia a fost inchis.", contentIntent);
        */

        exit = 1;
        toggleIcon();
        exit =0;

        tm.listen(numberReceiver, PhoneStateListener.LISTEN_NONE);

        observer.close();

    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }


}
