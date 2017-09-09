package com.reportme.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


import com.reportme.R;
import com.reportme.preferences.SharedPref;
import com.reportme.url.Url;

/**
 * Created by BHUSRI on 9/6/2017.
 */

public class Signunppage extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    private GoogleApiClient mGoogleApiClient;
    private String TAG = "USERSIGNUP2";
    private View v;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        Snackbar.make(signInButton.getRootView(), "Please sign in to report", Snackbar.LENGTH_LONG).show();
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.fbsignin);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        JSONObject obj = response.getJSONObject();
                        Log.d(TAG, "onCompleted: fb" + obj.toString());
                        try {
                            final String id = obj.getString("id");
                            final String name = obj.getString("name");
                            setVolleyRequest(Signunppage.this, name, id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "name,id"); // Parámetros que pedimos a facebook
                request.setParameters(parameters);
                request.executeAsync();

                //   fbcredential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());


            }

            @Override
            public void onCancel() {

            }


            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();

            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        v = signInButton.getRootView();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            try {
                handleSignInResult(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (data != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) throws JSONException {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String name = acct.getDisplayName();
            String id = acct.getId();
            setVolleyRequest(Signunppage.this, name, id);
            // mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            Log.d(TAG, "handleSignInResult: " + result.getStatus().getStatusMessage());
            updateUI(false);
        }
    }

    private void setVolleyRequest(final Context roastmeSignup, final String name, final String id) {
        StringRequest request = new StringRequest(Request.Method.POST, Url.mydomain + Url.signup, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                SharedPref pref = new SharedPref(Signunppage.this);
                pref.setUserid(response);
                startActivity(new Intent(Signunppage.this, MapsActivity.class));
                finish();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                SharedPref pref = new SharedPref(Signunppage.this);
                map.put("name", name);
                map.put("id", id);
                map.put("token", pref.getfcmtoken());
                return map;

            }
        };
        Volley.newRequestQueue(roastmeSignup).add(request);
    }

    private void updateUI(boolean b) {
        if (!b) {
            Snackbar.make(v, "sign in failed", Snackbar.LENGTH_LONG).show();
        }
    }

}
