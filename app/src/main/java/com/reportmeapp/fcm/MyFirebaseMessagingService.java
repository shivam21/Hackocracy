package com.reportmeapp.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.reportmeapp.R;
import com.reportmeapp.preferences.SharedPref;
import com.reportmeapp.ui.MapsActivity;
import com.reportmeapp.ui.ReportDetails;

import java.util.Map;
import java.util.Random;

/**
 * Created by shivambhusri on 5/1/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "1";
    String TAG = "MESSAGERECEIVED";
    private GoogleApiClient mGoogleApiClient;
    private String myid;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "onMessageReceived: " + remoteMessage.getData().toString());
        Map<String, String> map = remoteMessage.getData();
        if (map != null) {
            String category = map.get("category");
            String userid = map.get("userid");
            SharedPref pref = new SharedPref(this);
            String myuserid = pref.getUserid();
            if (!userid.equals(myuserid)) {
                String reportid = map.get("reportid");
                switch (category) {
                    case "Women Harassment":
                        if (pref.iswomen())
                            sendnotification(category, reportid);
                        break;
                    case "Theft":
                        if (pref.istheft())
                            sendnotification(category, reportid);
                        break;
                    case "Other":
                        if (pref.isother())
                            sendnotification(category, reportid);
                        break;
                    case "Child Labour":
                        if (pref.ischild())
                            sendnotification(category, reportid);
                        break;
                    case "Corruption":
                        if (pref.iscorrupt())
                            sendnotification(category, reportid);
                        break;

                }

            }

        }

    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        SharedPref pref = new SharedPref(this);
        pref.setfcmToken(s);
    }

    private void sendnotification(String category, String reportid) {
        NotificationCompat.Builder mBuilder4 =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(category + " alert")
                        .setPriority(Notification.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .setContentText("Click to know more");
        Intent backIntent = new Intent(this, MapsActivity.class);
        backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent resultIntent4 = new Intent(this, ReportDetails.class);
        resultIntent4.putExtra("reportid", reportid);
        resultIntent4.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        TaskStackBuilder stackBuilder4 = TaskStackBuilder.create(this);
        stackBuilder4.addParentStack(ReportDetails.class);
        stackBuilder4.addNextIntent(resultIntent4);
        PendingIntent resultPendingIntent4 =
                PendingIntent.getActivities(this, 102,
                        new Intent[]{backIntent, resultIntent4}, PendingIntent.FLAG_ONE_SHOT);
        mBuilder4.setContentIntent(resultPendingIntent4);
        SharedPref pref = new SharedPref(this);
        if (pref.getSoundStatus())
            mBuilder4.setDefaults(Notification.DEFAULT_SOUND);
        NotificationManager mNotificationManager4 =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "Reportme", importance);
            mNotificationManager4.createNotificationChannel(mChannel);
        }
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        mNotificationManager4.notify(m, mBuilder4.build());
    }
}
