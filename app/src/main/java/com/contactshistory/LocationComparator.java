package com.contactshistory;

import java.util.Comparator;

public class LocationComparator implements Comparator<ContactHelper> {

	public int compare(ContactHelper c1, ContactHelper c2) 
    {
       return c1.location.compareToIgnoreCase(c2.location);
    }
}
