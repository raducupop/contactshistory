package com.contactshistory;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CustomListSimple extends ArrayAdapter<String> {

    private final FragmentActivity context;
    private ArrayList<String> contact_list;

    CustomListSimple(FragmentActivity context, ArrayList<String> contact_list) {

        super(context, R.layout.item_simple, contact_list);
        this.context = context;
        this.contact_list = contact_list;

    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.item_simple, null, true);
        TextView textName = rowView.findViewById(R.id.text_name_all);
        ImageView contactPhoto = rowView.findViewById(R.id.photo_all);

        String fulltext = contact_list.get(i);
        String list_fields[] = fulltext.split("\\r?\\n");
        textName.setText(list_fields[1]);

        //textName.setText(list_fields[1]);
        Long id_by_name = retrieveContactID(context, list_fields[1]);
        Uri photoURI = getPhotoURI(context, id_by_name);

        if (photoURI != null) {

            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(photoURI);
                Drawable draw = Drawable.createFromStream(inputStream, photoURI.toString() );
                contactPhoto.setImageDrawable(draw);
            } catch (FileNotFoundException e) {
                //
            }

        } else {

            Uri nophoto = Uri.parse("android.resource://com.contactshistory/drawable/nophoto");

            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(nophoto);
                Drawable draw = Drawable.createFromStream(inputStream, nophoto.toString() );
                contactPhoto.setImageDrawable(draw);

            } catch (FileNotFoundException e) {
                System.out.println("Error " + e.getMessage());
            }

        }
        return rowView;

    }

    @SuppressLint("Range")
    private static long retrieveContactID(Context context, String nume) {
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(nume.trim()));
        Cursor mapContact = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
        String id;
        if (mapContact.moveToNext()) {
            id = mapContact.getString(mapContact.getColumnIndex(ContactsContract.Contacts._ID));

        } else id = "0";

        mapContact.close();
        return Long.parseLong(id);
    }

    private Uri getPhotoURI(Context ctx, Long id_contact) {

        Uri contact = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id_contact);
        Uri picUri = Uri.withAppendedPath(contact,ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        try {
            InputStream is = ctx.getContentResolver().openInputStream(picUri);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return picUri;
    }

}