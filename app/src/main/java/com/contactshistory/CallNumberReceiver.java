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

public class CallNumberReceiver extends PhoneStateListener {


    Context context;

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                // called when someone is ringing to this phone
                //Toast.makeText(MainActivity.appcontext, "Incoming: "+incomingNumber,Toast.LENGTH_LONG).show();

                context = MyApp.getContext();
                showInfo(incomingNumber, context);

        }
    }


    public void showInfo(String incomingNumber, Context context ) {

        //phone ringing
        //Toast.makeText(context, "ringing", Toast.LENGTH_LONG).show();
        Cursor cc = null;

        Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
        String[] projection = new String[]
                {
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.Contacts._ID,
                };
        String id;
        String sortOrder = ContactsContract.CommonDataKinds.StructuredPostal.DISPLAY_NAME_PRIMARY + " COLLATE LOCALIZED ASC";

        try {
            cc = context.getContentResolver().query(lookupUri, projection, null, null, sortOrder);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final DBAdapter db = new DBAdapter(context);
        db.open();

        //Toast.makeText(context,"in device pentru numar: "+String.valueOf(cursor_contacts.getCount()), Toast.LENGTH_LONG).show();


        if (cc != null && cc.getCount() > 0) {
            if (cc.moveToFirst()) {

                do {

                    id = cc.getString(cc.getColumnIndex(ContactsContract.Contacts._ID));

                    Cursor infoForId = db.getAllContacts();

                    //Toast.makeText(context,id+" in BD count : "+String.valueOf(infoForId.getCount()), Toast.LENGTH_LONG).show();

                    if (infoForId.getCount() > 0) {

                        if (infoForId.moveToFirst()) {

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                            SimpleDateFormat display_format = new SimpleDateFormat(prefs.getString("prefDateFormat", "dd MMMM yyyy") + " " + prefs.getString("prefTimeFormat", "HH:mm"));
                            SimpleDateFormat stored_format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            Date d = null;

                            do {

                                //Toast.makeText(context,id+" in bd ID :: "+infoForId.getString(0), Toast.LENGTH_LONG).show();

                                String idraw = getRawContactId(id, context);

                                //Toast.makeText(context,idraw, Toast.LENGTH_LONG).show();

                                if (infoForId.getString(0).substring(1, infoForId.getString(0).length() - 1).equals(idraw)) {

                                    try {
                                        d = stored_format.parse(infoForId.getString(1));
                                    } catch (ParseException e1) {

                                        e1.printStackTrace();
                                    }

                                    String date = display_format.format(d);

                                    String address;
                                    if (infoForId.getString(3).equalsIgnoreCase("null"))
                                        address = context.getResources().getString(R.string.helper_nolocation);
                                    else
                                        address = infoForId.getString(3);

                                    Toast.makeText(context, date + "\n" + address, Toast.LENGTH_LONG).show();

                                }

                            } while (infoForId.moveToNext());

                        }
                    }

                } while (cc.moveToNext());

            }
        }

        //cursor_contacts.close();
    }



    public String getRawContactId(String contactId, Context context)
    {
        String rawContactId = "";
        String[] projection=new String[]{ContactsContract.RawContacts._ID};
        String selection=ContactsContract.RawContacts.CONTACT_ID+"=?";
        String[] selectionArgs=new String[]{String.valueOf(contactId)};
        Cursor c=context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,projection,selection,selectionArgs , null);
        if (c.moveToFirst()) {
            rawContactId = c.getString(c.getColumnIndex(ContactsContract.RawContacts._ID));
        }

        return rawContactId;
    }






}