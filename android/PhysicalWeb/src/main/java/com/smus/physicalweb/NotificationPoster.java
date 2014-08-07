package com.smus.physicalweb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import 	android.support.v4.app.NotificationCompat;
import android.text.Html;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by abellajdel on 7/27/14.
 */
public class NotificationPoster {

    private Context context;
    private NotificationManager notificationManager;
    Notification.Builder myNotificationBuilder;
    PendingIntent pIntent;

    NotificationPoster(Context _context){
        context = _context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        myNotificationBuilder = new Notification.Builder(context);

        Intent new_intent = new Intent(context, MainActivity.class);
        new_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pIntent = PendingIntent.getActivity(context, 0, new_intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
    }

    public void updateNotification(Map<String, NearbyDevice> curentDevices){
        String beaconsUrls = "";
        int i = 0;
        int maxUrls = 5;
        int numberOfdevices = curentDevices.size();

        if(numberOfdevices < maxUrls)
            maxUrls = curentDevices.size();

        Iterator entries = curentDevices.entrySet().iterator();
        while (entries.hasNext() && i < maxUrls) {
            Map.Entry thisDevice = (Map.Entry) entries.next();
            Object key = thisDevice.getKey();
            //NearbyDevice device = (NearbyDevice) thisDevice.getValue();
            beaconsUrls += key.toString()+"\n";
            i++;
        }

        //Taking care of notifications here


        Notification myNotification = myNotificationBuilder
                //.setTicker("Notification " + curentDevicesNumber + " !")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                //.setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launcher)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setStyle(new Notification.BigTextStyle(myNotificationBuilder)
                        .bigText(beaconsUrls)
                        .setBigContentTitle("Beacons found")
                        .setSummaryText("Tap to see beacons"))
                .setNumber(numberOfdevices)
                .build();


        notificationManager.notify(1, myNotification);
    }

    public void cancelNotification(){
        notificationManager.cancel(1);
    }
}
