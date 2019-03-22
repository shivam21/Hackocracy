package com.reportmeapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.gun0912.tedpicker.Config;
import com.gun0912.tedpicker.ImagePickerActivity;
import com.reportmeapp.R;
import com.reportmeapp.preferences.SharedPref;
import com.reportmeapp.url.Url;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Created by BHUSRI on 9/9/2017.
 */

public class Report extends AppCompatActivity {
    private static final String TAG = "ALLREPORT";
    private static final int INTENT_REQUEST_GET_IMAGES = 100;
    private RelativeLayout baseLayout;
    private ViewGroup mSelectedImagesContainer;
    private static ArrayList<Uri> image_uris = new ArrayList<Uri>();
    private static HashMap<String, String> mymap = new HashMap<>();
    private static String name;
    private static JSONObject picnames = new JSONObject();
    private String strLatitude, strLongitude;
    private ProgressBar progressBar;

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
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        image_uris.clear();
        GPSTracker gps = new GPSTracker(Report.this);
        if (gps.canGetLocation()) {
            final double latitude = gps.getLatitude();
            final double longitude = gps.getLongitude();
            gps.stopUsingGPS();
            if (latitude == 0.0 || longitude == 0.0) {
                // Toast.makeText(getApplicationContext(), "Gps haven't set yet,Please  try after sometime", Toast.LENGTH_SHORT).show();
            } else {
                strLatitude = String.valueOf(latitude);
                strLongitude = String.valueOf(longitude);
                GeocodeAsyncTask obj = new GeocodeAsyncTask();
                obj.execute();
            }

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

    private void uploadPhotos() {
        picnames = new JSONObject();
        name = String.valueOf(Calendar.getInstance().getTimeInMillis());
        Toast.makeText(Report.this, "Uploading Started", Toast.LENGTH_SHORT).show();
        uploadMultipart();

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

    public void uploadMultipart() {
        String mydomain = Url.mydomain + Url.upload;
        //getting name for the image
        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();
            System.setProperty("http.keepAlive", "false");
            //Creating a multi part request
            if (mydomain.length() > 0) {
                MultipartUploadRequest request = new MultipartUploadRequest(this, mydomain);
                for (int i = 0; i < image_uris.size(); i++) {
                    String path = getRealPathFromURI(image_uris.get(i));
                    // String mytype = path.substring(path.lastIndexOf("."));
                    Random rand = new Random();
                    int n = rand.nextInt(50) + 1;
                    String name = String.valueOf(Calendar.getInstance().getTimeInMillis() + n);
                    picnames.put("" + i, name);
                    request.addFileToUpload(path, "image" + i) //Adding file
                            .addParameter("myname" + i, name);//Adding text parameter to the request
                }
                request.addParameter("count", String.valueOf(image_uris.size()));
                request.setMethod("POST")
                        .setNotificationConfig(new UploadNotificationConfig().setRingToneEnabled(false))
                        .setMaxRetries(2);
                request.setDelegate(new UploadStatusDelegate() {
                    @Override
                    public void onProgress(Context context, UploadInfo uploadInfo) {

                    }

                    @Override
                    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                        exception.printStackTrace();
                        Log.d(TAG, "onError: " + uploadInfo);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, "Failed to upload.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCompleted(final Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                        Log.d(TAG, "onCompleted: ser" + serverResponse.getBodyAsString());
                        mymap.put("imagenames", picnames.toString());
                        SharedPref pref = new SharedPref(context);
                        mymap.put("miles", pref.getMiles());
                        StringRequest request = new StringRequest(Request.Method.POST, Url.mydomain + Url.report, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "onResponse: " + response);
                                if (context != null)
                                    Toast.makeText(context, "Thanks for reporting us", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Log.d(TAG, "getParams: " + mymap);
                                return mymap;
                            }
                        };
                        request.setRetryPolicy(new DefaultRetryPolicy(
                                20000,
                                0,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        Volley.newRequestQueue(context).add(request);
                    }

                    @Override
                    public void onCancelled(Context context, UploadInfo uploadInfo) {

                    }
                });
                request.startUpload();
                finish();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }


    class GeocodeAsyncTask extends AsyncTask<Void, Void, Address> {

        String errorMessage = "";

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Address doInBackground(Void... none) {
            Geocoder geocoder = new Geocoder(Report.this, Locale.getDefault());
            List<Address> addresses = null;


            double latitude = Double.parseDouble(strLatitude);
            double longitude = Double.parseDouble(strLongitude);

            try {
                Log.d(TAG, "doInBackground: " + latitude + " " + longitude);
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException ioException) {
                errorMessage = "Service Not Available";
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                errorMessage = "Invalid Latitude or Longitude Used";
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + latitude + ", Longitude = " +
                        longitude, illegalArgumentException);
            }


            if (addresses != null && addresses.size() > 0)
                return addresses.get(0);

            return null;
        }

        protected void onPostExecute(Address addresses) {
            final Spinner catspinner = (Spinner) findViewById(R.id.catspinner);
            final EditText description = (EditText) findViewById(R.id.description);
            final Button post = (Button) findViewById(R.id.post);
            final TextView location = (TextView) findViewById(R.id.location);
            //   String address = addresses.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            if (addresses != null) {
                final String city = addresses.getLocality();
                final String state = addresses.getAdminArea();
                // String country = addresses.getCountryName();
                String postalCode = addresses.getPostalCode();
                String knownName = addresses.getFeatureName();
                String text = "";
                if (knownName != null)
                    text = text + knownName + "\n";
                text = text + city + "," + state;
                if (postalCode != null)
                    text = text + "\nPincode:" + postalCode;

                location.setText(text);
            } else {
                location.setText("Location tracked(" + strLatitude + "," + strLongitude + ")");
            }
            post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String cat = (String) catspinner.getSelectedItem();
                    String desc = description.getText().toString().trim();
                    if (desc.length() < 10) {
                        // description.setError("Please provide more info.");
                        Toast.makeText(Report.this, "Please add more description", Toast.LENGTH_SHORT).show();
                    } else {
                        mymap.clear();
                        SharedPref pref = new SharedPref(Report.this);
                        mymap.put("category", cat);
                        mymap.put("description", desc);
                        mymap.put("lat", strLatitude);
                        mymap.put("long", strLongitude);
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
                                    finish();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(context, "Server error.\n Please try again  later", Toast.LENGTH_SHORT).show();

                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Log.d(TAG, "getParams: " + mymap);
                                    return mymap;
                                }
                            };
                            Volley.newRequestQueue(context).add(request);


                        }
                        progressBar.setVisibility(View.VISIBLE);

                    }
                }
            });
        }
    }
}
