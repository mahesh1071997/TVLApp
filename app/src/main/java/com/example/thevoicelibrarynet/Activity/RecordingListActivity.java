package com.example.thevoicelibrarynet.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.thevoicelibrarynet.Model.RecordingItem;
import com.example.thevoicelibrarynet.Network.NetworkBaseClass;
import com.example.thevoicelibrarynet.R;
import com.example.thevoicelibrarynet.RecPlayerActivity;
import com.example.thevoicelibrarynet.Utilities.NetworkUtilities;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class RecordingListActivity extends AppCompatActivity {

    Context context;
    String id, title;
    int pos_dragged, pos_target;
    RecyclerView recRecyclerView;
    RecordingListAdapter adapter;
    String UrlThumbnail, IdThumbnail;
    LinkedList<RecordingItem> recordingItems = new LinkedList<>();
    String mImageUploadURL = NetworkBaseClass.SERVERAPIURL;
    String authToken, mRecordingListAuthToken, CardID;
    int mUserType = 2;
    AlertDialog titleAl, addAudioAl;
    private static final String TAG = "@@RecordingListActivity";

    String ErrorExist = "";
    String mFileName = "";
    String mRecordingTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        context = RecordingListActivity.this;
        getSupportActionBar().setTitle("Recording List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        SharedPreferences mAuthTokenInformationSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        authToken = mAuthTokenInformationSharedPreference.getString("authToken", "");
        mUserType = mAuthTokenInformationSharedPreference.getInt("userType", 2);

        hideSoftKeyboard();

        try {
            CardID = getIntent().getStringExtra("cardid");
            if (CardID == null) {
                CardID = "";
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, "Null Pointer Exception");
        }
        recRecyclerView = findViewById(R.id.recordingRecyclerView);
        recRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new RecordingListAdapter();
        recRecyclerView.setAdapter(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(simpleCallback);
        helper.attachToRecyclerView(recRecyclerView);

    }

    ItemTouchHelper.Callback simpleCallback = new ItemTouchHelper.Callback() {
        private boolean mOrderChanged;


        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

            final int drag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swip = ItemTouchHelper.START | ItemTouchHelper.END;
            if(mUserType == 2){
                return makeMovementFlags(0, 0);
            }
            else {
                return makeMovementFlags(drag, 0);
            }
        }


        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder.itemView.findViewById(R.id.recItemLayout).setBackground(getResources().getDrawable(R.color.ThemeColorLight));
                pos_dragged = viewHolder.getAdapterPosition();
                Log.d(TAG, "onSelectedChanged() called with: viewHolder = [" + viewHolder.getAdapterPosition() + "], actionState = [" + actionState + "]");
            }
           /* if(actionState == ItemTouchHelper.ACTION_STATE_IDLE)
            {
                pos_target = viewHolder.getAdapterPosition();
            }*/
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.findViewById(R.id.recItemLayout).setBackground(getResources().getDrawable(R.color.white));

            pos_target = viewHolder.getAdapterPosition();
            Log.d(TAG, "clearView() called with: targer " + pos_target);
            if (mOrderChanged) {
                mOrderChanged = false;
                task t = new task();
                t.execute();
            }
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            try {
                mOrderChanged = true;

                Log.d(TAG, "onMove: " + pos_target + "  draag =>  " + pos_dragged);
                Collections.swap(recordingItems,  viewHolder.getAdapterPosition(), target.getAdapterPosition());
                recRecyclerView.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return true;
            } catch (Exception e) {
                Log.d(TAG, "onMove: " + e);
                return false;
            }
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
    //    void abc() {
    final ProgressDialog[] mRecordingDetailsProgressDialog = new ProgressDialog[1];


    public class task extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mRecordingDetailsProgressDialog[0] = ProgressDialog.show(context, "Edit Recording Details", "Loading...", false, true);
            mRecordingDetailsProgressDialog[0].setCanceledOnTouchOutside(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (pos_dragged < pos_target) {
                for (int i = pos_dragged; i <= pos_target - 1; i++) {
                    String id = recordingItems.get(i).getRecordId();
                    String title = recordingItems.get(i).getTitleText();
                    String Number = recordingItems.get(i).getNumber();
                    try {
                        //File file = new File(Path + outputFile + ".wav");
                        HttpClient mImageUploadclient = new DefaultHttpClient();
                        HttpPost mImageUploadPost = new HttpPost(mImageUploadURL);
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                        builder.addPart("returnType", new StringBody("json"));
                        builder.addPart("action", new StringBody("editRecording"));
                        builder.addPart("authToken", new StringBody(authToken));
                        builder.addPart("recordingID", new StringBody(id));
                        builder.addPart("recordingTitleText", new StringBody(title));
                        int index = i;
                        builder.addPart("recordingNumber", new StringBody((index+1) + ""));
                        HttpEntity entity = builder.build();
                        mImageUploadPost.setEntity(entity);
                        HttpResponse mAudioUploadResponse = mImageUploadclient.execute(mImageUploadPost);
                        HttpEntity resEntity = mAudioUploadResponse.getEntity();
                        final String mImageUploadResponseString = EntityUtils.toString(resEntity);
                        if (resEntity != null) {
                            JSONObject imageUploadJsonObject = new JSONObject(mImageUploadResponseString);
                            ErrorExist = imageUploadJsonObject.isNull("errorsExist") ? "" : imageUploadJsonObject.optString("errorsExist");
                            System.out.println("api respons" + ErrorExist.toString());
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("File Not Found.");
                        e.printStackTrace();
                    } catch (IOException e1) {
                        System.out.println("Error Reading The File.");
                        e1.printStackTrace();
                    } catch (Exception ex) {
                        Log.e("Debug", "error: " + ex.getMessage(), ex);
                        ex.printStackTrace();
                    }
                }
            } else if (pos_dragged > pos_target) {
//                for (int i = pos_dragged; i >= pos_target + 1; i--) {
                    for(int i= pos_target; i<= pos_dragged ; i++ ){
                    String id_f = recordingItems.get(i).getRecordId();
                    String title_f = recordingItems.get(i).getTitleText();
                    String Number = recordingItems.get(i).getNumber();

                    try {
                        HttpClient mImageUploadclient = new DefaultHttpClient();
                        HttpPost mImageUploadPost = new HttpPost(mImageUploadURL);
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                        builder.addPart("returnType", new StringBody("json"));
                        builder.addPart("action", new StringBody("editRecording"));
                        builder.addPart("authToken", new StringBody(authToken));
                        builder.addPart("recordingID", new StringBody(id_f));
                        builder.addPart("recordingTitleText", new StringBody(title_f));
                        int index = i;
                        builder.addPart("recordingNumber", new StringBody((index+1)+ ""));
                        HttpEntity entity = builder.build();
                        mImageUploadPost.setEntity(entity);
                        HttpResponse mAudioUploadResponse = mImageUploadclient.execute(mImageUploadPost);
                        HttpEntity resEntity = mAudioUploadResponse.getEntity();
                        final String mImageUploadResponseString = EntityUtils.toString(resEntity);
                        if (resEntity != null) {

                            JSONObject imageUploadJsonObject = new JSONObject(mImageUploadResponseString);
                            ErrorExist = imageUploadJsonObject.isNull("errorsExist") ? "" : imageUploadJsonObject.optString("errorsExist");
//                                System.out.println("api respons" + ErrorExist);
                            Log.d(TAG, "doInBackground: " + ErrorExist);

                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("File Not Found.");
                        e.printStackTrace();
                    } catch (IOException e1) {
                        System.out.println("Error Reading The File.");
                        e1.printStackTrace();
                    } catch (Exception ex) {
                        Log.e("Debug", "error: " + ex.getMessage(), ex);
                        ex.printStackTrace();
                    }

                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mRecordingDetailsProgressDialog[0].dismiss();
//                GetRecordingDetails();
        }
    }

    ;

//    }

    private void GetRecordingList() {

        final ProgressDialog mRecordingListProgressDialog = ProgressDialog.show(RecordingListActivity.this, "Getting Recording list", "Loading...", false, true);
        mRecordingListProgressDialog.setCanceledOnTouchOutside(false);

        try {

            mRecordingListAuthToken = URLEncoder.encode(authToken, "utf-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        String mURLRecordingList = "https://mobile.thevoicelibrary.net/Services.aspx?returnType=json&action=getRecordingList&cardID=" + CardID + "&authToken=" + mRecordingListAuthToken;
        String mURLRecordingList = "https://app.thevoicelibrary.net/Services.aspx?returnType=json&action=getRecordingList&cardID=" + CardID + "&authToken=" + mRecordingListAuthToken;

        StringRequest mRecordingListNetworkRequest = new StringRequest(Request.Method.POST, mURLRecordingList, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    //Main Json object response
                    JSONObject mJsonObjectRecordingList = new JSONObject(response);
                    String mIsErrorRecordingList = mJsonObjectRecordingList.isNull("errorsExist") ? "" : mJsonObjectRecordingList.optString("errorsExist");
                    if (mIsErrorRecordingList.equals("false")) {

                        //Main Data Object
                        JSONObject mJsonObjectRecordingListData = mJsonObjectRecordingList.getJSONObject("data");
                        //CardList Array
                        JSONArray mJsonArrayRecordingList = mJsonObjectRecordingListData.getJSONArray("recordingList");
                        int mArrayLength = mJsonArrayRecordingList.length();

                        Log.d("JSONData: ", "onResponse() called with: response = [" + response + "]");

                        int Count = 1;

                        if (mArrayLength > 0) {
                            recordingItems.clear();

                            for (int i = 0; i < mArrayLength; i++) {
                                JSONObject mJsonObjectRecordingListDetails = mJsonArrayRecordingList.getJSONObject(i);

                                String mRecordId = mJsonObjectRecordingListDetails.isNull("RecordID") ? "" : mJsonObjectRecordingListDetails.optString("RecordID");
                                String mTitleText = mJsonObjectRecordingListDetails.isNull("TitleText") ? "" : mJsonObjectRecordingListDetails.optString("TitleText");
                                String Url = mJsonObjectRecordingListDetails.isNull("ThumbnailImageURL") ? "" : mJsonObjectRecordingListDetails.optString("ThumbnailImageURL");
                                String Number = mJsonObjectRecordingListDetails.isNull("Number") ? "" : mJsonObjectRecordingListDetails.optString("Number");
                                String ThumbnailImageID = mJsonObjectRecordingListDetails.isNull("ThumbnailImageID") ? "" : mJsonObjectRecordingListDetails.optString("ThumbnailImageID");

                                if (!mRecordId.trim().equals("") && !mTitleText.trim().equals(""))
//                                    recordingItems.add(new RecordingItem(mRecordId, mTitleText,Url));
                                    recordingItems.add(new RecordingItem(mRecordId, mTitleText, Number, ThumbnailImageID, Url));
                            }
                            //Set Data to Adapter
                            adapter.notifyDataSetChanged();

                        }

                    }


                    if (mRecordingListProgressDialog != null)
                        mRecordingListProgressDialog.dismiss();

                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception Error");
                    e.printStackTrace();
                    if (mRecordingListProgressDialog != null)
                        mRecordingListProgressDialog.dismiss();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mRecordingListProgressDialog != null)
                    mRecordingListProgressDialog.dismiss();
                Log.d(TAG, "Response Error");
            }
        });


        //Add Network Request in request queue
        RequestQueue mRecordingListNetworkRequesttQueue = Volley.newRequestQueue(RecordingListActivity.this);
        mRecordingListNetworkRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRecordingListNetworkRequest.setShouldCache(false);
        mRecordingListNetworkRequesttQueue.add(mRecordingListNetworkRequest);

    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        SharedPreferences mAuthTokenInformationSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        authToken = mAuthTokenInformationSharedPreference.getString("authToken", "");
        mUserType = mAuthTokenInformationSharedPreference.getInt("userType", 2);

        if (mUserType == 1)
            getMenuInflater().inflate(R.menu.recording_list_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean CheckPermissions() {
        if (ContextCompat.checkSelfPermission(context, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, 001);
            //Toast.makeText(context, "Permission Not Granted", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 001:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        //addRecording();
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                } else {
                    CheckPermissions();
                }
                break;
        }
    }

    public void addRecording() {

        View v = LayoutInflater.from(context).inflate(R.layout.new_rec, null, false);

        final EditText recTitle = v.findViewById(R.id.recTitle);
        Button doneBtn = v.findViewById(R.id.doneBtn);
        Button cancelBtn = v.findViewById(R.id.cancelBtn);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecordingTitle = recTitle.getText().toString().trim();
                if (mRecordingTitle != null && !mRecordingTitle.trim().equals("")) {

                    titleAl.dismiss();

                    AlertDialog.Builder adb = new AlertDialog.Builder(context)
                            .setTitle("Add Audio!")
                            .setItems(new String[]{"Start Recording", "Upload Existing"}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int pos) {
                                    if (pos == 0) {

                                        Intent i = new Intent(context, RecordScreenActivity.class);

                                        i.putExtra("new_recording_title", mRecordingTitle);
                                        i.putExtra("cardid", CardID);
                                        startActivity(i);
                                        //finish();

                                    } else if (pos == 1) {
                                        Intent intent_upload = new Intent();
                                        intent_upload.setType("audio/*");
                                        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                                        startActivityForResult(intent_upload, 1);

                                        //Toast.makeText(context, "Upload existing", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                    addAudioAl = adb.create();
                    addAudioAl.show();

                } else {
                    Toast.makeText(context, "Please enter Recording Title", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                titleAl.dismiss();
            }
        });
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setView(v);
        titleAl = adb.create();
        titleAl.show();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_rec) {
            if (CheckPermissions()) {
                addRecording();
            }
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //the selected audio.
                try {

                    mFileName = data.getData().getPath();

                    Cursor c = getContentResolver().query(data.getData(),
                            null, null, null, null);

                    String displayName = "";
                    if (c != null && c.moveToFirst()) {
                        Log.d(TAG, "\n\nonActivityResult: From INTERNAL_CONTENT_URI");
                        do {
                            for (int i = 0; i < c.getColumnCount(); i++) {
                                displayName = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                                Log.d(TAG, "onActivityResult: From Data == cursor[" + c.getColumnName(i) + "]" + c.getString(i));
                            }
                        } while (c.moveToNext());
                    }

                    Cursor c2 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            null, null, null, null);

                    if (c2 != null && c2.moveToFirst()) {
                        Log.d(TAG, "\n\nonActivityResult: From INTERNAL_CONTENT_URI");
                        do {
                            for (int i = 0; i < c2.getColumnCount(); i++) {

                                if (c2.getString(c2.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)).equals(displayName)) {
                                    mFileName = c2.getString(c2.getColumnIndex(MediaStore.Audio.Media.DATA));
                                }

                                Log.d(TAG, "onActivityResult: From EXT_URI == cursor[" + c2.getColumnName(i) + "]" + c2.getString(i));
                            }
                        } while (c2.moveToNext());
                    }

//
//                    Log.d(TAG, "onActivityResult: Filename: " + data.getData());
                    UploadExistingAudio();
                } catch (Exception ex) {
                    //Toast.makeText(context, "Error: " + ex, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "onActivityResult: ", ex);
                }

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public class RecordingListAdapter extends RecyclerView.Adapter<RecordingListAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {

            LinearLayout recItemLayout;
            ImageView recImage;
            TextView recTitle, recDetail;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                recItemLayout = itemView.findViewById(R.id.recItemLayout);
                recImage = itemView.findViewById(R.id.recDefaultImage);
                recTitle = itemView.findViewById(R.id.recTitle);
                recDetail = itemView.findViewById(R.id.recDetails);

            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View myView = LayoutInflater.from(context).inflate(R.layout.rec_list_item, parent, false);
            return new MyViewHolder(myView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            final RecordingItem item = recordingItems.get(position);
            try {
                Picasso.get().load(item.getThumbnailImageURL()).networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.recImage);
            } catch (Exception ex) {
                holder.recImage.setImageResource(R.drawable.ic_def_img);
            }
            holder.recTitle.setText(item.getTitleText());
            holder.recDetail.setText(item.getRecordId());

            holder.recItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //open play screen
                    Intent i = new Intent(context, RecPlayerActivity.class);
                    i.putExtra("recordingid", item.getRecordId());
                    i.putExtra("RecTitle", item.getTitleText());
                    i.putExtra("cardid", CardID);
                    startActivity(i);
                }
            });

            holder.recItemLayout.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    id = item.getRecordId();
                    title = item.getTitleText();
                    return false;
                }
            });

        }

        @Override
        public int getItemCount() {
            return recordingItems.size();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (addAudioAl instanceof AlertDialog)
            addAudioAl.dismiss();

        if (titleAl instanceof AlertDialog)
            titleAl.dismiss();

        recordingItems.clear();
        GetRecordingList();


    }


    public void UploadExistingAudio() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final ProgressDialog mRecordingDetailsProgressDialog = ProgressDialog.show(context, "Upload Recording", "Uploading...", false, true);
        mRecordingDetailsProgressDialog.setCanceledOnTouchOutside(false);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                DoFileUpload();
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (ErrorExist.equals("false")) {
                            if (mRecordingDetailsProgressDialog != null)
                                mRecordingDetailsProgressDialog.dismiss();
                            Toast.makeText(context, "Audio Uploaded Sucessfully", Toast.LENGTH_SHORT).show();
                            NetworkUtilities.mMediaRecorderState = "";

                            Intent RecordingList = new Intent(context, RecordingListActivity.class);
                            RecordingList.putExtra("cardid", CardID);
                            startActivity(RecordingList);
                            finish();

                        } else {
                            if (mRecordingDetailsProgressDialog != null)
                                mRecordingDetailsProgressDialog.dismiss();
                            Toast.makeText(context, "Please try again later", Toast.LENGTH_SHORT).show();

                        }

                    }
                });
            }
        });
        thread.start();
    }

    public void DoFileUpload() {

        String mImageUploadURL = NetworkBaseClass.SERVERAPIURL;
        try {

            //File file = new File(Path + outputFile + ".wav");
            File file = new File(mFileName);
            ContentBody audioFile = new FileBody(file);
            HttpClient mImageUploadclient = new DefaultHttpClient();
            HttpPost mImageUploadPost = new HttpPost(mImageUploadURL);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("returnType", new StringBody("json"));
            builder.addPart("action", new StringBody("newRecording"));
            builder.addPart("cardID", new StringBody(CardID));
            builder.addPart("recordingTitleText", new StringBody(mRecordingTitle));
            builder.addPart("recordingID", new StringBody(""));
            builder.addPart("authToken", new StringBody(authToken));
            builder.addPart("audioFile", audioFile);
            HttpEntity entity = builder.build();
            mImageUploadPost.setEntity(entity);
            HttpResponse mAudioUploadResponse = mImageUploadclient.execute(mImageUploadPost);
            HttpEntity resEntity = mAudioUploadResponse.getEntity();
            final String mImageUploadResponseString = EntityUtils.toString(resEntity);
            if (resEntity != null) {
                JSONObject imageUploadJsonObject = new JSONObject(mImageUploadResponseString);
                ErrorExist = imageUploadJsonObject.isNull("errorsExist") ? "" : imageUploadJsonObject.optString("errorsExist");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        } catch (Exception ex) {
            Log.e("Debug", "error: " + ex.getMessage(), ex);
            ex.printStackTrace();
        }

    }
}
