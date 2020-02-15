package com.contactshistory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.AdapterView;

public class AllFragment extends Fragment {

    GetSimpleListHelper lst = new GetSimpleListHelper();

    View rootView = null;
    Context context = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.layout_simple, container, false);
        context = MainActivity.getAppContext();

        setHasOptionsMenu(true);

        CustomListSimple contacts_adapter;
        try {
            final ListView contacte = rootView.findViewById(R.id.listView_simple);

            contacts_adapter = new CustomListSimple(getActivity(), lst.getContactsSortedByID(context));
            contacte.setAdapter(contacts_adapter);

            final SwipeRefreshLayout swipeLayout = rootView.findViewById(R.id.swipe_container);
            swipeLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeLayout.setRefreshing(true);
                    ListView cnct = rootView.findViewById(R.id.listView_simple);
                    CustomListSimple contacts_adapter = new CustomListSimple(getActivity(), lst.getContactsSortedByID(context));
                    cnct.setAdapter(contacts_adapter);
                    ImageView img = rootView.findViewById(R.id.statusImage);
                    swipeLayout.setRefreshing(false);

                }

            });

            swipeLayout.setColorScheme(android.R.color.holo_green_dark,
                    android.R.color.holo_red_dark,
                    android.R.color.holo_blue_dark,
                    android.R.color.holo_orange_dark);

            contacte.setOnItemClickListener (new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> aView, View v, int position, long id) {

                    String rawid_from_list = lst.list.get(position).id;

                    //Toast.makeText(MainActivity.getAppContext(), "id in CH "+rawid_from_list, Toast.LENGTH_LONG).show();

                    Uri lookup = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, rawid_from_list);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(lookup);
                    AllFragment.this.startActivity(intent);

                  /*  Intent intent_open_contact = new Intent(Intent.ACTION_VIEW);
                    Uri uri_contact = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, rawid_from_list);
                    intent_open_contact.setData(uri_contact);
                    context.startActivity(intent_open_contact);
*/
                    /*if (contacts_cursor.getCount() > 0)
                    {
                        contacts_cursor.moveToFirst();
                        do {
                            String id_at_cursor = contacts_cursor.getString(contacts_cursor.getColumnIndex(ContactsContract.Data._ID));

                            if (id_at_cursor.contentEquals(rawid_from_list))
                            {
                                found_lookup = id_at_cursor;
                                contact_found = 1;

                                //Toast.makeText(MainActivity.getAppContext(), "id in CH "+rawid_from_list+" in device "+idraw+" lookup "+found_lookup, Toast.LENGTH_LONG).show();

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
                            AllFragment.this.startActivity(intent);
                        }
                        else Toast.makeText(context, getResources().getString(R.string.err_no_info), Toast.LENGTH_LONG).show();


                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, getResources().getString(R.string.err_no_info), Toast.LENGTH_LONG).show();

                    }
*/
//                    String rawid_from_list = lst.list.get(position).id;
//
//                    Log.i("CH","CLICK LIST ID: --> "+rawid_from_list);
//
//                    try {
//
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(rawid_from_list));
//                    intent.setData(uri);
//                    context.startActivity(intent);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Toast.makeText(context, getResources().getString(R.string.err_no_info), Toast.LENGTH_LONG).show();
//
//                    }
                }
            });


        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

        inflater.inflate(R.menu.menu_tab_all, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {


            case R.id.pref:
                Intent prefsIntent = new Intent(getActivity(), SettingsActivityWrapper.class);
                Bundle bundle = new Bundle();
                bundle.putInt("tab", 0);
                prefsIntent.putExtras(bundle);
                prefsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                AllFragment.this.startActivity(prefsIntent);

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

}