package com.contactshistory;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;


public class RemoveActivity extends AppCompatActivity {

    Toolbar toolbar;

    ListView myList;
    Button doDelete;
    Button goBack;

    GetListHelper lst = new GetListHelper();
    CustomListMultiple del_list_adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dark_theme = sharedPrefs.getBoolean("prefDarkUI", false);
        if (dark_theme) {
            setTheme(R.style.AppThemeDark);
        }
        else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.remove_layout);

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle b = getIntent().getExtras();

        myList = findViewById(R.id.list_remove);
	    CheckBox selectAll = findViewById(R.id.selectall_checkbox);
        selectAll.setChecked(false);

        doDelete = findViewById(R.id.remove_del_button);
        goBack = findViewById(R.id.remove_cancel_button);

		ArrayList<ContactHelper> lista =  (ArrayList<ContactHelper>) b.getSerializable("lista");

        lst = new GetListHelper(lista);
        del_list_adapter = new CustomListMultiple(RemoveActivity.this, lst.resetSearch(lst.list));
    	myList.setAdapter(del_list_adapter);

        doDelete.setOnClickListener(new Button.OnClickListener(){
 
            @Override
 
            public void onClick(View v) {

                String key_selected;
                String date_selected;

                Context context = RemoveActivity.this.getApplicationContext(); 
                int flag=0;
              
                final DBAdapter database = new DBAdapter(context);
                database.open();
                
                for(int i = 0; i < del_list_adapter.getCount(); i++)
                {

                		if(del_list_adapter.checkBoxState[i])
                		{
                			key_selected = "["+lst.list.get(i).id+"]";
                            date_selected = lst.list.get(i).date;
                            try {
                                  database.deleteContactByKeyAndDate(key_selected, date_selected);
                            } catch (Exception e) {
                                  e.printStackTrace();
                            }
                            if (flag!=1) flag=1;
                		}
                }

                database.close();
                if (flag==1)
                {
                	Toast.makeText(RemoveActivity.this, getResources().getString(R.string.rem_info), Toast.LENGTH_LONG).show();
                }


                Bundle bundle = getIntent().getExtras();
                SharedPreferences dtab = context.getSharedPreferences("tabPrefes",0);
                SharedPreferences.Editor edit = dtab.edit();
                edit.putInt("default_tab", bundle.getInt("tab", 0));
                edit.apply();

                Intent myIntent = new Intent(RemoveActivity.this, MainActivity.class );
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 

                startActivity(myIntent);

                finish();
    			 
            }});

       goBack.setOnClickListener(new Button.OnClickListener(){
    	 
        @Override

        public void onClick(View v) {

        	finish();

        }});

}

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();


        if (checked){

            del_list_adapter.checkAllFlag = true;

            del_list_adapter.notifyDataSetChanged();

        }

        else {

            del_list_adapter.uncheckAllFlag = true;

            del_list_adapter.notifyDataSetChanged();

        }

    }

 
}