package com.reportme.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.reportme.R;
import com.reportme.preferences.SharedPref;
import com.reportme.url.Url;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by BHUSRI on 9/6/2017.
 */

public class Splash extends AppCompatActivity {
    private static final String TAG = "SPLASHSCREEN";
    private GPSTracker gps;

    @Override
    protected void onResume() {
        super.onResume();
        gps = new GPSTracker(Splash.this);

        // check if GPS enabled
        if (gps.canGetLocation()) {
            final double latitude = gps.getLatitude();
            final double longitude = gps.getLongitude();

            StringRequest request = new StringRequest(Request.Method.POST, Url.mydomain + Url.getReportedplaces, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "onResponse: " + response);
                    Intent intent = new Intent(Splash.this, MapsActivity.class);
                    intent.putExtra("data", response);
                    startActivity(intent);
                    finish();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Toast.makeText(Splash.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    SharedPref pref = new SharedPref(Splash.this);
                    Map<String, String> map = new HashMap<>();
                    map.put("lat", String.valueOf(latitude));
                    map.put("long", String.valueOf(longitude));
                    map.put("miles", pref.getMiles());
                    return map;
                }
            };
            Volley.newRequestQueue(Splash.this).add(request);
           /* double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            gps.stopUsingGPS();
            if (latitude == 0.0 || longitude == 0.0) {
                // Toast.makeText(getApplicationContext(), "Gps haven't set yet,Please  try after sometime", Toast.LENGTH_SHORT).show();
            } else {

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(Splash.this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    final String city = addresses.get(0).getLocality();
                    final String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                 }
                 */

        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Splash.this);

            // Setting Dialog Title
            alertDialog.setTitle("GPS is settings");

            // Setting Dialog Message
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
            alertDialog.setCancelable(false);
            // On pressing Settings button
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(Splash.this, "Location is required", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            // Showing Alert Message
            alertDialog.show();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

    }
}