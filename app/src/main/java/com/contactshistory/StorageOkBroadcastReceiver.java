package com.contactshistory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StorageOkBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, HistoryService.class);
        context.startService(startServiceIntent);

        Log.d("contactshistory", "Boot ok");

    }
}
