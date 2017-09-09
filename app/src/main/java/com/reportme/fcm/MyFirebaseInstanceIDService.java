package com.reportme.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.reportme.preferences.SharedPref;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by shivambhusri on 5/1/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private String TAG = "InstanceIDService";
    private String SENDER_ID = "453694894087";
    private AtomicInteger msgId;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        SharedPref pref = new SharedPref(this);
        pref.setfcmToken(token);

    }
}
