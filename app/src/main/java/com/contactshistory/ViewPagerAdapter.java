package com.contactshistory;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private String [] titles; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    private int numberOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created

    // Build a Constructor and assign the passed Values to appropriate values in the class
    ViewPagerAdapter(FragmentManager fm, String [] mTitles, int mNumberOfTabs) {
        super(fm);

        this.titles = mTitles;
        this.numberOfTabs = mNumberOfTabs;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());
        Boolean compact_ui = sharedPrefs.getBoolean("prefCompactUI", true);

        if (compact_ui){

            switch (position) {

                case 0:
                    return new AllFragment();
                case 1:
                    return new RecentFragment();
                case 2:
                    return new DateFragment();
                case 3:
                    return new DateTFragment();
                case 4:
                    return new LocationFragment();

            }

        }

        else{

            switch (position) {
                case 0:
                    return new AllFragment();
                case 1:
                    return new RecentFragment();
                case 2:
                    return new TodayFragment();
                case 3:
                    return new WeekFragment();
                case 4:
                    return new MonthFragment();
                case 5:
                    return new DateFragment();
                case 6:
                    return new DateTFragment();
                case 7:
                    return new LocationFragment();
            }
        }

        return null;
    }

    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public String getPageTitle(int position) {
        return titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}