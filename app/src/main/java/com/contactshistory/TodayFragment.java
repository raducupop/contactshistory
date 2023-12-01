package com.contactshistory;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TodayFragment extends Fragment {

	String date_display = null;

	GetListHelper lst = new GetListHelper();

	int SearchUsedFlag=0;

    View rootView = null;
    Context context = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

          rootView = inflater.inflate(R.layout.main_layout, container, false);
          context = MainActivity.getAppContext();

          setHasOptionsMenu(true);

          initializeSearch();

          ImageView image = rootView.findViewById(R.id.statusImage);

          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
          boolean dark = prefs.getBoolean("prefDarkUI", false);
          int img = R.drawable.status;
          if (dark) {
             img = R.drawable.status_dark;
          }
          image.setImageResource(img);

          image.setVisibility(View.GONE);

          SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
          SimpleDateFormat display_format = new SimpleDateFormat(sharedPrefs.getString("prefDateFormat","dd MMMM yyyy"));

          final ListView contacte = rootView.findViewById(R.id.listView1);

          Date date = Calendar.getInstance().getTime();
    	  SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
          final String data_azi = formatter.format(date);

          date_display = display_format.format(date);
  		  // Toast.makeText(getApplicationContext(), date_display, Toast.LENGTH_LONG).show();

        CustomList contacts_adapter = null;
        try {
            contacts_adapter = new CustomList(getActivity(), lst.getList(data_azi, data_azi, context));

            contacte.setAdapter(contacts_adapter);

            image.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CustomList contacts_adapter = new  CustomList(getActivity(), lst.getList(data_azi, data_azi, context));

                    contacte.setAdapter(contacts_adapter);
                    ImageView img = rootView.findViewById(R.id.statusImage);
                    if (contacts_adapter.getCount()==0)
                    {
                        img.setVisibility(View.VISIBLE);
                    }
                    else
                        img.setVisibility(View.GONE);
                }

            });

            final SwipeRefreshLayout swipeLayout = rootView.findViewById(R.id.swipe_container);
            swipeLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
                  @Override
                  public void onRefresh() {
                      swipeLayout.setRefreshing(true);
                      ListView cnct = rootView.findViewById(R.id.listView1);
                      CustomList contacts_adapter = new  CustomList(getActivity(), lst.getList(data_azi, data_azi, context));
                      cnct.setAdapter(contacts_adapter);
                      ImageView img = rootView.findViewById(R.id.statusImage);
                      if (contacts_adapter.getCount()==0)
                      {
                          img.setVisibility(View.VISIBLE);
                      }
                      else
                          img.setVisibility(View.GONE);
                      swipeLayout.setRefreshing(false);
                  }
              });

            swipeLayout.setColorScheme(android.R.color.holo_green_dark,
                    android.R.color.holo_red_dark,
                    android.R.color.holo_blue_dark,
                    android.R.color.holo_orange_dark);


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
                          TodayFragment.this.startActivity(intent);
                      }
                      else Toast.makeText(context.getApplicationContext(), getResources().getString(R.string.err_no_info), Toast.LENGTH_LONG).show();


                  } catch (Exception e) {
                      e.printStackTrace();
                      Toast.makeText(context.getApplicationContext(), getResources().getString(R.string.err_no_info), Toast.LENGTH_LONG).show();

                  }

              }
          });
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }


        if (contacts_adapter.getCount()==0)
          {
              image.setVisibility(View.VISIBLE);
          }
          else
              image.setVisibility(View.GONE);

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
            b.putInt("tab", 1);
            myIntent.putExtras(b);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            TodayFragment.this.startActivity(myIntent);
        } else if (itemId == R.id.pref) {
            Intent intent = new Intent(getActivity(), SettingsActivityWrapper.class);
            Bundle bundle = new Bundle();
            bundle.putInt("tab", 1);
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