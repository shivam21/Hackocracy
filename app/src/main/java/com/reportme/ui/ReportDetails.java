package com.reportme.ui;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.reportme.R;
import com.reportme.url.Url;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by BHUSRI on 9/8/2017.
 */

public class ReportDetails extends AppCompatActivity implements OnMapReadyCallback {
    private String reportid;
    private TextView reportlocation;
    private ViewGroup mSelectedImagesContainer;
    private TextView categorytext, des, loc;
    private RelativeTimeTextView timeTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.reportme.R.layout.reportdetails);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Bundle b = getIntent().getExtras();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        reportid = b.getString("reportid");
        categorytext = (TextView) findViewById(R.id.category);
        des = (TextView) findViewById(R.id.description);
        loc = (TextView) findViewById(R.id.loc);
        progressBar = (ProgressBar) findViewById(R.id.pogressbar);
        timeTextView = (RelativeTimeTextView) findViewById(R.id.time);
        mSelectedImagesContainer = (ViewGroup) findViewById(R.id.selected_photos_container);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        setNetworkRequest(googleMap);

    }

    private void setNetworkRequest(final GoogleMap mMap) {
        StringRequest request = new StringRequest(Request.Method.POST, Url.mydomain + Url.getreportdetails, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    progressBar.setVisibility(View.GONE);
                    JSONArray array = new JSONArray(response);
                    JSONObject reportdata = array.getJSONObject(0);
                    JSONArray reportpics = array.getJSONArray(1);
                    String category = reportdata.getString("category");
                    String desc = reportdata.getString("summary");
                    String time = reportdata.getString("time");
                    double latitude = Double.parseDouble(reportdata.getString("latitude"));
                    double longitude = Double.parseDouble(reportdata.getString("longitude"));
                    categorytext.setText(category);
                    if (time.length() > 0)
                        timeTextView.setReferenceTime(Long.parseLong(time));
                    des.setText(desc);
                    LatLng latlon = new LatLng(latitude, longitude);
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(ReportDetails.this, Locale.getDefault());

                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        final String city = addresses.get(0).getLocality();
                        final String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();
                        String postalCode = addresses.get(0).getPostalCode();
                        String knownName = addresses.get(0).getFeatureName();
                        String text = "";
                        if (knownName != null)
                            text = text + knownName + "\n";
                        text = text + city + "," + state;
                        if (postalCode != null)
                            text = text + "\nPincode:" + postalCode;
                        loc.setText(text);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMap.addMarker(new MarkerOptions().position(latlon).title(category));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, 15));
                    mSelectedImagesContainer.removeAllViews();
                    if (reportpics.length() >= 1) {
                        mSelectedImagesContainer.setVisibility(View.VISIBLE);
                    }

                    int wdpx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                    int htpx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
                    for (int i = 0; i < reportpics.length(); i++) {
                        JSONObject obj = reportpics.getJSONObject(i);
                        String picid = obj.getString("picname");
                        View imageHolder = LayoutInflater.from(ReportDetails.this).inflate(R.layout.photo_item, null);
                        ImageView thumbnail = (ImageView) imageHolder.findViewById(R.id.media_image);
                        Glide.with(ReportDetails.this)
                                .load(String.format("%s%s%s.jpg", Url.mydomain, Url.imagepath, picid))
                                .into(thumbnail);

                        mSelectedImagesContainer.addView(imageHolder);

                        thumbnail.setLayoutParams(new FrameLayout.LayoutParams(wdpx, htpx));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ReportDetails.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ReportDetails.this, "failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("reportid", reportid);
                return map;
            }
        };
        progressBar.setVisibility(View.VISIBLE);
        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
