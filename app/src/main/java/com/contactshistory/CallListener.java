package com.contactshistory;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CallListener extends PhoneStateListener {

    Context context;
    Cursor cursor_contacts;

    public CallListener(Context ctx){
        context = ctx;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {


    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

    if (sharedPrefs.getBoolean("prefDisplayCallInfo", false) ){

       showInfo(state, incomingNumber);

    }


   }


     public void showInfo(int state, String incomingNumber ) {

     if (TelephonyManager.CALL_STATE_RINGING == state) {
         // phone ringing

         //Toast.makeText(context, "Intrare apel.", Toast.LENGTH_LONG).show();

         Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
         String[] projection = new String[]
                 {
                         // retrive name and id

                         ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                         ContactsContract.RawContacts._ID,
                 };
         String id = null;
         String name = null;
         String sortOrder = ContactsContract.CommonDataKinds.StructuredPostal.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC";

         cursor_contacts = context.getContentResolver().query(lookupUri, projection, null, null, sortOrder);

         final DBAdapter db = new DBAdapter(context);
         db.open();


         if (cursor_contacts.getCount() > 0) {
             if (cursor_contacts.moveToFirst()) {

                 do {


                     id = cursor_contacts.getString(cursor_contacts.getColumnIndex(ContactsContract.RawContacts._ID));

                     Cursor infoForId = db.getAllContacts();

                     if (infoForId.getCount() > 0) {

                         if (infoForId.moveToFirst()) {

                             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                             SimpleDateFormat display_format = new SimpleDateFormat(prefs.getString("prefDateFormat", "dd MMMM yyyy") + " " + prefs.getString("prefTimeFormat", "HH:mm"));
                             SimpleDateFormat stored_format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                             Date d = null;

                             do {

                                 if (infoForId.getString(0).substring(1, infoForId.getString(0).length()-1).equals(id)) {

                                     try {
                                         d = (Date) stored_format.parse(infoForId.getString(1));
                                     } catch (ParseException e1) {

                                         e1.printStackTrace();
                                     }

                                     String date = display_format.format(d);

                                     String address = "";
                                     if (infoForId.getString(3).equalsIgnoreCase("null"))
                                         address = context.getResources().getString(R.string.helper_nolocation);
                                     else
                                         address = infoForId.getString(3);

                                     Toast.makeText(context, date + "\n" + address, Toast.LENGTH_LONG).show();

                                 }

                             } while (infoForId.moveToNext());

                         }
                     }

                 } while (cursor_contacts.moveToNext());

             }
         }

         //cursor_contacts.close();

     }

     if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
         // active
         //Toast.makeText(MainActivity.getAppContext(), "S-a raspuns la apel.", Toast.LENGTH_LONG).show();

     }

 }

}