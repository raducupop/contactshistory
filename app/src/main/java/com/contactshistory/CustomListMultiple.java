package com.contactshistory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListMultiple extends ArrayAdapter<String>{

    boolean[] checkBoxState;

    private ViewHolder viewHolder;
    Boolean checkAllFlag;
    Boolean uncheckAllFlag;

	private final Activity context;
	private ArrayList<String> contact_list;
	
	String name, date, time;

    Bitmap photo = null;

	CustomListMultiple(Activity context, ArrayList<String> contact_list) {

		super(context, R.layout.item, contact_list);
		this.context = context;
		this.contact_list = contact_list;

        checkBoxState=new boolean[contact_list.size()];

		Log.v("Debug", ""+contact_list.size());

        checkAllFlag = false;
        uncheckAllFlag = false;
    }


    private class ViewHolder
    {

        CheckBox checkBox;
        TextView textName, textDate, textLocation, textTime;
        ImageView contactPhoto;
    }

	@Override
	public View getView(final int i, View view, ViewGroup parent) {


        if(view==null)
        {
            LayoutInflater inflater = context.getLayoutInflater();
            view=inflater.inflate(R.layout.item_multiple, null);
            viewHolder=new ViewHolder();

            viewHolder.checkBox= view.findViewById(R.id.checkBox1);
            viewHolder.textName= view.findViewById(R.id.text_name);
            viewHolder.textDate= view.findViewById(R.id.text_date);
            viewHolder.textLocation= view.findViewById(R.id.text_location);
            viewHolder.textTime= view.findViewById(R.id.text_time);
            viewHolder.contactPhoto = view.findViewById(R.id.photo);

            view.setTag(viewHolder);

        }
        else
            viewHolder=(ViewHolder) view.getTag();

		String fulltext = contact_list.get(i);
		String list_fields[] = fulltext.split("\\r?\\n");


        viewHolder.textName.setText(list_fields[1]);
        viewHolder.textDate.setText(list_fields[2]);
        viewHolder.textLocation.setText(list_fields[3]);
        viewHolder.textTime.setText(list_fields[4]);


        Long id_by_name = retrieveContactID(context, list_fields[1]);
        Uri photoURI = getPhotoURI(context, id_by_name);


        if (photoURI != null) {

            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(photoURI);
                Drawable draw = Drawable.createFromStream(inputStream, photoURI.toString() );
                viewHolder.contactPhoto.setImageDrawable(draw);
            } catch (FileNotFoundException e) {
                System.out.println("Error " + e.getMessage());
            }

        } else {

            Uri nophoto = Uri.parse("android.resource://com.contactshistory/drawable/nophoto");

            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(nophoto);
                Drawable draw = Drawable.createFromStream(inputStream, nophoto.toString() );
                viewHolder.contactPhoto.setImageDrawable(draw);
            } catch (FileNotFoundException e) {
                System.out.println("Error " + e.getMessage());
            }

        }


        viewHolder.checkBox.setChecked(checkBoxState[i]);

        if (checkAllFlag)
        {
            checkBoxState[i]=true;
            viewHolder.checkBox.setChecked(true);

        }

        if (uncheckAllFlag)
        {
            checkBoxState[i]=false;
            viewHolder.checkBox.setChecked(false);

        }

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                checkBoxState[i] = ((CheckBox) v).isChecked();
            }
        });

        if (i==(contact_list.size()-1)) {
            checkAllFlag = false;
            uncheckAllFlag = false;
        }

		return view;
		
	}

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
