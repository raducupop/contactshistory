package com.contactshistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.app.NotificationManager;
import android.app.Service;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.content.ContentResolver;
import android.content.ContentProviderOperation;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.provider.ContactsContract.CommonDataKinds;
import android.widget.Toast;


public class HistoryService extends Service{

    Cursor c, observer;
    public ContentObserver obs;
    int n=0,na=0;
    int contacts_count = 0;
    ArrayList<String> contacte = new ArrayList<String>();
    ArrayList<String> contacte_a = new ArrayList<String>();
    ArrayList<String> temp = new ArrayList<String>();

    String info =null;

    Boolean locationFound = false;

    int exit = 0;
    int edit = 1;

    @Override
    public void onCreate() {

        super.onCreate();

        // Toast.makeText(getApplicationContext(),"serv creat! ", Toast.LENGTH_LONG).show();

        Intent startServiceIntent = new Intent(this.getBaseContext(), HistoryService.class);
        this.getBaseContext().startService(startServiceIntent);

        c = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
        Cursor full_contacts = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        int contacts_count = full_contacts.getCount();

        n = c.getCount();
        contacte.clear();
        String id=null;

        if (c.getCount() > 0)
        {

            c.moveToFirst();
            do {
                id = c.getString(c.getColumnIndex(ContactsContract.RawContacts._ID));
                contacte.add(id);
            } while (c.moveToNext());
            c.close();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStart(intent, startId);

        // Toast.makeText(getApplicationContext(),"serv pornit! ", Toast.LENGTH_LONG).show();

        toggleIcon();


        CallListener phoneListener = new CallListener(getApplicationContext());
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);


        final DBAdapter db = new DBAdapter(this);

        observer = getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);


