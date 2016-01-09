package com.contactshistory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;


public class ContactHelper implements Parcelable {

	String id = null;
	String name = null;
	String date = null;
	String location = null;
	String address = null;



	public ContactHelper(){
		id = null;
		name = null;
		date = null;
		location = null;
		address = null;
			
	}
	
	public ContactHelper(String i, String n, String d, String l, String a){
		id = i;
		name = n;
		date = d;
		location = l;
		address = a;
			
	}


    public String toText(){

    	

        Context applicationContext = MainActivity.getAppContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        SimpleDateFormat display_format = new SimpleDateFormat(prefs.getString("prefDateFormat","dd MMMM yyyy")+" "+prefs.getString("prefTimeFormat","HH:mm"));

        
        SimpleDateFormat stored_format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        
		Date d = null;
		try {
			d = (Date)stored_format.parse(date);
		} catch (ParseException e1) {

			e1.printStackTrace();
		}
		String date_display = display_format.format(d);
		
		DateHelper datehelper = new DateHelper();
        String relativeTime = datehelper.getTimeAgo(d, applicationContext);

        String output = null;

		if (address.equals("null"))

			output = id+"\n"+name+"\n"+date_display+"\n"+applicationContext.getResources().getString(R.string.helper_nolocation)+"\n"+relativeTime;
		else
			output = id+"\n"+name+"\n"+date_display+"\n"+address+"\n"+relativeTime;
		
		
		
		return output;
	}
	

    public int describeContents() {
        return 0;
    }
	
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(date);
        out.writeString(location);
        out.writeString(address);
    }

    public static final Parcelable.Creator<ContactHelper> CREATOR
            = new Parcelable.Creator<ContactHelper>() {
        public ContactHelper createFromParcel(Parcel in) {
            return new ContactHelper(in);
        }

        public ContactHelper[] newArray(int size) {
            return new ContactHelper[size];
        }
    };
    
    private ContactHelper(Parcel in) {
        id = in.readString();
        name = in.readString();
        date = in.readString();
        location = in.readString();
        address = in.readString();
    }

}
