package com.reportmeapp.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.reportmeapp.R;

/**
 * Created by BHUSRI on 9/6/2017.
 */

public class SharedPref {
    private Context context;
    private String userid;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public void setUserid(String userid) {
        editor.putString(context.getString(R.string.userid), userid);
        editor.apply();
    }

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

    }

    public String getUserid() {
        return sharedPreferences.getString(context.getString(R.string.userid), "0");
    }

    public String getMiles() {
        return sharedPreferences.getString(context.getString(R.string.miles), "25");
    }

    public void setfcmToken(String fcmToken) {
        editor.putString(context.getString(R.string.fcmtoken), userid);
        editor.apply();
    }

    public String getfcmtoken() {
        return sharedPreferences.getString(context.getString(R.string.fcmtoken), FirebaseInstanceId.getInstance().getToken());
    }

    public boolean getSoundStatus() {
        return sharedPreferences.getBoolean(context.getString(R.string.soundstatus), true);
    }

    public boolean iswomen() {
        return sharedPreferences.getBoolean(context.getString(R.string.iswomen), true);
    }

    public boolean istheft() {
        return sharedPreferences.getBoolean(context.getString(R.string.theft), true);

    }

    public boolean ischild() {
        return sharedPreferences.getBoolean(context.getString(R.string.child), true);

    }

    public boolean iscorrupt() {
        return sharedPreferences.getBoolean(context.getString(R.string.corrupt), true);

    }

    public boolean isother() {
        return sharedPreferences.getBoolean(context.getString(R.string.other), true);

    }

    public void setiswomen(boolean iswomen) {
        editor.putBoolean(context.getString(R.string.iswomen), iswomen);
        editor.apply();
    }

    public void setistheft(boolean istheft) {
        editor.putBoolean(context.getString(R.string.theft), istheft);
        editor.apply();
    }

    public void setischild(boolean ischild) {
        editor.putBoolean(context.getString(R.string.child), ischild);
        editor.apply();
    }

    public void setiscorrupt(boolean iscorrupt) {
        editor.putBoolean(context.getString(R.string.corrupt), iscorrupt);
        editor.apply();
    }

    public void setisother(boolean isother) {
        editor.putBoolean(context.getString(R.string.other), isother);
        editor.apply();
    }

    public void setSoundStatus(boolean soundStatus) {
        editor.putBoolean(context.getString(R.string.soundstatus), soundStatus);
        editor.apply();
    }

    public void setMiles(int miles) {
        editor.putString(context.getString(R.string.miles), String.valueOf(miles));
        editor.apply();
    }
}
