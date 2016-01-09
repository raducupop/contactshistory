package com.contactshistory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;


public class GetListHelper {

	ArrayList<ContactHelper> list = new ArrayList<ContactHelper>();
	
	public GetListHelper(){
		
		ArrayList<ContactHelper> list = new ArrayList<ContactHelper>();
		
	}
	
    public GetListHelper(ArrayList<ContactHelper> l){
		
		list.clear();
		list.addAll(l);
	}
	
	
	public ArrayList<String> getList(String d1, String d2, Context context)
	{
		
	    ArrayList<String> lista_contacte = new ArrayList<String>();
	    ArrayList<String> tmp = new ArrayList<String>();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date min = null;
		try {
			min = (Date)formatter.parse(d1);
		} catch (ParseException e1) {

			e1.printStackTrace();
		}

        Date max = null;
		try {
			max = (Date)formatter.parse(d2);
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
                		  Date d = (Date)formatter.parse(c.getString(1));
     
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
            
    		Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
            Cursor raw = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);

    		String id=null;
    		String nume=null;
    	    String data=null;
    	    String locatie=null;
    	    String adresa=null;

            String idraw="";
            String account="";

    	    lista_contacte.clear();
    	    list.clear();
    	    
    	    cursor.moveToFirst();

    	    do {
    	    	nume = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
    	    	id = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                idraw = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));


