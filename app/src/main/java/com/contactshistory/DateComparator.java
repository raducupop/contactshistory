package com.contactshistory;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class DateComparator implements Comparator<ContactHelper> {
	
	public int compare(ContactHelper c1, ContactHelper c2) 
    {

        Context applicationContext = MainActivity.getAppContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        SimpleDateFormat ok_format = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        SimpleDateFormat stored_format = new SimpleDateFormat("dd/MM/yyyy HH:mm");

         Date d1 = null;
        try {
            d1 = (Date)stored_format.parse(c1.date);
        } catch (ParseException e1) {

            e1.printStackTrace();
        }
        String d1formated = ok_format.format(d1);


        Date d2 = null;
        try {
            d2 = (Date)stored_format.parse(c2.date);
        } catch (ParseException e1) {

            e1.printStackTrace();
        }
        String d2formated = ok_format.format(d2);



        return d1formated.compareToIgnoreCase(d2formated);
   }
}
