package com.example.thevoicelibrarynet.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubscriptionListActivity extends AppCompatActivity {

    Context context;

    RecyclerView subRecyclerView;
    List<SubscriptionItem> subscriptionItemList = new ArrayList<>();
    SubscriptionListAdapter adapter;

    private String authToken, mSubscriptionListAuthToken;

    private static final String TAG = "SubscriptionListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription_list);
        context = SubscriptionListActivity.this;
        getSupportActionBar().setTitle("Subscription List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        SharedPreferences mAuthTokenInformationSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        authToken = mAuthTokenInformationSharedPreference.getString("authToken", "");

        hideSoftKeyboard();

        if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
            GetSubScriptionList();
        } else {
            Toast.makeText(SubscriptionListActivity.this, "No internet connectivity", Toast.LENGTH_SHORT).show();
        }

        subRecyclerView = findViewById(R.id.subscriptionsRecyclerView);
        subRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        adapter = new SubscriptionListAdapter();
        subRecyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private class SubscriptionListAdapter extends RecyclerView.Adapter<SubscriptionListAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {

            LinearLayout subLayout;
            TextView subTitle, subCode;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                subLayout = itemView.findViewById(R.id.subItemLayout);
                subTitle = itemView.findViewById(R.id.subTitle);
                subCode = itemView.findViewById(R.id.subCode);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View myView = LayoutInflater.from(context).inflate(R.layout.subscription_list_item, parent, false);
            return new MyViewHolder(myView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final SubscriptionItem item = subscriptionItemList.get(position);
            holder.subTitle.setText(item.getsName());
            holder.subCode.setText(item.getsNumber());

            holder.subLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, RecordingListActivity.class);
                    i.putExtra("cardid", item.getCardId());
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return subscriptionItemList.size();
        }

    }

    private void GetSubScriptionList() {

        final ProgressDialog mSubscriptionListProgressDialog = ProgressDialog.show(SubscriptionListActivity.this, "Getting Subscriptions list", "Loading...", false, true);
        mSubscriptionListProgressDialog.setCanceledOnTouchOutside(false);

        try {

            mSubscriptionListAuthToken = URLEncoder.encode(authToken, "utf-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        String url = "https://mobile.thevoicelibrary.net/Services.aspx?returnType=json&action=getCardList&authToken=" + mSubscriptionListAuthToken;
        String url = "https://app.thevoicelibrary.net/Services.aspx?returnType=json&action=getCardList&authToken=" + mSubscriptionListAuthToken;

        StringRequest mSubscriptionListNetworkRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @SuppressLint("LongLogTag")
            @Override
            public void onResponse(String response) {

                try {

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
                            for (int i = 0; i < mArrayLength; i++) {
                                HashMap<String, String> mCardListData = new HashMap<String, String>();
                                JSONObject mJsonObjectCardList = mJsonArrayCardList.getJSONObject(i);
                                String cardId = mJsonObjectCardList.isNull("CardID") ? "" : mJsonObjectCardList.optString("CardID");
                                String productId = mJsonObjectCardList.isNull("ProductID") ? "" : mJsonObjectCardList.optString("ProductID");
                                String number = mJsonObjectCardList.isNull("Number") ? "" : mJsonObjectCardList.optString("Number");
                                String name = mJsonObjectCardList.isNull("Name") ? "" : mJsonObjectCardList.optString("Name");

                                subscriptionItemList.add(new SubscriptionItem(cardId, productId, number, name));
                            }

                            //Set Data to Adapter
                            adapter.notifyDataSetChanged();
                        }
                    }

                    if (mSubscriptionListProgressDialog != null) mSubscriptionListProgressDialog.dismiss();



                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception Error");
                    e.printStackTrace();
                    if (mSubscriptionListProgressDialog != null) mSubscriptionListProgressDialog.dismiss();
                }


            }
        }, new Response.ErrorListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mSubscriptionListProgressDialog != null) mSubscriptionListProgressDialog.dismiss();
                Log.d(TAG, "Response Error");
            }
        });


        //Add Network Request in request queue
        RequestQueue mSubscriptionListNetworkRequesttQueue = Volley.newRequestQueue(SubscriptionListActivity.this);
        mSubscriptionListNetworkRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mSubscriptionListNetworkRequest.setShouldCache(false);
        mSubscriptionListNetworkRequesttQueue.add(mSubscriptionListNetworkRequest);

    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
