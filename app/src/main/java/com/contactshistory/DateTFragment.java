package com.contactshistory;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTFragment extends Fragment {


    String date_from_picker1 = " ";
    Date date1 = null;

    String date_from_picker2 = " ";
    Date date2 = null;

    String date_display1 = null;
    String date_display2 = null;

    GetListHelper lst = new GetListHelper();

    View rootView = null;
    Context context = null;

    int SearchUsedFlag=0;

    static final ImageView image = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.datet_layout, container, false);
        context = MainActivity.getAppContext();

        setHasOptionsMenu(true);

        initializeSearch();

        final ImageView image = rootView.findViewById(R.id.statusImage);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dark = prefs.getBoolean("prefDarkUI", false);
        int img = R.drawable.status;
        if (dark) {
            img = R.drawable.status_dark;
        }
        image.setImageResource(img);

        image.setVisibility(View.VISIBLE);

        final TextView txt1 = rootView.findViewById(R.id.text1);
        txt1.setText("");
        final TextView txt2 = rootView.findViewById(R.id.text2);
        txt2.setText("");

        final Button pick1 = rootView.findViewById(R.id.pick1);
        pick1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                showStartDatePicker();
            }
        });


        final Button pick2 = rootView.findViewById(R.id.pick2);
        pick2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                showEndDatePicker();
            }
        });

        final Button show = rootView.findViewById(R.id.show);
        show.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                ListView contacte = rootView.findViewById(R.id.listView1);

                if (!(date_from_picker1.contentEquals(" ")) && !(date_from_picker2.contentEquals(" ")))
                {
                    if ( date1.after(date2) )
                    {
                        String temp=date_from_picker2;
                        date_from_picker2=date_from_picker1;
                        date_from_picker1=temp;

                        temp=date_display2;
                        date_display2=date_display1;
                        date_display1=temp;

                        Date temp_date;

                        temp_date = date2;
                        date2=date1;
                        date1=temp_date;


                        // Toast.makeText(getApplicationContext()," aici" , Toast.LENGTH_SHORT).show();

                        txt1.setText(date_display1);
                        txt2.setText(date_display2);


                    }


                    Toast.makeText(context, date_display1+" - "+date_display2 , Toast.LENGTH_SHORT).show();

                    CustomList contacts_adapter = null;
                    try {
                        contacts_adapter = new CustomList(getActivity(), lst.getList(date_from_picker1, date_from_picker2, context));

                        contacte.setAdapter(contacts_adapter);

                        contacte.setOnItemClickListener (new AdapterView.OnItemClickListener() {
                            public void onItemClick(AdapterView<?> aView, View v, int position, long id) {

                                int contact_found = 0;
                                String rawid_from_list = lst.list.get(position).id;
                                String found_lookup = "";
                                Cursor contacts_cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, null, null, null);
                                if (contacts_cursor.getCount() > 0)
                                {
                                    contacts_cursor.moveToFirst();
                                    do {
                                        @SuppressLint("Range") String idraw = contacts_cursor.getString(contacts_cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
                                        @SuppressLint("Range") String idlookup = contacts_cursor.getString(contacts_cursor.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));


                                        if (idraw.contentEquals(rawid_from_list))
                                        {
                                            found_lookup = idlookup;
                                            contact_found = 1;
                                            //Toast.makeText(getApplicationContext(), "gasit. ", Toast.LENGTH_LONG).show();
                                        }

                                    } while (contacts_cursor.moveToNext());
                                    contacts_cursor.close();
                                }


                                try {
                                    if (contact_found == 1)
                                    {
                                        Uri lookup = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, found_lookup);
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(lookup);
                                        DateTFragment.this.startActivity(intent);
                                    }
                                    else Toast.makeText(context, getResources().getString(R.string.err_no_info), Toast.LENGTH_LONG).show();


                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, getResources().getString(R.string.err_no_info), Toast.LENGTH_LONG).show();

                                }

                            }
                        });
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    }


                    ImageView image = rootView.findViewById(R.id.statusImage);
                    if (contacts_adapter.getCount()==0)
                    {
                        image.setVisibility(View.VISIBLE);
                    }
                    else
                        image.setVisibility(View.GONE);


                }

                else
                {
                    Toast.makeText(getActivity(), getResources().getString(R.string.str_info_getbothdates) , Toast.LENGTH_SHORT).show();

                }


            }
        });


        EditText search_string = rootView.findViewById(R.id.search_string);
        search_string.addTextChangedListener(new TextWatcher(){

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                performSearch();

            }

        });

        return rootView;

    }

    private void showStartDatePicker() {
        DatePickerFragment date = new DatePickerFragment();
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        date.setCallBack(onDateStartSet);
        date.show(getFragmentManager(), getResources().getString(R.string.title_pick_date));
    }


    private void showEndDatePicker() {
        DatePickerFragment date = new DatePickerFragment();
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        date.setCallBack(onDateEndSet);
        date.show(getFragmentManager(), getResources().getString(R.string.title_pick_date));
    }


        DatePickerDialog.OnDateSetListener onDateStartSet = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                SharedPreferences sharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(context);
                SimpleDateFormat display_format = new SimpleDateFormat(sharedPrefs.getString("prefDateFormat","dd MMMM yyyy"));

                final TextView txt1 = rootView.findViewById(R.id.text1);
                date_from_picker1 = String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year); // luna incepe de la 0, deci treuie adunat 1;
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                date1 = null;
                try {
                    date1 = formatter.parse(date_from_picker1);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                date_from_picker1 = formatter.format(date1);
                date_display1 = display_format.format(date1);
                txt1.setText(date_display1);
            }
  };


    DatePickerDialog.OnDateSetListener onDateEndSet = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            SimpleDateFormat display_format = new SimpleDateFormat(sharedPrefs.getString("prefDateFormat","dd MMMM yyyy"));

            final TextView txt2 = rootView.findViewById(R.id.text2);

            date_from_picker2 = String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year); // luna incepe de la 0, deci treuie adunat 1;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            date2 = null;
            try {
                date2 = formatter.parse(date_from_picker2);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            date_from_picker2 = formatter.format(date2);
            date_display2 = display_format.format(date2);
            txt2.setText(date_display2);
        }
    };


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

        inflater.inflate(R.menu.menu_main, menu);
    }



    public boolean onOptionsItemSelected(MenuItem item) {

        ListView contacte = rootView.findViewById(R.id.listView1);

        int itemId = item.getItemId();
        if (itemId == R.id.sort_name) {
            CustomList n_s = new CustomList(getActivity(), lst.sortByName(lst.list));
            contacte.setAdapter(n_s);
        } else if (itemId == R.id.sort_date) {
            CustomList d_s = new CustomList(getActivity(), lst.sortByDate(lst.list));
            contacte.setAdapter(d_s);
        } else if (itemId == R.id.sort_location) {
            CustomList l_s = new CustomList(getActivity(), lst.sortByLocation(lst.list));
            contacte.setAdapter(l_s);
        } else if (itemId == R.id.view_location) {
            CustomList f_l = new CustomList(getActivity(), lst.filterWithLocation(lst.list));
            contacte.setAdapter(f_l);
        } else if (itemId == R.id.view_all) {
            CustomList f_a = new CustomList(getActivity(), lst.filterAll(lst.list));
            contacte.setAdapter(f_a);
        } else if (itemId == R.id.search) {
            showSearch();
        } else if (itemId == R.id.delete) {
            Intent myIntent = new
                    Intent(getActivity(), RemoveActivity.class);
            Bundle b = new Bundle();
            b.putSerializable("lista", lst.list);
            b.putInt("tab", 5);
            myIntent.putExtras(b);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            DateTFragment.this.startActivity(myIntent);
        } else if (itemId == R.id.pref) {
            Intent intent = new Intent(getActivity(), SettingsActivityWrapper.class);
            Bundle bundle = new Bundle();
            bundle.putInt("tab", 5);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (itemId == R.id.help_about) {
            Intent intent_about = new Intent(getActivity(), AboutActivity.class);
            intent_about.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent_about);
        } else if (itemId == R.id.help_tutorial) {
            Intent intent_tutorial = new Intent(getActivity(), TutorialActivity.class);
            intent_tutorial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent_tutorial);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return false;
    }


    public void initializeSearch(){
        this.hideSearch();

        Button search_close_button = rootView.findViewById(R.id.search_close_button);
        search_close_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideSearch();
            }
        });
    }


    public void showSearch(){

        LinearLayout search_layout = rootView.findViewById(R.id.search_layout);
        search_layout.setVisibility(View.VISIBLE);
        SearchUsedFlag = 1;
    }

    public void hideSearch(){

        LinearLayout search_layout = rootView.findViewById(R.id.search_layout);
        search_layout.setVisibility(View.GONE);

        if (SearchUsedFlag!=0)
        {
            ListView contacte = rootView.findViewById(R.id.listView1);
		    CustomList reset = new CustomList(getActivity(), lst.resetSearch(lst.list));
		    contacte.setAdapter(reset);
        }
        SearchUsedFlag = 0;
    }

    public void performSearch(){

        EditText search_string = rootView.findViewById(R.id.search_string);
        ListView contacte = rootView.findViewById(R.id.listView1);
        String query = search_string.getText().toString().toLowerCase();
        CustomList results_adapter = new CustomList(getActivity(), lst.searchList(lst.list, query));
        contacte.setAdapter(results_adapter);
        ImageView image = rootView.findViewById(R.id.statusImage);
        if (results_adapter.getCount()==0)
        {
            image.setVisibility(View.VISIBLE);
        }
        else
            image.setVisibility(View.GONE);


    }


}