package com.ahmadelbaz.remember;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

public class AlarmReciever extends BroadcastReceiver {

    SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {

        prefs = context.getSharedPreferences(Activity.class.getSimpleName(), Context.MODE_PRIVATE);
        int notificationNumber = prefs.getInt("notificationNumber", 0);


        String mTitle = intent.getStringExtra("TITLE_MESSAGE");
        String mContent = intent.getStringExtra("MESSAGE");

        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.mBuilder;
        notificationHelper.getPackageManager();

        notificationHelper.mBuilder.setContentTitle(mTitle);
        notificationHelper.mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(mContent));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationNumber, nb.build());
        SharedPreferences.Editor editor = prefs.edit();
        notificationNumber++;
        editor.putInt("notificationNumber", notificationNumber);
        editor.commit();
   }
}