package com.reportme.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.reportme.R;
import com.reportme.preferences.SharedPref;
import com.reportme.url.Url;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by BHUSRI on 9/9/2017.
 */

public class Statistics extends AppCompatActivity {
    private static final String MYTAG = "STASTICS";
    private BarChart barChart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);
        barChart = (BarChart) findViewById(R.id.chart);
        Bundle b = getIntent().getExtras();
        final String lat = String.valueOf(b.getDouble("lat"));
        final String longitude = String.valueOf(b.getDouble("long"));
        if (b != null) {
            StringRequest request = new StringRequest(Request.Method.POST, Url.mydomain + Url.statistics, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Log.d(MYTAG, "onResponse: " + response);
                        JSONArray array = new JSONArray(response);
                        List<BarEntry> entries = new ArrayList<BarEntry>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            String cat = obj.getString("category");
                            int mycount = Integer.parseInt(obj.getString("mycount"));
                            int count2 = Integer.parseInt(obj.getString("count2"));
                            int ypercent = mycount * 100 / count2;
                            Log.d(MYTAG, "onResponse: " + ypercent);
                            switch (cat) {
                                case "Women Harassment":
                                    entries.add(new BarEntry(30, ypercent, ContextCompat.getDrawable(Statistics.this, R.drawable.women)));
                                    break;
                                case "Theft":
                                    entries.add(new BarEntry(50, ypercent, ContextCompat.getDrawable(Statistics.this, R.drawable.thief)));
                                    break;
                                case "Child Labour":
                                    entries.add(new BarEntry(70, ypercent, ContextCompat.getDrawable(Statistics.this, R.drawable.child)));
                                    break;
                                case "Corruption":
                                    entries.add(new BarEntry(10, ypercent, ContextCompat.getDrawable(Statistics.this, R.drawable.bribe)));
                                    break;
                                case "Other":
                                    entries.add(new BarEntry(90, ypercent, ContextCompat.getDrawable(Statistics.this, R.drawable.recycle)));
                                    break;
                            }

                        }
                        BarDataSet dataSet = new BarDataSet(entries, "Crime Statistics in your area");
                        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
                        BarData barData = new BarData(dataSet);
                        barData.setBarWidth(10);
                        barChart.setData(barData);
                        barChart.setFitBars(true);
                        barChart.invalidate();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

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
                    SharedPref pref = new SharedPref(Statistics.this);
                    map.put("lat", lat);
                    map.put("long", longitude);
                    map.put("miles", pref.getMiles());
                    return map;
                }
            };
            Log.d(MYTAG, "onCreate: started");
            Volley.newRequestQueue(this).add(request);
        }


    }
}
