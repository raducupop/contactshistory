package com.contactshistory;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactHelperSimple implements Parcelable {

    String id;
    String name;

    public ContactHelperSimple(){
        id = null;
        name = null;

    }

    public ContactHelperSimple(String i, String n){
        id = i;
        name = n;
    }

    String toText(){

        String output;
        output = id+"\n"+name;
        return output;
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
    }

    public static final Parcelable.Creator<ContactHelperSimple> CREATOR
            = new Parcelable.Creator<ContactHelperSimple>() {
        public ContactHelperSimple createFromParcel(Parcel in) {
            return new ContactHelperSimple(in);
        }

        public ContactHelperSimple[] newArray(int size) {
            return new ContactHelperSimple[size];
        }
    };

    private ContactHelperSimple(Parcel in) {
        id = in.readString();
        name = in.readString();
    }

}