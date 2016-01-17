package com.contactshistory;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ImageView;


public class DateFragment extends Fragment {


    GetListHelper lst = new GetListHelper();

    static final ImageView image = null;

    int SearchUsedFlag=0;

    View rootView = null;
    Context context = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.date_layout, container, false);
        context = MainActivity.getAppContext();

        setHasOptionsMenu(true);

        initializeSearch();

        ImageView image = (ImageView) rootView.findViewById(R.id.statusImage);
        image.setImageResource(R.drawable.status);
        image.setVisibility(View.VISIBLE);

        final Button button = (Button) rootView.findViewById(R.id.pickdate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                showDatePicker();
            }
        });

        EditText search_string = (EditText) rootView.findViewById(R.id.search_string);
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

    private void showDatePicker() {

        DatePickerFragment date = new DatePickerFragment();
        /**
         * Set Up Current Date Into dialog
         */
        Calendar calender = Calendar.getInstance();
        Bundle args = new Bundle();
        args.putInt("year", calender.get(Calendar.YEAR));
        args.putInt("month", calender.get(Calendar.MONTH));
        args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
        date.setArguments(args);
        /**
         * Set Call back to capture selected date
         */
        date.setCallBack(ondate);
        date.show(getFragmentManager(), getResources().getString(R.string.title_pick_date));
    }

    DatePickerDialog.OnDateSetListener ondate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(context);

            SimpleDateFormat display_format = new SimpleDateFormat(sharedPrefs.getString("prefDateFormat","dd MMMM yyyy"));
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            final ListView contacte =(ListView) rootView.findViewById(R.id.listView1);

            String date_from_picker = String.valueOf(dayOfMonth)+"/"+String.valueOf(monthOfYear+1)+"/"+String.valueOf(year);

            Date date = null;
            try {
                date = formatter.parse(date_from_picker);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            String d1 = formatter.format(date);

            String date_display = display_format.format(date);

            Toast.makeText(context, date_display , Toast.LENGTH_LONG).show();

            CustomList contacts_adapter = new  CustomList(getActivity(), lst.getList(d1, d1, context));

            contacte.setAdapter(contacts_adapter);

            contacte.setOnItemClickListener (new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> aView, View v, int position, long id) {

                    int contact_found = 0;
                    String rawid_from_list = lst.list.get(position).id;
                    String found_lookup = "";
                    Cursor contcats_cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, null, null, null);
                    if (contcats_cursor.getCount() > 0)
                    {
                        contcats_cursor.moveToFirst();
                        do {
                            String idraw = contcats_cursor.getString(contcats_cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
                            String idlookup = contcats_cursor.getString(contcats_cursor.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));


                            if (idraw.contentEquals(rawid_from_list))
                            {
                                found_lookup = idlookup;
                                contact_found = 1;
                                //Toast.makeText(getApplicationContext(), "gasit. ", Toast.LENGTH_LONG).show();
                            }

                        } while (contcats_cursor.moveToNext());
                        //contcats_cursor.close();
                    }


                    try {
                        if (contact_found == 1)
                        {
                            Uri lookup = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, found_lookup);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(lookup);
                            DateFragment.this.startActivity(intent);
                        }
                        else Toast.makeText(context.getApplicationContext(), getResources().getString(R.string.err_no_info), Toast.LENGTH_LONG).show();


                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, getResources().getString(R.string.err_no_info), Toast.LENGTH_LONG).show();

                    }

                }
            });


            ImageView image = (ImageView) rootView.findViewById(R.id.statusImage);
            if (contacts_adapter.getCount()==0)
            {
                image.setVisibility(View.VISIBLE);
            }
            else
                image.setVisibility(View.GONE);


        }
    };


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

        inflater.inflate(R.menu.menu_main, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        ListView contacte = (ListView) rootView.findViewById(R.id.listView1);

        switch (item.getItemId()) {

	        case R.id.sort_name:
				CustomList n_s = new CustomList(getActivity(), lst.sortByName(lst.list));
		        contacte.setAdapter(n_s);
		        break;
		        
			case R.id.sort_date:
				
				CustomList d_s = new CustomList(getActivity(), lst.sortByDate(lst.list));
		        contacte.setAdapter(d_s);
		        break;
			
			case R.id.sort_location: 
			
				CustomList l_s = new CustomList(getActivity(), lst.sortByLocation(lst.list));
		        contacte.setAdapter(l_s);
		        break;
			    
			case R.id.view_location: 
				
				CustomList f_l = new CustomList(getActivity(), lst.filterWithLocation(lst.list));
		        contacte.setAdapter(f_l);
		        break;    
			    
			
			case R.id.view_all:    
		    
				CustomList f_a = new CustomList(getActivity(), lst.filterAll(lst.list));
		        contacte.setAdapter(f_a);
		        break;    

            case R.id.search:

                showSearch();
                break;

            case R.id.delete:

                Intent myIntent = new
                        Intent(getActivity(), RemoveActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("lista", lst.list);
                b.putInt("tab", 4);
                myIntent.putExtras(b);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                DateFragment.this.startActivity(myIntent);
                break;

            case R.id.pref:
                Intent intent = new Intent(getActivity(), SettingsActivityWrapper.class);
                Bundle bundle = new Bundle();
                bundle.putInt("tab", 4);
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;

            case R.id.help_about:
                Intent intent_about = new Intent(getActivity(), AboutActivity.class);
                intent_about.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent_about);
                break;

            case R.id.help_tutorial:
                Intent intent_tutorial = new Intent(getActivity(), TutorialActivity.class);
                intent_tutorial.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent_tutorial);
                break;


            default:
                return super.onOptionsItemSelected(item);

        }
        return false;
    }


    public void initializeSearch(){
        this.hideSearch();

        Button search_close_button = (Button) rootView.findViewById(R.id.search_close_button);
        search_close_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideSearch();
            }
        });
    }


    public void showSearch(){

        LinearLayout search_layout = (LinearLayout) rootView.findViewById(R.id.search_layout);
        search_layout.setVisibility(View.VISIBLE);
        SearchUsedFlag = 1;
    }

    public void hideSearch(){

        LinearLayout search_layout = (LinearLayout) rootView.findViewById(R.id.search_layout);
        search_layout.setVisibility(View.GONE);

        if (SearchUsedFlag!=0)
        {
            ListView contacte = (ListView) rootView.findViewById(R.id.listView1);
            CustomList reset = new CustomList(getActivity(), lst.resetSearch(lst.list));
		    contacte.setAdapter(reset);
        }
        SearchUsedFlag = 0;
    }

    public void performSearch(){


        EditText search_string = (EditText) rootView.findViewById(R.id.search_string);
        ListView contacte = (ListView) rootView.findViewById(R.id.listView1);
        String query = search_string.getText().toString().toLowerCase();
    	CustomList results_adapter = new CustomList(getActivity(), lst.searchList(lst.list, query));
        contacte.setAdapter(results_adapter);
        ImageView image = (ImageView) rootView.findViewById(R.id.statusImage);
        if (results_adapter.getCount()==0)
        {
            image.setVisibility(View.VISIBLE);
        }
        else
            image.setVisibility(View.GONE);

    }

}