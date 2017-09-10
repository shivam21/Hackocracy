package com.reportme.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.reportme.R;
import com.reportme.preferences.SharedPref;
import com.reportme.url.Url;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int INTENT_REQUEST_GET_IMAGES = 100;
    private static final String TAG = "MAPS";
    private GoogleMap mMap;
    private FloatingActionButton fab;
    private JSONArray array;
    private Toolbar toolbar;
    private TextView textView;
    private GoogleApiClient mGoogleApiClient;
    private double latitude, longitude;

    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = (TextView) findViewById(R.id.textview);
        textView.setText(Html.fromHtml("Report" + "<font color='#00BFFF'>me</font>"));
        Bundle b = getIntent().getExtras();
        if (b != null) {
            String data = b.getString("data");
            try {
                array = new JSONArray(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPref pref = new SharedPref(MapsActivity.this);
                String userid = pref.getUserid();
                if (userid.equals("0")) {
                    startActivity(new Intent(MapsActivity.this, Signunppage.class));
                    finish();
                } else {
                    startActivity(new Intent(MapsActivity.this, Report.class));
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.d(TAG, "onInfoWindowClick: " + marker.getPosition());
                Intent intent = new Intent(MapsActivity.this, ReportDetails.class);
                intent.putExtra("reportid", String.valueOf(marker.getTag()));
                startActivity(intent);
            }
        });
        if (array != null)
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject obj = array.getJSONObject(i);
                    double lat = Double.parseDouble(obj.getString("latitude"));
                    double longi = Double.parseDouble(obj.getString("longitude"));
                    String category = obj.getString("category");
                    String reportid = obj.getString("reportid");
                    LatLng latlon = new LatLng(lat, longi);
                    Marker marker;
                    switch (category) {
                        case "Other":
                            marker = mMap.addMarker(new MarkerOptions().position(latlon).title(category).icon(BitmapDescriptorFactory.fromResource(R.drawable.recycle)));
                            marker.setSnippet("Click to get details of the crime");
                            marker.setTag(reportid);
                            break;
                        case "Child Labour":
                            marker = mMap.addMarker(new MarkerOptions().position(latlon).title(category).icon(BitmapDescriptorFactory.fromResource(R.drawable.child)));
                            marker.setSnippet("Click to get details of the crime");
                            marker.setTag(reportid);
                            break;
                        case "Women Harassment":
                            marker = mMap.addMarker(new MarkerOptions().position(latlon).title(category).icon(BitmapDescriptorFactory.fromResource(R.drawable.women)));
                            marker.setSnippet("Click to get details of the crime");
                            marker.setTag(reportid);
                            break;
                        case "Theft":
                            marker = mMap.addMarker(new MarkerOptions().position(latlon).title(category).icon(BitmapDescriptorFactory.fromResource(R.drawable.thief)));
                            marker.setSnippet("Click to get details of the crime");
                            marker.setTag(reportid);
                            break;
                        case "Corruption":
                            marker = mMap.addMarker(new MarkerOptions().position(latlon).title(category).icon(BitmapDescriptorFactory.fromResource(R.drawable.bribe)));
                            marker.setSnippet("Click to get details of the crime");
                            marker.setTag(reportid);
                            break;
                    }

                    //mMap.addMarker(new MarkerOptions().position(latlon).title(category));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        GPSTracker gps = new GPSTracker(MapsActivity.this);
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            updatemypositions(latitude, longitude);
            gps.stopUsingGPS();
            if (latitude == 0.0 || longitude == 0.0) {
                // Toast.makeText(getApplicationContext(), "Gps haven't set yet,Please  try after sometime", Toast.LENGTH_SHORT).show();
            } else {
                LatLng latlon = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(latlon).title("MyHome").icon(BitmapDescriptorFactory.fromResource(R.drawable.home)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, 15));
            }
        }


    }

    private void updatemypositions(final double latitude, final double longitude) {
        StringRequest request = new StringRequest(Request.Method.POST, Url.mydomain + Url.updatemypositions, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                SharedPref pref = new SharedPref(MapsActivity.this);
                map.put("lat", String.valueOf(latitude));
                map.put("long", String.valueOf(longitude));
                map.put("userid", pref.getUserid());
                return map;
            }
        };
        Volley.newRequestQueue(MapsActivity.this).add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(MapsActivity.this, MySettings.class));
                break;
            case R.id.statics:
                Intent intent = new Intent(MapsActivity.this, Statistics.class);
                intent.putExtra("lat", latitude);
                intent.putExtra("long", longitude);
                startActivity(intent);
                break;
            case R.id.about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("About App").setMessage(getString(R.string.aboutapp));
                builder.show();
                break;
            case R.id.logout:
                SharedPref pref = new SharedPref(this);
                pref.setUserid("0");
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                // Log.d(TAG, "onResult: logout");
                            }
                        });
                LoginManager.getInstance().logOut();
                Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


}
