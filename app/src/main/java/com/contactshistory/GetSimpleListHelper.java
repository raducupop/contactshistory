package com.contactshistory;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collections;

public class GetSimpleListHelper {

    ArrayList<ContactHelperSimple> list = new ArrayList<>();

    public GetSimpleListHelper(){

        ArrayList<ContactHelperSimple> list = new ArrayList<>();

    }

    @SuppressLint("Range")
    ArrayList<String> getContactsSortedByID(Context context) {

        ArrayList<String> lista = null;

        try {

            lista = new ArrayList<>();
            String id;
            String nume;
            lista.clear();
            this.list.clear();

            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

            if ((cursor != null ? cursor.getCount() : 0) > 0) {
                while (cursor != null && cursor.moveToNext()) {
                       id = cursor.getString(
                            cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));

                       //Log.i("CH","Lookup key: --> "+id);

                       nume = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));

                       ContactHelperSimple contact = new ContactHelperSimple(id, nume);

                       lista.add(contact.toText());
                       list.add(contact);
                }
            }
            if(cursor!=null){
                cursor.close();
            }

        } catch (Exception e) {

        }

        Collections.reverse(list);
        Collections.reverse(lista);

        return lista;
    }

}
