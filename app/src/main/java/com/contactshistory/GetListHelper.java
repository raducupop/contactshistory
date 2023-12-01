package com.contactshistory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class GetListHelper {

	ArrayList<ContactHelper> list = new ArrayList<>();

	public GetListHelper(){

		ArrayList<ContactHelper> list = new ArrayList<>();

	}
	
    public GetListHelper(ArrayList<ContactHelper> l){
		
		list.clear();
		list.addAll(l);
	}
	
	
	@SuppressLint("Range")
    public ArrayList<String> getList(String d1, String d2, Context context)
	{

        ArrayList<String> lista_contacte = null;
        try {
            lista_contacte = new ArrayList<>();
            ArrayList<String> tmp = new ArrayList<>();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date min = null;
            try {
                min = formatter.parse(d1);
            } catch (ParseException e1) {

                e1.printStackTrace();
            }

            Date max = null;
            try {
                max = formatter.parse(d2);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }

            final DBAdapter db = new DBAdapter(context);
            db.open();
            Cursor c = db.getAllContacts();

            if (c.getCount()>0)
            {
                if (c.moveToFirst())
                {
                    tmp.clear();
                    do {

                          try{
                              Date d = formatter.parse(c.getString(1));

                              if (d.compareTo(min) >= 0 && d.compareTo(max) <= 0)
                              {
                                  tmp.add(c.getString(0).substring(1, c.getString(0).length()-1)); // removes [ ] at start and at the end of id
                              }
                          }
                          catch (ParseException e) {
                                e.printStackTrace();
                            }

                    } while (c.moveToNext());


                }

                Cursor cursor = null;
                Cursor raw = null;
                try {
                    cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
                    raw = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
                } catch (Exception e) {

                }

                String id="";
                String nume="";
                String data="";
                String locatie="";
                String adresa="";

                String idraw="";
                String account="";

                lista_contacte.clear();
                list.clear();

                assert cursor != null;

                try {
                    cursor.moveToFirst();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    do {
                        try {
                            nume = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
                            id = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                            idraw = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                        } catch (Exception e) {

                        }


                        if (tmp.contains(id))
                        {
                            if (c.moveToFirst()) {
                                try {
                                    do {

                                        if (c.getString(0).substring(1, c.getString(0).length() - 1).equalsIgnoreCase(id)) {
                                            data = c.getString(1);
                                            locatie = c.getString(2);
                                            adresa = c.getString(3);

                                            ContactHelper contact = new ContactHelper(id, nume, data, locatie, adresa);

                                            Log.v(" key | id ", id + " | " + idraw);


                                            assert raw != null;
                                            if (raw.moveToFirst())
                                            {
                                                do {
                                                    String raw_id = null;
                                                    try {
                                                        raw_id = raw.getString(raw.getColumnIndex(ContactsContract.RawContacts._ID));
                                                    } catch (Exception e) {

                                                    }


                                                    try {
                                                        if (idraw.contentEquals(raw_id))
                                                        {
                                                            account = raw.getString(raw.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                                                            Log.v("ch",raw_id);
                                                            Log.v("ch",account);
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                } while (raw.moveToNext());

                                                if (account.length()>0)
                                                {
                                                    if (account.toLowerCase().contains("phone") && sharedPrefs.getBoolean("prefSourceDevice",true) )
                                                    {
                                                        Log.v("ch", "Contact on DEVICE");
                                                        if (!list.contains(contact))
                                                        list.add(contact);
                                                    }

                                                    if (account.toLowerCase().contains("sim") && sharedPrefs.getBoolean("prefSourceSim",true) )
                                                    {
                                                        Log.v("ch", "Contact on SIM");
                                                        if (!list.contains(contact))
                                                        list.add(contact);
                                                    }

                                                    if ( (!account.toLowerCase().contains("phone") && !account.toLowerCase().contains("sim")) && sharedPrefs.getBoolean("prefSourceAccounts",true) )
                                                    {
                                                        Log.v("ch", "Contact on OTHER ACCOUNT");
                                                        if (!list.contains(contact))
                                                        list.add(contact);
                                                    }
                                                }

                                                else{
                                                    if (!list.contains(contact))
                                                    list.add(contact);
                                                }
                                            }

                                        }
                                    } while (c.moveToNext());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                    } while (cursor.moveToNext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    db.close();
                    c.close();
                    assert raw != null;
                    raw.close();
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /// default sort - preference

                if ( Integer.valueOf(sharedPrefs.getString("prefDefaultSort","0")) == 0)
                {
                    Collections.sort(list, new DateComparator());
                    Collections.reverse(list);
                }
                if ( Integer.valueOf(sharedPrefs.getString("prefDefaultSort","0")) == 1)
                    Collections.sort(list, new NameComparator());

                if ( Integer.valueOf(sharedPrefs.getString("prefDefaultSort","0")) == 2)
                    Collections.sort(list, new LocationComparator());

                for (int i=0; i<list.size() ; i++)
                {
                    lista_contacte.add(list.get(i).toText());
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {

        }

        return lista_contacte;
	}
	

	@SuppressLint("Range")
    ArrayList<String> getRecent(int n, Context context)
	{

        ArrayList<String> lista_recente = null;
        try {
            lista_recente = new ArrayList<>();
            ArrayList<String> tmp = new ArrayList<>();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());


            final DBAdapter db = new DBAdapter(context);
            db.open();
            Cursor c = db.getAllContacts();

            if (c.getCount()>0)
            {
                if (c.moveToFirst())
                {
                    tmp.clear();
                    do {

                        tmp.add(c.getString(0).substring(1, c.getString(0).length()-1)); // removes [ ] at start and at the end of id

                        } while (c.moveToNext());

                }

                Cursor cursor = null;
                Cursor raw = null;
                try {
                    cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
                    raw = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
                } catch (Exception e) {
                    e.printStackTrace();

                }

                String id="";
                String nume="";
                String data="";
                String locatie;
                String adresa;

                String idraw="";

                String account=null;

                lista_recente.clear();
                list.clear();

                assert cursor != null;
                try {
                    cursor.moveToFirst();
                } catch (Exception e) {

                }
                try {
                    do {
                        try {
                            nume = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
                            id = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                            idraw = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                        } catch (Exception e) {
                            e.printStackTrace();

                        }

                        if (tmp.contains(id))
                        {
                            if (c.moveToFirst())
                            do {

                                if (c.getString(0).substring(1, c.getString(0).length() - 1).equalsIgnoreCase(id) )
                                    {
                                       data = c.getString(1);
                                       locatie = c.getString(2);
                                       adresa = c.getString(3);

                                       ContactHelper contact = new ContactHelper(id, nume, data, locatie, adresa);
                                        Log.v(" key | id ", id + " | " + idraw);


                                        assert raw != null;
                                        if (raw.moveToFirst())
                                        {
                                            do {
                                                String raw_id = raw.getString(raw.getColumnIndex(ContactsContract.RawContacts._ID));


                                                try {
                                                    if (idraw.contentEquals(raw_id))
                                                    {
                                                        account = raw.getString(raw.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                                                        Log.v("ch",raw_id);
                                                        Log.v("ch",account);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } while (raw.moveToNext());

                                            assert account != null;
                                            if (account.length()>0)
                                            {
                                                if (account.toLowerCase().contains("phone") && sharedPrefs.getBoolean("prefSourceDevice",true) )
                                                {
                                                    Log.v("ch", "Contact on DEVICE");
                                                    if (!list.contains(contact))
                                                    list.add(contact);
                                                }

                                                if (account.toLowerCase().contains("sim") && sharedPrefs.getBoolean("prefSourceSim",true) )
                                                {
                                                    Log.v("ch", "Contact on SIM");
                                                    if (!list.contains(contact))
                                                    list.add(contact);
                                                }

                                                if ( (!account.toLowerCase().contains("phone") && !account.toLowerCase().contains("sim")) && sharedPrefs.getBoolean("prefSourceAccounts",true) )
                                                {
                                                    Log.v("ch", "Contact on OTHER ACCOUNT");
                                                    if (!list.contains(contact))
                                                    list.add(contact);
                                                }
                                            }

                                            else{
                                                if (!list.contains(contact))
                                                list.add(contact);
                                            }
                                        }

                                    }
                                } while (c.moveToNext());

                        }

                    }

                    while (cursor.moveToNext());
                } catch (Exception e) {
                    e.printStackTrace();

                }
                try {
                    db.close();
                    c.close();
                    assert raw != null;
                    raw.close();
                    cursor.close();
                } catch (Exception e) {

                }

            }

            Collections.sort(list, new DateComparator());
            Collections.reverse(list);

            ArrayList<ContactHelper> temp = new ArrayList<>();

            if (list.size()<=n)
               for (int i=0; i<list.size() ; i++)
            {
                lista_recente.add(list.get(i).toText());
            }

            if (list.size()>n)
            {
                for (int i=0; i<n ; i++)
                {
                    lista_recente.add(list.get(i).toText());
                    temp.add(list.get(i));
                }
                list.clear();
                list.addAll(temp);
                temp.clear();
            }
        } catch (SQLException e) {

        }

        return lista_recente;
	}


    @SuppressLint("Range")
    ArrayList<String> getByLocation(String location, Context context)
    {
        ArrayList<String> lista_contacte = null;

        try {
            //Log.d("location"," INSIDE GET BY LOCATION METHOD , parameter: "+location);

            lista_contacte = new ArrayList<>();
            ArrayList<String> tmp = new ArrayList<>();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());


            final DBAdapter db = new DBAdapter(context);
            db.open();
            Cursor c = db.getAllContacts();

            if (c.getCount()>0)
            {
                if (c.moveToFirst())
                {
                    tmp.clear();
                    do {
                            Log.d("location",c.getString(3));

                            if (c.getString(3).toLowerCase().contains(location))
                            {
                                Log.d("location",c.getString(3)+" inside IF");

                                tmp.add(c.getString(0).substring(1, c.getString(0).length()-1)); // removes [ ] at start and at the end of id
                            }

                    } while (c.moveToNext());

                }

                Cursor cursor = null;
                Cursor raw = null;
                try {
                    cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
                    raw = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String id;
                String nume;
                String data;
                String locatie;
                String adresa;

                String idraw;
                String account="";

                lista_contacte.clear();
                list.clear();

                assert cursor != null;
                try {
                    cursor.moveToFirst();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    do {
                        nume = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
                        id = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                        idraw = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));


                        if (tmp.contains(id))
                        {
                            if (c.moveToFirst()) {
                                do {

                                    if (c.getString(0).substring(1, c.getString(0).length()-1).equalsIgnoreCase(id)) {
                                        data = c.getString(1);
                                        locatie = c.getString(2);
                                        adresa = c.getString(3);

                                        ContactHelper contact = new ContactHelper(id, nume, data, locatie, adresa);

                                        Log.v(" key | id ", id + " | " + idraw);


                                        assert raw != null;
                                        if (raw.moveToFirst())
                                        {
                                            do {
                                                String raw_id = raw.getString(raw.getColumnIndex(ContactsContract.RawContacts._ID));


                                                try {
                                                    if (idraw.contentEquals(raw_id))
                                                    {
                                                        account = raw.getString(raw.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                                                        Log.v("ch",raw_id);
                                                        Log.v("ch",account);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } while (raw.moveToNext());

                                            if (account.length()>0)
                                            {
                                                if (account.toLowerCase().contains("phone") && sharedPrefs.getBoolean("prefSourceDevice",true) )
                                                {
                                                    Log.v("ch", "Contact on DEVICE");
                                                    if (!list.contains(contact))
                                                    list.add(contact);
                                                }

                                                if (account.toLowerCase().contains("sim") && sharedPrefs.getBoolean("prefSourceSim",true) )
                                                {
                                                    Log.v("ch", "Contact on SIM");
                                                    if (!list.contains(contact))
                                                    list.add(contact);
                                                }

                                                if ( (!account.toLowerCase().contains("phone") && !account.toLowerCase().contains("sim")) && sharedPrefs.getBoolean("prefSourceAccounts",true) )
                                                {
                                                    Log.v("ch", "Contact on OTHER ACCOUNT");
                                                    if (!list.contains(contact))
                                                    list.add(contact);
                                                }
                                            }

                                            else{
                                                if (!list.contains(contact))
                                                list.add(contact);
                                            }
                                        }

                                    }
                                } while (c.moveToNext());
                            }

                        }

                    } while (cursor.moveToNext());
                    db.close();

                    c.close();
                    assert raw != null;
                    raw.close();
                    cursor.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                /// default sort - preference

                if ( Integer.valueOf(sharedPrefs.getString("prefDefaultSort","0")) == 0)
                {
                    Collections.sort(list, new DateComparator());
                    Collections.reverse(list);
                }
                if ( Integer.valueOf(sharedPrefs.getString("prefDefaultSort","0")) == 1)
                    Collections.sort(list, new NameComparator());

                if ( Integer.valueOf(sharedPrefs.getString("prefDefaultSort","0")) == 2)
                    Collections.sort(list, new LocationComparator());

                for (int i=0; i<list.size() ; i++)
                {
                    lista_contacte.add(list.get(i).toText());
                }

            }

            if (location.isEmpty())
            {
                lista_contacte.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {

        }

        return lista_contacte;
    }

    ArrayList<String> sortByName(ArrayList<ContactHelper> lst){
		
		ArrayList<String> sorted = new ArrayList<>();

		sorted.clear();
		Collections.sort(lst, new NameComparator());
		for (int i=0; i<lst.size() ; i++)
	            {
	    	      sorted.add(lst.get(i).toText());
	             }

		return sorted;	
	}
	
	ArrayList<String> sortByDate(ArrayList<ContactHelper> lst){
		
		ArrayList<String> sorted = new ArrayList<>();

		sorted.clear();
		Collections.sort(lst, new DateComparator());
		Collections.reverse(lst);
		
		for (int i=0; i<lst.size() ; i++)
	        {
	    	    sorted.add(lst.get(i).toText());
	        }
		return sorted;	
	}


	ArrayList<String> sortByLocation(ArrayList<ContactHelper> lst){
	
		ArrayList<String> sorted = new ArrayList<>();

		sorted.clear();
		ArrayList<ContactHelper> with_location = new ArrayList<>();
		ArrayList<ContactHelper> no_location = new ArrayList<>();
		with_location.clear();
		no_location.clear();
		
		
		for (int i = 0; i < lst.size(); i++)
  	      {
  	    	  if (lst.get(i).address.equalsIgnoreCase("null"))
  	    		  no_location.add(lst.get(i));
  	    	  else
  	    		  with_location.add(lst.get(i));
  	      }
		

	    Collections.sort(with_location, new LocationComparator());
		Collections.sort(no_location, new NameComparator());
		
		lst.clear();
		lst.addAll(with_location);
		lst.addAll(no_location);

		for (int i=0; i<lst.size() ; i++)
	            {
	    	      sorted.add(lst.get(i).toText());
	            }
		
		return sorted;	
	}
	
	
	ArrayList<String> filterWithLocation(ArrayList<ContactHelper> lst){
		
		ArrayList<String> filtered = new ArrayList<>();
		filtered.clear();
		
		for (int i = 0; i < lst.size(); i++)
  	      {
  	    	  if (!(lst.get(i).address.equalsIgnoreCase("null")))
	  
  	    		  filtered.add(lst.get(i).toText());
  	      }

		return filtered;	
	
	}
	
	ArrayList<String> filterAll (ArrayList<ContactHelper> lst){
		
		ArrayList<String> all = new ArrayList<>();
		all.clear();
		
		for (int i = 0; i < lst.size(); i++)
  	    all.add(lst.get(i).toText());
  	  
		return all;	
	
	}
	
     ArrayList<String> searchList(ArrayList<ContactHelper> lst, String query){
		
		ArrayList<String> result = new ArrayList<>();
		result.clear();
		
		for (int i = 0; i < lst.size(); i++)
  	    if  (lst.get(i).toText().toLowerCase().contains(query))
		result.add(lst.get(i).toText());
  	  
		return result;	
	
	}

     ArrayList<String> resetSearch(ArrayList<ContactHelper> lst){
 		
 		ArrayList<String> originalList = new ArrayList<>();
 		originalList.clear();
 		
 		for (int i=0; i<lst.size() ; i++)
 	            {
 			       originalList.add(lst.get(i).toText());
 	            }

 		return originalList;	
 	} 
     

}
