package com.example.thevoicelibrarynet.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.thevoicelibrarynet.Model.SubscriptionItem;
import com.example.thevoicelibrarynet.R;
import com.example.thevoicelibrarynet.Utilities.NetworkUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LoginScreenActivity extends AppCompatActivity {

    RadioGroup loginAs;
    RadioButton RecorderRB, ListenerRB;
    EditText UsernameET, PasswordET;
    CheckBox ShouldRememberCB;
    TextView SubscriptionLink, PrivacyPolicyLink;
    Button loginBtn;
    SharedPreferences mLoginInformationSharedPreference;

    String cardId;

    Context context;
    String authToken;

    private static final String TAG = "@@LoginScreenActivity";
    private String mSubscriptionListAuthToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        context = LoginScreenActivity.this;

        mLoginInformationSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (mLoginInformationSharedPreference.getBoolean("isChecked", false)) {

        }

        loginAs = findViewById(R.id.loginAs);
        RecorderRB = findViewById(R.id.RecorderRB);
        ListenerRB = findViewById(R.id.ListenerRB);

        UsernameET = findViewById(R.id.UsernameET);
        PasswordET = findViewById(R.id.PasswordET);
        ShouldRememberCB = findViewById(R.id.ShouldRememberCB);

        loginBtn = findViewById(R.id.loginBtn);

        if (mLoginInformationSharedPreference.getBoolean("isChecked", false)) {
            ShouldRememberCB.setChecked(true);
            if (mLoginInformationSharedPreference.getInt("userType", 1) == 2) {
                ListenerRB.setChecked(true);
            }

            UsernameET.setText(mLoginInformationSharedPreference.getString("Username", null));
            UsernameET.setSelection(UsernameET.getText().length());
            PasswordET.setText(mLoginInformationSharedPreference.getString("Password", null));
            PasswordET.setSelection(PasswordET.getText().length());
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();

                if (NetworkUtilities.isNetworkAvailable(context)) {
                    if (validateCredentials())
                        performLogin();
                }
            }
        });

        SubscriptionLink = findViewById(R.id.SubscriptionLink);
        SubscriptionLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.thevoicelibrary.net/"));
                startActivity(i);
            }
        });

        PrivacyPolicyLink = findViewById(R.id.PrivacyPolicyLink);
        PrivacyPolicyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.thevoicelibrary.net/privacy/"));
                startActivity(i);

            }
        });

    }
    public void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private int getUserType() {
        if (RecorderRB.isChecked())
            return 1;
        else
            return 2;
    }

    private void performLogin() {

        final String mUsername = UsernameET.getText().toString();
        final String mPassword = PasswordET.getText().toString();

        final ProgressDialog mLoginProgressDialog = ProgressDialog.show(context, "", "Authenticating...", false, true);
        mLoginProgressDialog.setCanceledOnTouchOutside(false);

//        String mUrl = "http://mobile.thevoicelibrary.net/Services.aspx?returnType=json&action=login&usertype=" + getUserType() + "&username=" + mUsername + "&password=" + mPassword;
//        String mUrl = "https://mobile.thevoicelibrary.net/Services.aspx?returnType=json&action=login&usertype=" + getUserType() + "&username=" + mUsername + "&password=" + mPassword;
        String mUrl = " https://app.thevoicelibrary.net/Services.aspx?returnType=json&action=login&usertype=" + getUserType() + "&username=" + mUsername + "&password=" + mPassword;

        StringRequest mLoginNetworkRequest = new StringRequest(Request.Method.POST, mUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    //Main Json object response
                    JSONObject mJsonObjectLogin = new JSONObject(response);
                    String mIsErrorLogin = mJsonObjectLogin.isNull("errorsExist") ? "" : mJsonObjectLogin.optString("errorsExist");

                    Log.d(TAG, "Login onResponse() called with: response = [" + response + "]");
                    //Json Object of data (authToken)
                    JSONObject mJsonObjectData = mJsonObjectLogin.getJSONObject("data");

                    if (mIsErrorLogin.equals("false")) {
                        //if error does not exist
                        //Current Date
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH/mm/ss");
                        String currentDateandTime = sdf.format(new Date());
                        Date date = sdf.parse(currentDateandTime);

                        //Date after 12 hour
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        calendar.add(Calendar.HOUR, 12);
                        String dateAfter12Hour = sdf.format(calendar.getTime());


                        //Get authToken from Object
                        authToken = mJsonObjectData.isNull("authToken") ? "" : mJsonObjectData.optString("authToken");
                        if (getUserType() == 2) {
                            cardId = mJsonObjectData.isNull("cardID") ? "" : mJsonObjectData.optString("cardID");
                        }
                        Log.d(TAG, "Token: before >>>"+authToken);
//                         "+" becomes "%2B" and the "=" becomes "%3D"
//                        authToken = authToken.replace("+","%2B").replace("=","%3D");
//                        Log.d(TAG, "Token: After replace  >>>"+ authToken);
                        SharedPreferences mLoginInformationSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor mSharedPreferencesEditor = mLoginInformationSharedPreference.edit();

                        //check if checkbox is checked or not
                        mSharedPreferencesEditor.putString("Username", mUsername);
                        mSharedPreferencesEditor.putString("Password", mPassword);
                        mSharedPreferencesEditor.putBoolean("isChecked", ShouldRememberCB.isChecked());
                        mSharedPreferencesEditor.putString("authToken", authToken);
                        mSharedPreferencesEditor.putInt("userType", getUserType());
                        mSharedPreferencesEditor.putString("authTokendate", dateAfter12Hour);

                        mSharedPreferencesEditor.commit();
                        mSharedPreferencesEditor.apply();

                    /*    Toast t = Toast.makeText(context, "Login Successfully!", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.TOP, 0, 500);
                        t.show();*/
                        if (mLoginProgressDialog != null) {mLoginProgressDialog.dismiss();}
                        if (getUserType() == 1) {
                            GetSubScriptionList();
                        } else {
                            Intent intent_recordinglist = new Intent(context, RecordingListActivity.class);
                            intent_recordinglist.putExtra("cardid", cardId);
                            startActivity(intent_recordinglist);
                            finish();
                        }

                    } else {
                        Toast t = Toast.makeText(context, "Check Your Credentials!", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.TOP, 0, 500);
                        t.show();
                        //Toast.makeText(context, "Check your credentials.Please try again", Toast.LENGTH_SHORT).show();
                        if (mLoginProgressDialog != null) {
                            mLoginProgressDialog.dismiss();
                        }
                    }

                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception Error");
                    e.printStackTrace();
                    if (mLoginProgressDialog != null)
                    {
                        mLoginProgressDialog.dismiss();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    if (mLoginProgressDialog != null)
                    {
                        mLoginProgressDialog.dismiss();
                    }
                    Log.d(TAG, "onResponse: "+e.getMessage());
                }


            }
        }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e(TAG, "onErrorResponse: Volley Error: ", error);

                if (mLoginProgressDialog != null){ mLoginProgressDialog.dismiss();}

                if (error instanceof NetworkError) {
                    Toast t = Toast.makeText(context, "Unable to connect to Internet!", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.TOP, 0, 500);
                    t.show();
                    //Toast.makeText(context, "Cannot connect to Internet...Please check your connection!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ServerError) {
                    Toast t = Toast.makeText(context, "Server not found. Please try agian after some time!", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.TOP, 0, 500);
                    t.show();
                    //Toast.makeText(context, "The server could not be found. Please try again after some time!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof AuthFailureError) {
                    Toast t = Toast.makeText(context, "Authentication Error!", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.TOP, 0, 500);
                    t.show();
                    //Toast.makeText(context, "Cannot connect to Internet...Please check your connection!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ParseError) {
                    Toast t = Toast.makeText(context, "Parsing Error!", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.TOP, 0, 500);
                    t.show();
                    //Toast.makeText(context, "Parsing error! Please try again after some time!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof NoConnectionError) {
                    Toast t = Toast.makeText(context, "Connection Error!", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.TOP, 0, 500);
                    t.show();
                    //Toast.makeText(context, "Cannot connect to Internet...Please check your connection!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof TimeoutError) {
                    Toast t = Toast.makeText(context, "Connection Timeout!", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.TOP, 0, 500);
                    t.show();
                    //Toast.makeText(context, "Connection TimeOut! Please check your internet connection.", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "Response Error");
            }
        }
        );


        //Add Network Request in request queue
        RequestQueue mLoginRequestQueue = Volley.newRequestQueue(context);
        mLoginNetworkRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mLoginRequestQueue.add(mLoginNetworkRequest);

    }

    private boolean validateCredentials() {
        if (TextUtils.equals(UsernameET.getText().toString(), "")) {
            Toast t = Toast.makeText(context, "Please Enter Username!", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP, 0, 500);
            t.show();
            //Toast.makeText(context, "Please enter username", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.equals(PasswordET.getText().toString(), "")) {
            Toast t = Toast.makeText(context, "Please Enter Password!", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP, 0, 500);
            t.show();
            //Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void GetSubScriptionList() {

        try {

            mSubscriptionListAuthToken = URLEncoder.encode(authToken, "utf-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        String url = "http://mobile.thevoicelibrary.net/Services.aspx?returnType=json&action=getCardList&authToken=" + mSubscriptionListAuthToken;
        String url = "https://app.thevoicelibrary.net/Services.aspx?returnType=json&action=getCardList&authToken=" + mSubscriptionListAuthToken;

        Log.d(TAG, "GetSubScriptionList: "+ url);
        StringRequest mSubscriptionListNetworkRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(String response) {

                try {
                    Log.d(TAG, "GetSubScriptionList onResponse: "+ response);
                    //Main Json object response
                    JSONObject mJsonObjectSubscriptionList = new JSONObject(response);
                    String mIsErrorSubscriptionList = mJsonObjectSubscriptionList.isNull("errorsExist") ? "" : mJsonObjectSubscriptionList.optString("errorsExist");
                    if (mIsErrorSubscriptionList.equals("false")) {

                        //Main Data Object
                        JSONObject mJsonObjectSubScriptionListData = mJsonObjectSubscriptionList.getJSONObject("data");

                        //CardList Array
                        JSONArray mJsonArrayCardList = mJsonObjectSubScriptionListData.getJSONArray("cardList");
                        int mArrayLength = mJsonArrayCardList.length();

                        Log.d("JSONData", "onResponse() called with: response = [" + response + "]");

                        if (mArrayLength > 0) {
                            if (mArrayLength == 1) {

                                JSONObject mJsonObjectCardList = mJsonArrayCardList.getJSONObject(0);
                                String cardId = mJsonObjectCardList.isNull("CardID") ? "" : mJsonObjectCardList.optString("CardID");

                                //move to subscription list page
                                Intent intent_recordinglist = new Intent(context, RecordingListActivity.class);
                                intent_recordinglist.putExtra("cardid", cardId);
                                startActivity(intent_recordinglist);
                                finish();

                            } else {
                                //move to login page
                                Intent intent_subscriptionlist = new Intent(context, SubscriptionListActivity.class);
                                startActivity(intent_subscriptionlist);
                                finish();
                            }
                        }
                    }

                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception Error");
                    e.printStackTrace();

                }


            }
        }, new Response.ErrorListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(TAG, "Response Error");
            }
        });


        //Add Network Request in request queue
        RequestQueue mSubscriptionListNetworkRequesttQueue = Volley.newRequestQueue(LoginScreenActivity.this);
        mSubscriptionListNetworkRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mSubscriptionListNetworkRequest.setShouldCache(false);
        mSubscriptionListNetworkRequesttQueue.add(mSubscriptionListNetworkRequest);

    }

}
