package com.contactshistory;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class CustomList extends ArrayAdapter<String> {

	private final Activity context;
	ArrayList<String> contact_list = new ArrayList<String>();

	Uri photoURI = null;

	public CustomList(Activity context, ArrayList<String> contact_list) {

		super(context, R.layout.item, contact_list);
		this.context = context;
		this.contact_list = contact_list;

	}

	@Override
	public View getView(int i, View view, ViewGroup parent) {

		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.item, null, true);
		TextView textName = (TextView) rowView.findViewById(R.id.text_name);
		TextView textDate = (TextView) rowView.findViewById(R.id.text_date);
		TextView textLocation = (TextView) rowView.findViewById(R.id.text_location);
		TextView textTime = (TextView) rowView.findViewById(R.id.text_time);
		ImageView contactPhoto = (ImageView) rowView.findViewById(R.id.photo);

		String fulltext = contact_list.get(i);
		String list_fields[] = fulltext.split("\\r?\\n");

		textName.setText(list_fields[1]);
		textDate.setText(list_fields[2]);
		textLocation.setText(list_fields[3]);
		textTime.setText(list_fields[4]);


		Long id_by_name = retrieveContactID(context, list_fields[1]);
		photoURI = getPhotoURI(context, id_by_name);



		if (photoURI!= null) {
			contactPhoto.setImageURI(photoURI);
		} else {

			Uri nophoto = Uri.parse("android.resource://com.contactshistory/drawable/nophoto");
			contactPhoto.setImageURI(nophoto);

		}

		return rowView;

	}

	public static long retrieveContactID(Context context, String nume) {
		Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(nume.trim()));
		Cursor mapContact = context.getContentResolver().query(uri, new String[]{ContactsContract.PhoneLookup._ID}, null, null, null);
		String id;
		if (mapContact.moveToNext()) {
			id = mapContact.getString(mapContact.getColumnIndex(ContactsContract.Contacts._ID));

		} else id = "0";

		return Long.parseLong(id);
	}

	public Uri getPhotoURI(Context ctx, Long id_contact) {

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