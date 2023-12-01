package com.contactshistory;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootOkBroadcastReceiver extends BroadcastReceiver {
	 @SuppressLint("UnsafeProtectedBroadcastReceiver")
	 @Override
	    public void onReceive(Context context, Intent intent) {

		 Intent i = new Intent(context, MainActivity.class);

		 boolean startFromService = true;

		 i.putExtra("START_TYPE", startFromService);

		 context.startActivity(i);
	 }
}