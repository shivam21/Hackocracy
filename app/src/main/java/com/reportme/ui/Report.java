package com.reportme.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.gun0912.tedpicker.Config;
import com.gun0912.tedpicker.ImagePickerActivity;
import com.reportme.R;
import com.reportme.preferences.SharedPref;
import com.reportme.url.Url;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Created by BHUSRI on 9/9/2017.
 */

public class Report extends AppCompatActivity implements UploadStatusDelegate {
    private static final String TAG = "REPORT";
    private static final int INTENT_REQUEST_GET_IMAGES = 100;
    private RelativeLayout baseLayout;
    private ViewGroup mSelectedImagesContainer;
    private static ArrayList<Uri> image_uris = new ArrayList<Uri>();
    private static HashMap<String, String> mymap = new HashMap<>();
    private static String name;
    private static JSONObject picnames = new JSONObject();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        baseLayout = (RelativeLayout) findViewById(R.id.base_popup_layout);
        final TextView addphotos = (TextView) findViewById(R.id.addphotos);
        final Spinner catspinner = (Spinner) findViewById(R.id.catspinner);
        final EditText description = (EditText) findViewById(R.id.description);
        final Button post = (Button) findViewById(R.id.post);
        final TextView location = (TextView) findViewById(R.id.location);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GPSTracker gps = new GPSTracker(Report.this);
                if (gps.canGetLocation()) {
                    final double latitude = gps.getLatitude();
                    final double longitude = gps.getLongitude();
                    gps.stopUsingGPS();
                    if (latitude == 0.0 || longitude == 0.0) {
                        // Toast.makeText(getApplicationContext(), "Gps haven't set yet,Please  try after sometime", Toast.LENGTH_SHORT).show();
                    } else {

                        Geocoder geocoder;
                        List<Address> addresses;
                        geocoder = new Geocoder(Report.this, Locale.getDefault());

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

                            location.setText(text);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    post.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String cat = (String) catspinner.getSelectedItem();
                            String desc = description.getText().toString().trim();
                            if (desc.length() < 10) {
                                // description.setError("Please provide more info.");
                                Toast.makeText(Report.this, "Please add more desccription", Toast.LENGTH_SHORT).show();
                            } else {
                                mymap.clear();
                                SharedPref pref = new SharedPref(Report.this);
                                mymap.put("category", cat);
                                mymap.put("description", desc);
                                mymap.put("lat", String.valueOf(latitude));
                                mymap.put("long", String.valueOf(longitude));
                                mymap.put("userid", pref.getUserid());
                                mymap.put("imagenames", picnames.toString());
                                mymap.put("time", String.valueOf(Calendar.getInstance().getTimeInMillis()));
                                if (image_uris.size() > 0) {
                                    uploadPhotos();
                                } else {
                                    mymap.put("miles", pref.getMiles());
                                    final Context context = Report.this;
                                    StringRequest request = new StringRequest(Request.Method.POST, Url.mydomain + Url.report, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            Log.d(TAG, "onResponse: " + response);
                                            if (context != null)
                                                Toast.makeText(context, "Thanks for reporting us", Toast.LENGTH_SHORT).show();
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            error.printStackTrace();
                                        }
                                    }) {
                                        @Override
                                        protected Map<String, String> getParams() throws AuthFailureError {
                                            Log.d(TAG, "getParams: " + mymap);
                                            return mymap;
                                        }
                                    };
                                    Volley.newRequestQueue(context).add(request);
                                    Toast.makeText(context, "Uploading Started", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }
                    });
                }
                mSelectedImagesContainer = (ViewGroup) findViewById(R.id.selected_photos_container);
                addphotos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Config config = new Config();
                        config.setSelectionMin(1);
                        config.setSelectionLimit(5);
                        config.setFlashOn(true);

                        ImagePickerActivity.setConfig(config);

                        Intent intent = new Intent(Report.this, ImagePickerActivity.class);

                        if (image_uris != null) {
                            intent.putParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS, image_uris);
                        }


                        startActivityForResult(intent, INTENT_REQUEST_GET_IMAGES);
                    }
                });
            }
        }, 500);
    }

    private void uploadPhotos() {
        picnames = new JSONObject();
        name = String.valueOf(Calendar.getInstance().getTimeInMillis());
        uploadMultipart(name, getRealPathFromURI(image_uris.get(0)));

    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public void uploadMultipart(String name, String path) {
        String mydomain = Url.mydomain + Url.upload;
        //getting name for the image
        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();
            System.setProperty("http.keepAlive", "false");
            //Creating a multi part request
            String mytype = path.substring(path.lastIndexOf("."));
            Log.d("MYTYPEE", "uploadMultipart: " + path);
            if (mydomain.length() > 0) {
                MultipartUploadRequest request = new MultipartUploadRequest(this, mydomain)
                        .addFileToUpload(path, "image") //Adding file
                        .addParameter("type", mytype)
                        .setMethod("POST")
                        .addParameter("name", name) //Adding text parameter to the request
                        .setNotificationConfig(new UploadNotificationConfig().setIcon(R.mipmap.ic_launcher).setTitle("Solo Uploader"))
                        .setMaxRetries(2);
                request.setDelegate(Report.this);
                request.startUpload();
            }
            //Starting the upload
        } catch (Exception exc) {
            exc.printStackTrace();
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == INTENT_REQUEST_GET_IMAGES) {

                image_uris = intent.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);

                if (image_uris != null) {
                    showMedia();
                }


            }
        }
    }

    private void showMedia() {
        // Remove all views before
        // adding the new ones.
        mSelectedImagesContainer.removeAllViews();
        if (image_uris.size() >= 1) {
            mSelectedImagesContainer.setVisibility(View.VISIBLE);
        }

        int wdpx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        int htpx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());


        for (Uri uri : image_uris) {

            View imageHolder = LayoutInflater.from(this).inflate(R.layout.photo_item, null);
            ImageView thumbnail = (ImageView) imageHolder.findViewById(R.id.media_image);

            Glide.with(this)
                    .load(uri.toString())
                    .into(thumbnail);

            mSelectedImagesContainer.addView(imageHolder);

            thumbnail.setLayoutParams(new FrameLayout.LayoutParams(wdpx, htpx));


        }

    }

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {

    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public void onCompleted(final Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        int picno = picnames.length();
        try {
            picnames.put(String.valueOf(picno), name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("SIZE", "onCompleted: " + picnames.length() + " " + image_uris.size());
        if (picnames.length() == image_uris.size()) {
            mymap.put("imagenames", picnames.toString());
            SharedPref pref = new SharedPref(context);
            mymap.put("miles", pref.getMiles());
            StringRequest request = new StringRequest(Request.Method.POST, Url.mydomain + Url.report, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "onResponse: " + response);
                    if (context != null)
                        Toast.makeText(context, "Thanks for reporting us", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Log.d(TAG, "getParams: " + mymap);
                    return mymap;
                }
            };
            Volley.newRequestQueue(context).add(request);
        } else {
            name = String.valueOf(Calendar.getInstance().getTimeInMillis());
            Log.d(TAG, "onCompleted: new name" + name);
            uploadMultipart(name, getRealPathFromURI(image_uris.get(picnames.length())));
        }
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
