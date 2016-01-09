package com.contactshistory;

import java.util.Comparator;

public class NameComparator implements Comparator<ContactHelper>{

	public int compare(ContactHelper c1, ContactHelper c2)
    {
       return c1.name.compareToIgnoreCase(c2.name);
   }

}