    	    	if (tmp.contains(id))
    	    	{
    	    		if (c.moveToFirst()) {
                        do {

                            if (c.getString(0).contains(id)) {
                                data = c.getString(1);
                                locatie = c.getString(2);
                                adresa = c.getString(3);

                                ContactHelper contact = new ContactHelper(id, nume, data, locatie, adresa);

                                Log.v(" key | id ", id + " | " + idraw);


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
                                            Log.v("ch","Contact on DEVICE");
                                            list.add(contact);
                                        }

                                        if (account.toLowerCase().contains("sim") && sharedPrefs.getBoolean("prefSourceSim",true) )
                                        {
                                            Log.v("ch","Contact on SIM");
                                            list.add(contact);
                                        }

                                        if ( (!account.toLowerCase().contains("phone") && !account.toLowerCase().contains("sim")) && sharedPrefs.getBoolean("prefSourceAccounts",true) )
                                        {
                                            Log.v("ch","Contact on OTHER ACCOUNT");
                                            list.add(contact);
                                        }
                                    }

                                    else{
                                        list.add(contact);
                                    }
                                }

                            }
                        } while (c.moveToNext());
                    }
    	 	
    	    	}
    	    		
    	    } while (cursor.moveToNext());
    	    db.close();

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

	    return lista_contacte;
	}
	

	public ArrayList<String> getRecent(int n, Context context)
	{
		
	    ArrayList<String> lista_recente = new ArrayList<String>();
	    ArrayList<String> tmp = new ArrayList<String>();
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
            
    		Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
            Cursor raw = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);

            String id=null;
    		String nume=null;
    	    String data=null;
    	    String locatie=null;
    	    String adresa=null;

            String idraw="";

            String account=null;

    	    lista_recente.clear();
    	    list.clear();
    	    
    	    cursor.moveToFirst();
    	    do {
    	    	nume = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
    	    	id = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                idraw = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));

                if (tmp.contains(id))
    	    	{
    	    		if (c.moveToFirst())
    	            do { 

    	                	if (c.getString(0).contains(id) )
    	                	{
    	                	   data = c.getString(1);
    	                	   locatie = c.getString(2);
    	                	   adresa = c.getString(3);
    	                	   
    	                	   ContactHelper contact = new ContactHelper(id, nume, data, locatie, adresa);
                                Log.v(" key | id ", id + " | " + idraw);


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
                                            Log.v("ch","Contact on DEVICE");
                                            list.add(contact);
                                        }

                                        if (account.toLowerCase().contains("sim") && sharedPrefs.getBoolean("prefSourceSim",true) )
                                        {
                                            Log.v("ch","Contact on SIM");
                                            list.add(contact);
                                        }

                                        if ( (!account.toLowerCase().contains("phone") && !account.toLowerCase().contains("sim")) && sharedPrefs.getBoolean("prefSourceAccounts",true) )
                                        {
                                            Log.v("ch","Contact on OTHER ACCOUNT");
                                            list.add(contact);
                                        }
                                    }

                                    else{
                                        list.add(contact);
                                    }
                                }
    	                	   
    	                	}
    	                } while (c.moveToNext());
    	 	
    	    	}
    	    		
    	    } while (cursor.moveToNext());
    	    db.close();
   
        }
        
        Collections.sort(list, new DateComparator());
        Collections.reverse(list);

		ArrayList<ContactHelper> temp = new ArrayList<ContactHelper>();
    	
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
   	       	    
     return lista_recente;
	}



    public ArrayList<String> getByLocation(String location, Context context)
    {

        Log.d("location"," INSIDE GET BY LOCATION METHOD , parameter: "+location);

        ArrayList<String> lista_contacte = new ArrayList<String>();
        ArrayList<String> tmp = new ArrayList<String>();
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

            Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);
            Cursor raw = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, null, null, null, null);

            String id=null;
            String nume=null;
            String data=null;
            String locatie=null;
            String adresa=null;

            String idraw="";
            String account="";

            lista_contacte.clear();
            list.clear();

            cursor.moveToFirst();

            do {
                nume = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY));
                id = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));
                idraw = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID));


                if (tmp.contains(id))
                {
                    if (c.moveToFirst()) {
                        do {

                            if (c.getString(0).contains(id)) {
                                data = c.getString(1);
                                locatie = c.getString(2);
                                adresa = c.getString(3);

                                ContactHelper contact = new ContactHelper(id, nume, data, locatie, adresa);

                                Log.v(" key | id ", id + " | " + idraw);


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
                                            Log.v("ch","Contact on DEVICE");
                                            list.add(contact);
                                        }

                                        if (account.toLowerCase().contains("sim") && sharedPrefs.getBoolean("prefSourceSim",true) )
                                        {
                                            Log.v("ch","Contact on SIM");
                                            list.add(contact);
                                        }

                                        if ( (!account.toLowerCase().contains("phone") && !account.toLowerCase().contains("sim")) && sharedPrefs.getBoolean("prefSourceAccounts",true) )
                                        {
                                            Log.v("ch","Contact on OTHER ACCOUNT");
                                            list.add(contact);
                                        }
                                    }

                                    else{
                                        list.add(contact);
                                    }
                                }

                            }
                        } while (c.moveToNext());
                    }

                }

            } while (cursor.moveToNext());
            db.close();

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

        return lista_contacte;
    }


    public ArrayList<String> sortByName(ArrayList<ContactHelper> lst){
		
		ArrayList<String> sorted = new ArrayList<String>();

		sorted.clear();
		Collections.sort(lst, new NameComparator());
		for (int i=0; i<lst.size() ; i++)
	            {
	    	      sorted.add(lst.get(i).toText());
	             }

		return sorted;	
	}
	
	
	public ArrayList<String> sortByDate(ArrayList<ContactHelper> lst){
		
		ArrayList<String> sorted = new ArrayList<String>();

		sorted.clear();
		Collections.sort(lst, new DateComparator());
		Collections.reverse(lst);
		
		for (int i=0; i<lst.size() ; i++)
	        {
	    	    sorted.add(lst.get(i).toText());
	        }
		return sorted;	
	}


	public ArrayList<String> sortByLocation(ArrayList<ContactHelper> lst){
	
		ArrayList<String> sorted = new ArrayList<String>();

		sorted.clear();
		ArrayList<ContactHelper> with_location = new ArrayList<ContactHelper>();
		ArrayList<ContactHelper> no_location = new ArrayList<ContactHelper>();
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
	
	
	public ArrayList<String> filterWithLocation(ArrayList<ContactHelper> lst){
		
		ArrayList<String> filtered = new ArrayList<String>();
		filtered.clear();
		
		for (int i = 0; i < lst.size(); i++)
  	      {
  	    	  if (!(lst.get(i).address.equalsIgnoreCase("null")))
	  
  	    		  filtered.add(lst.get(i).toText());
  	      }

		return filtered;	
	
	}
	
	public ArrayList<String> filterAll (ArrayList<ContactHelper> lst){
		
		ArrayList<String> all = new ArrayList<String>();
		all.clear();
		
		for (int i = 0; i < lst.size(); i++)
  	    all.add(lst.get(i).toText());
  	  
		return all;	
	
	}
	
     public ArrayList<String> searchList(ArrayList<ContactHelper> lst, String query){
		
		ArrayList<String> result = new ArrayList<String>();
		result.clear();
		
		for (int i = 0; i < lst.size(); i++)
  	    if  (lst.get(i).toText().toLowerCase().contains(query))
		result.add(lst.get(i).toText());
  	  
		return result;	
	
	}

     public ArrayList<String> resetSearch(ArrayList<ContactHelper> lst){
 		
 		ArrayList<String> originalList = new ArrayList<String>();
 		originalList.clear();
 		
 		for (int i=0; i<lst.size() ; i++)
 	            {
 			       originalList.add(lst.get(i).toText());
 	            }

 		return originalList;	
 	} 
     

}