        obs = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {


                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Boolean ok_to_notify = false;
                NotificationManager changeNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification main_notification = new Notification(R.drawable.icon_change, "Contacts History", 0);
                Notification no_location_notification = new Notification(R.drawable.ic_location_off_white_24dp, "Contacts History", 0);



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
                int contacts_count_after = full_contacts_after.getCount();


                na = ca.getCount();
                String id_contact = null;
                contacte_a.clear();

                if (ca.getCount() > 0) {
                    ca.moveToFirst();
                    do {
                        id_contact = ca.getString(ca.getColumnIndex(ContactsContract.RawContacts._ID));
                        contacte_a.add(id_contact);
                    } while (ca.moveToNext());
                    //ca.close();
                }

                if ( (na > n) && (contacts_count != contacts_count_after) ) {

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
                                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
                                    Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
                                    //Log.d("ReadData","Notes count for contact: "+String.valueOf(noteCur.getCount()));

                                    if (noteCur.moveToFirst()) {

                                        db.open();

                                        do {

                                            String note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));

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
                                    //noteCur.close();
                                }
                            } while (read.moveToNext());

                        }
                        //read.close();


                    }

                    if (sharedPrefs.getBoolean("prefWriteData", true)) {

                        writeToContact(cidd, data_azi, getLocation().get(0), getLocation().get(1));
                    }

                    edit = 1;
                }

                if ( (na < n) && ( contacts_count != contacts_count_after)) {

                    temp = contacte;
                    temp.removeAll(contacte_a);

                    db.open();
                    db.deleteContact(temp.toString());
                    db.close();

                    info = "Contact sters.";
                    //Toast.makeText(getApplicationContext(),"Sters din Contacts History", Toast.LENGTH_SHORT).show();
                }



                if ((na == n) && (edit == 1)) {

                    ArrayList<String> old_id = new ArrayList<String>();
                    ArrayList<String> new_id = new ArrayList<String>();

                    old_id.addAll(contacte);
                    new_id.addAll(contacte_a);

                    old_id.removeAll(contacte_a);
                    new_id.removeAll(contacte);

                    if (old_id.size() > 0) {
                        //Toast.makeText(getApplicationContext(),"Contact editat in Contacts History", Toast.LENGTH_SHORT).show();
                        info = "Contact editat.";

                        db.open();
                        db.updateContact(old_id.toString(), new_id.toString());
                        //db.insertContact(new_id.toString(),data_azi,getLocation().get(0),getLocation().get(1));
                        db.close();

                    }


                }

                contacte.clear();
                contacte.addAll(contacte_a);
                n = ca.getCount();
                //ca.close();

                if ((ok_to_notify == true) && (sharedPrefs.getBoolean("prefDisplayIconContact", true))) {


                    if (locationFound) {
                        main_notification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.service_icon_contact), info, pendingIntent);
                        main_notification.flags = Notification.FLAG_AUTO_CANCEL;
                        changeNotificationManager.notify(65152, main_notification);
                    }

                    else {

                        no_location_notification.setLatestEventInfo(getApplicationContext(), getResources().getString(R.string.service_icon_contact), info, pendingIntent);
                        no_location_notification.flags = Notification.FLAG_AUTO_CANCEL;
                        changeNotificationManager.notify(65153, no_location_notification);

                    }


                }

                DBAdapter db_tmp = new DBAdapter(getBaseContext());
                db_tmp.open();
                Cursor db_ids =  db_tmp.getAllContacts();
                int found_id = 0;

                Long a=Long.valueOf(0);
                Long b=Long.valueOf(0);

                ArrayList<String> list_purge = new ArrayList<>();

                //Toast.makeText(getApplicationContext(),"In DB: "+String.valueOf(db_ids.getCount()), Toast.LENGTH_SHORT).show();
                Cursor all_ids = getBaseContext().getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);

                if (db_ids.getCount()>0)
                {
                    if(db_ids.moveToFirst())
                    {
                        do{

                            found_id = 0;
                            String id_db = db_ids.getString(0);

                            String id_db_ok = id_db.substring(1, id_db.length()-1);
                            try {
                                a=Long.valueOf(id_db_ok);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                            if(all_ids.getCount()>0)
                            {
                                if (all_ids.moveToFirst()){

                                    do {

                                        String id_provider_row = all_ids.getString(all_ids.getColumnIndex(ContactsContract.RawContacts._ID));
                                        try {
                                            b = Long.valueOf(id_provider_row);
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                        }

                                        if (a.equals(b)) found_id = 1;
                                    }while (all_ids.moveToNext());
                                }
                            }


                            if (found_id == 0){
                                list_purge.add(id_db);
                            }

                        }while (db_ids.moveToNext());

                    }

                }

                if (list_purge.size()>0){

                    for (int i=0;i<list_purge.size();i++){
                        //Toast.makeText(getApplicationContext(),"In purge: "+String.valueOf(list_purge.size()), Toast.LENGTH_SHORT).show();
                        db_tmp.deleteContact(list_purge.get(i));
                    }
                }

                db_tmp.close();


            }
            @Override
            public boolean deliverSelfNotifications() {
                return false;
            }
        };

        observer.registerContentObserver(obs);

        return(START_STICKY);
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

    public void writeToContact(String ID, String Date, String Geo, String Location)
    {


        if (ID.length()>0)
        {

            ArrayList<ContentProviderOperation> ops =
                    new ArrayList<ContentProviderOperation>();

            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, ID)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.Data.DATA1, "Contacts History Data |"+Date+"|"+Location+"|"+Geo)
                    .build());
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }

        }
    }

    public ArrayList<String> getLocation(){

        locationFound = false;

        ArrayList<String> result = new ArrayList<String>();
        Location location = null;

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo dcon = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected() || dcon.isConnected())
        {

            final LocationManager locationManager;
            String svcName = Context.LOCATION_SERVICE;
            locationManager = (LocationManager)getSystemService(svcName);


            final String provider = locationManager.NETWORK_PROVIDER;


            LocationListener myLocationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    location = locationManager.getLastKnownLocation(provider);


                    Log.d("contactshistory_service", "on location changed - update efectuat");


                }
                public void onProviderDisabled(String provider){

                }
                public void onProviderEnabled(String provider){
                }
                public void onStatusChanged(String provider, int status,Bundle extras){
                }
            };

            Looper looper = null;

            for(int i= 1; i<=10; i++)    //  refresh location
            {
                Log.d("contactshistory_service", "inainte de update");

                locationManager.requestSingleUpdate(provider, myLocationListener, looper);

                Log.d("contactshistory_service", "dupa request, inainte de get last");
            }

            location = locationManager.getLastKnownLocation(provider);

            locationManager.removeUpdates(myLocationListener);

            Geocoder geocoder;

            List<Address> user = null;
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
                    lat=(double)user.get(0).getLatitude();
                    lng=(double)user.get(0).getLongitude();

                    result.add(lat+"#"+lng);


                    if (user != null && user.size() > 0) {
                        Address address = user.get(0);

                        String addressText = String.format("%s, %s, %s",
                                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                                address.getLocality(),
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

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification n = new Notification(R.drawable.icon_status, "Contacts History",0);

        Intent launchMain = new Intent(getApplicationContext(),
                MainActivity.class);
        launchMain .setAction("android.intent.action.MAIN");
        launchMain .addCategory("android.intent.category.LAUNCHER");

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, launchMain, PendingIntent.FLAG_UPDATE_CURRENT);

        n.flags = Notification.FLAG_NO_CLEAR;
        n.setLatestEventInfo(this, getResources().getString(R.string.service_icon_firstline), getResources().getString(R.string.service_icon_secondline), pendingIntent);

        if ( sharedPrefs.getBoolean("prefDisplayIcon", true) )
        {
            notificationManager.notify(65151, n);
        }

        else
        {
            notificationManager.cancel(65151);
        }

        if (exit==1)
        {
            notificationManager.cancel(65151);
        }
    }


    @Override
    public void onDestroy() {

        observer.unregisterContentObserver(obs);

        super.onDestroy();

        Intent startServiceIntent = new Intent(this.getBaseContext(), HistoryService.class);


        // TRY TO RESTART SERVICE ON SERVICE STOP
        this.getBaseContext().startService(startServiceIntent);

        // Toast.makeText(getApplicationContext(),"serv inchis! ", Toast.LENGTH_LONG).show();

        /*	Notification notification_service_stop = new Notification(R.drawable.icon_status_red, "Contacts History",0);
        	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        	notification_service_stop.setLatestEventInfo(this, "Contacts History nu functioneaza.", "Noile contacte nu vor fi stocate in Contacts History.\nService-ul pe care se bazeaza aplicatia a fost inchis.", contentIntent);
        */

        exit = 1;
        toggleIcon();
        exit =0;

    }



    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
