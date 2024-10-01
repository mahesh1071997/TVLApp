package com.example.thevoicelibrarynet.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import androidx.core.view.MenuItemCompat;
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
import com.example.thevoicelibrarynet.Model.RecImageItem;
import com.example.thevoicelibrarynet.Network.NetworkBaseClass;
import com.example.thevoicelibrarynet.R;
import com.example.thevoicelibrarynet.Utilities.ImageUtilities;
import com.example.thevoicelibrarynet.Utilities.NetworkUtilities;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class ManageImageActivity extends AppCompatActivity {
    String mImageUploadURL = NetworkBaseClass.SERVERAPIURL;
    Context context;
    AlertDialog updDisplayFor, updDisplayAt, updCaption;
    int pos_dragged, pos_target;

    int possition;
    private static final String TAG = "ManageImage";
    private String authToken;
    private int mUserType;
    private String mRecordingDetailsAuthToken;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 24;
    private Uri mCaptureduri;
    private Bitmap mBitmapUploadImage;
    private ByteArrayOutputStream mByteArrayOutputStreamUploadImage;
    private byte[] mByteArrayUploadImage;
    private ByteArrayBody mImageBody;
    String mUploadImagePath, ErrorExist;
    String image_id;
    boolean isAdvansImage = false;
    boolean isLoopImage = true;
    String RecordingID, RecordingTitle, CardID, ThumbnailImageID, ImageDuration;
    EditText editTextImgDuraton;
    TextView recTotalDuration, equalizerBtn, addImageBtn, Lblduration;
    RecyclerView imgRecView;
    List<RecImageItem> recImageItems = new ArrayList<>();
    RecImageAdapter adapter;
    CheckBox checkboxAdvnc, checkboxLoop;
    long recDuration;
    private Uri mCropImageUri;
    static boolean addToDefault;

    LinearLayout imageListLayout, noImageLayout;
    //    FloatingActionButton floatingSaveButton;
    TextView addFirstImage;
    private boolean flage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_image);
        hideSoftKeyboard();

        context = ManageImageActivity.this;

        SharedPreferences mAuthTokenInformationSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        authToken = mAuthTokenInformationSharedPreference.getString("authToken", "");
        mUserType = mAuthTokenInformationSharedPreference.getInt("userType", 2);
        checkboxAdvnc = findViewById(R.id.checkboxAdvnc);
        checkboxLoop = findViewById(R.id.checkboxLoop);
        editTextImgDuraton = findViewById(R.id.editTextImgDuraton);
        Lblduration = findViewById(R.id.Lblduration);

        editTextImgDuraton.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (before == 0) {
//                String data = editTextImgDuraton.getText().toString();
                    EditRecoding(String.valueOf(s), "CImgDur");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        checkboxLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    EditRecoding("true", "CisLoop");
                } else {
                    EditRecoding("false", "CisLoop");
                }
            }
        });


        imageListLayout = findViewById(R.id.imageListLayout);
        noImageLayout = findViewById(R.id.noImagesLayout);
//        floatingSaveButton = findViewById(R.id.saveButton);
        addFirstImage = findViewById(R.id.addFirstImage);


        try {
            RecordingID = getIntent().getStringExtra("recordingid");
            CardID = getIntent().getStringExtra("cardid");
        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, "Null Pointer Exception");
        }
        recTotalDuration = findViewById(R.id.recTotalDuration);

        if (NetworkUtilities.isNetworkAvailable(context)) {
            GetRecordingDetails();
        }

        getSupportActionBar().setTitle(RecordingTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        imgRecView = findViewById(R.id.imgRecView);
        imgRecView.setLayoutManager(new LinearLayoutManager(context));

//        imgRecView.
        adapter = new RecImageAdapter();
        imgRecView.setAdapter(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(simpleCallback);
        helper.attachToRecyclerView(imgRecView);
        addImageBtn = findViewById(R.id.addImageBtn);
        addImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToDefault = false;
                if (checkAndRequestPermissions()) {
                    ImagePicker();
                }
            }
        });

        addFirstImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToDefault = false;
                if (checkAndRequestPermissions()) {
                    ImagePicker();
                }
            }
        });

        equalizerBtn = findViewById(R.id.equalizerBtn);
        equalizerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recImageItems.size() > 0) {
                    long avgDur = recDuration / recImageItems.size();
                    long reminder = recDuration % recImageItems.size();

                    for (int i = 0; i < recImageItems.size(); i++) {

                        if (recImageItems.size() - 1 == i) {

                            recImageItems.get(i).setDisplayIndex(recImageItems.get(i - 1).getDisplayIndex() + avgDur);
                            recImageItems.get(i).setDuration(avgDur + reminder);
                            editimageapi(i);
                        } else {
                            recImageItems.get(i).setDuration(avgDur);
                            if (i == 0) {
                                recImageItems.get(i).setDisplayIndex((long) 0);
                            } else {
                                recImageItems.get(i).setDisplayIndex(recImageItems.get(i - 1).getDisplayIndex() + avgDur);
                            }
                            editimageapi(i);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    Toast.makeText(context, "Duration Equalization Successful", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Please add images first", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    ItemTouchHelper.Callback simpleCallback = new ItemTouchHelper.Callback() {

        private boolean mOrderChanged;

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            final int drag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swip = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(drag, swip);
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder.itemView.findViewById(R.id.recItemLayout).setBackground(getResources().getDrawable(R.color.ThemeColorLight));
                pos_dragged = viewHolder.getAdapterPosition();
            }
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.findViewById(R.id.recItemLayout).setBackground(getResources().getDrawable(R.color.white));

            if (mOrderChanged) {
                pos_target = viewHolder.getAdapterPosition();
                mOrderChanged = false;
                abc(pos_dragged, pos_target);
            }

        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            mOrderChanged = true;
            imgRecView.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            try {
                mOrderChanged = false;
                switch (direction) {
                    case ItemTouchHelper.START:
                        Log.d(TAG + "if", "onSwiped() called with: viewHolder = [" + recImageItems.get(viewHolder.getAdapterPosition()).getImageId() + "], direction = [" + ThumbnailImageID + "]");

                        if (recImageItems.get(viewHolder.getAdapterPosition()).getImageId().equals(ThumbnailImageID)) {
                            viewHolder.itemView.findViewById(R.id.btnthum).setVisibility(View.GONE);
                        } else {
                            viewHolder.itemView.findViewById(R.id.btnthum).setVisibility(View.VISIBLE);
                        }
                        viewHolder.itemView.findViewById(R.id.btndelete).setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                        break;
                    case ItemTouchHelper.END:
                    default:
                        viewHolder.itemView.findViewById(R.id.btndelete).setVisibility(View.GONE);
                        viewHolder.itemView.findViewById(R.id.btnthum).setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                        break;
                }
            } catch (Exception e) {
            }
        }

    };

    private void DeleteAPi(String imgID) {

        final ProgressDialog mRecordingDetailsProgressDialog = ProgressDialog.show(context, "Delete image.", "Loading...", false, true);
        mRecordingDetailsProgressDialog.setCanceledOnTouchOutside(false);

        try {
            mRecordingDetailsAuthToken = URLEncoder.encode(authToken, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String mURLRecordingDetails = "https://app.thevoicelibrary.net/Services.aspx?returnType=json&action=deleteImage&imageID=" + imgID;
        StringRequest mRecordingDetailsNetworkRequest = new StringRequest(Request.Method.POST, mURLRecordingDetails, new Response.Listener<String>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onResponse(String response) {
                try {

                    JSONObject mJsonObjectRecordingDetails = new JSONObject(response);
                    String mIsErrorRecordingDetails = mJsonObjectRecordingDetails.isNull("errorsExist") ? "" : mJsonObjectRecordingDetails.optString("errorsExist");
                    if (mIsErrorRecordingDetails.equals("false")) {
                        Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                    }

                    if (mRecordingDetailsProgressDialog.isShowing())
                        mRecordingDetailsProgressDialog.dismiss();
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception Error");
                    e.printStackTrace();
                    if (mRecordingDetailsProgressDialog.isShowing())
                        mRecordingDetailsProgressDialog.dismiss();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mRecordingDetailsProgressDialog.isShowing())
                    mRecordingDetailsProgressDialog.dismiss();
                Log.d(TAG, "Response Error");
            }
        });

        //Add Network Request in request queue
        RequestQueue mRecordingDetailsNetworkRequestQueue = Volley.newRequestQueue(context);
        mRecordingDetailsNetworkRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRecordingDetailsNetworkRequest.setShouldCache(false);
        mRecordingDetailsNetworkRequestQueue.add(mRecordingDetailsNetworkRequest);
    }

    void abc(final int drag, final int target) {

        RecImageItem dragObj = new RecImageItem();
        RecImageItem TarObj = new RecImageItem();
        RecImageItem TempT = new RecImageItem();
        RecImageItem TdragObj = new RecImageItem();

        dragObj = recImageItems.get(pos_dragged);
        TarObj = recImageItems.get(pos_target);


        TempT.setCaption(TarObj.getCaption());
        TempT.setDuration(dragObj.getDuration());
        TempT.setDisplayIndex(dragObj.getDisplayIndex());
        TempT.setImagePath(TarObj.getImagePath());
        TempT.setImageId(TarObj.getImageId());

        TdragObj.setCaption(dragObj.getCaption());
        TdragObj.setDuration(TarObj.getDuration());
        TdragObj.setDisplayIndex(TarObj.getDisplayIndex());
        TdragObj.setImagePath(dragObj.getImagePath());
        TdragObj.setImageId(dragObj.getImageId());
        recImageItems.set(pos_dragged, TdragObj);
        recImageItems.set(pos_target, TempT);
        adapter.notifyDataSetChanged();


        final ProgressDialog[] mRecordingDetailsProgressDialog = new ProgressDialog[1];

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mRecordingDetailsProgressDialog[0] = ProgressDialog.show(context, "Edit Recording Details", "Loading...", false, true);
                mRecordingDetailsProgressDialog[0].setCanceledOnTouchOutside(false);
            }

            @Override
            protected Void doInBackground(Void... params) {
                editimageapi(drag);
                editimageapi(target);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mRecordingDetailsProgressDialog[0].dismiss();
                GetRecordingDetails();
            }
        };

        task.execute();
    }

    void editimageapi(int pos) {

//        final ProgressDialog mRecordingDetailsProgressDialog = ProgressDialog.show(context, "Edit image.", "Loading...", false, true);
//        mRecordingDetailsProgressDialog.setCanceledOnTouchOutside(false);
        final RecImageItem item = recImageItems.get(pos);
        try {

            Log.d(TAG, "run: ");
            //File file = new File(Path + outputFile + ".wav");
            HttpClient mImageUploadclient = new DefaultHttpClient();
            HttpPost mImageUploadPost = new HttpPost(mImageUploadURL);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("returnType", new StringBody("json"));
            builder.addPart("action", new StringBody("editImage"));
            builder.addPart("authToken", new StringBody(authToken));
            builder.addPart("cardId", new StringBody(CardID));
            builder.addPart("recordingId", new StringBody(RecordingID));
            builder.addPart("imageId", new StringBody(String.valueOf(item.getImageId())));
            builder.addPart("displayIndex", new StringBody(String.valueOf(item.getDisplayIndex())));
            builder.addPart("duration", new StringBody(String.valueOf(item.getDuration())));
            builder.addPart("caption", new StringBody(String.valueOf(item.getCaption())));
            
//when only edit image with peram
            if (flage) {
                flage = false;
                builder.addPart("imageFile", mImageBody);
            }

            HttpEntity entity = builder.build();
            mImageUploadPost.setEntity(entity);
            HttpResponse mAudioUploadResponse = mImageUploadclient.execute(mImageUploadPost);
            HttpEntity resEntity = mAudioUploadResponse.getEntity();
            final String mImageUploadResponseString = EntityUtils.toString(resEntity);
            if (resEntity != null) {
                JSONObject imageUploadJsonObject = new JSONObject(mImageUploadResponseString);
                Log.d(TAG, "run() called" + imageUploadJsonObject.toString());
                ErrorExist = imageUploadJsonObject.isNull("errorsExist") ? "" : imageUploadJsonObject.optString("errorsExist");
                GetRecordingDetails();
            }

//            if (mRecordingDetailsProgressDialog != null)
//                mRecordingDetailsProgressDialog.dismiss();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "run: " + e.getMessage());
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            Log.e(TAG, "run: " + e1.getMessage());
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        } catch (Exception ex) {
            Log.e(TAG, "run: " + ex.getMessage());
            Log.e("Debug", "error: " + ex.getMessage(), ex);
            ex.printStackTrace();
        } finally {
//            if (mRecordingDetailsProgressDialog != null)
//                mRecordingDetailsProgressDialog.dismiss();
        }

    }

    void EditRecoding(String Id, String cCasse) {

        final ProgressDialog mRecordingDetailsProgressDialog = ProgressDialog.show(context,
                "Edit Recoding.", "Loading...", false, true);
        mRecordingDetailsProgressDialog.setCanceledOnTouchOutside(false);

        try {
            Log.d(TAG, "run: ");
            //File file = new File(Path + outputFile + ".wav");
            HttpClient mImageUploadclient = new DefaultHttpClient();
            HttpPost mImageUploadPost = new HttpPost(mImageUploadURL);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("returnType", new StringBody("json"));
            builder.addPart("action", new StringBody("editRecording"));
            builder.addPart("authToken", new StringBody(authToken));
            builder.addPart("cardId", new StringBody(CardID));
            builder.addPart("recordingId", new StringBody(RecordingID));
            builder.addPart("recordingTitleText", new StringBody(RecordingTitle));

//            builder.addPart("recordingTitleText", new StringBody(RecordingTitle));

            switch (cCasse) {
                case "CID":
                    /*recordingNumber*/
                    builder.addPart("recordingAdvancedImages", new StringBody(isAdvansImage ? "true" : "false"));
//                    builder.addPart("recordingAdvancedImages", new StringBody(isAdvansImage+""));
                    builder.addPart("recordingLoopImages", new StringBody(isLoopImage ? "true" : "false"));
                    builder.addPart("recordingImageDuration", new StringBody(ImageDuration));
                    builder.addPart("thumbnailImageID", new StringBody(Id));
                    break;

                case "CisAdvc":
                    builder.addPart("recordingAdvancedImages", new StringBody(Id));
                    builder.addPart("recordingLoopImages", new StringBody(isLoopImage + ""));
                    builder.addPart("recordingImageDuration", new StringBody(ImageDuration));
                    builder.addPart("thumbnailImageID", new StringBody(ThumbnailImageID));
                    break;

                case "CisLoop":
                    builder.addPart("recordingAdvancedImages", new StringBody(isAdvansImage ? "true" : "false"));
                    builder.addPart("recordingLoopImages", new StringBody(Id));
                    builder.addPart("recordingImageDuration", new StringBody(ImageDuration));
                    builder.addPart("thumbnailImageID", new StringBody(ThumbnailImageID));
                    break;

                case "CImgDur":
                    builder.addPart("recordingAdvancedImages", new StringBody(isAdvansImage ? "true" : "false"));
                    builder.addPart("recordingLoopImages", new StringBody(isLoopImage ? "true" : "false"));
                    builder.addPart("recordingImageDuration", new StringBody(Id));
                    builder.addPart("thumbnailImageID", new StringBody(ThumbnailImageID));
                    break;
            }

            HttpEntity entity = builder.build();
            mImageUploadPost.setEntity(entity);
            HttpResponse mAudioUploadResponse = mImageUploadclient.execute(mImageUploadPost);
            HttpEntity resEntity = mAudioUploadResponse.getEntity();
            final String mImageUploadResponseString = EntityUtils.toString(resEntity);
            if (resEntity != null) {
                JSONObject imageUploadJsonObject = new JSONObject(mImageUploadResponseString);
                Log.d(TAG, "run() called" + imageUploadJsonObject.toString());
                ErrorExist = imageUploadJsonObject.isNull("errorsExist") ? "" : imageUploadJsonObject.optString("errorsExist");
                GetRecordingDetails();
            }
            if (mRecordingDetailsProgressDialog.isShowing())
                mRecordingDetailsProgressDialog.dismiss();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "run: " + e.getMessage());
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            Log.e(TAG, "run: " + e1.getMessage());
            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        } catch (Exception ex) {
            Log.e(TAG, "run: " + ex.getMessage());
            Log.e("Debug", "error: " + ex.getMessage(), ex);
            ex.printStackTrace();
        } finally {

            if (mRecordingDetailsProgressDialog.isShowing())
                mRecordingDetailsProgressDialog.dismiss();
        }

    }

    int UpdateTime(String Time) {
        String[] splitArr = Time.split(":");
        int hour = Integer.parseInt(splitArr[0].trim());
        int minit = Integer.parseInt(splitArr[1].trim());
        int Second = Integer.parseInt(splitArr[2].trim());
        return ((hour * 60 * 60) + (minit * 60) + Second);
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
       /* InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);*/
    }

    private String getTimeString(long millis) {
        millis = millis * 1000;


        StringBuffer buf = new StringBuffer();

        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = ((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000;
        buf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }

    private void GetRecordingDetails() {
        final ProgressDialog mRecordingDetailsProgressDialog = ProgressDialog.show(context, "Getting Recording Details", "Loading...", false, true);
        mRecordingDetailsProgressDialog.setCanceledOnTouchOutside(false);

        try {
            mRecordingDetailsAuthToken = URLEncoder.encode(authToken, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String mURLRecordingDetails = "https://app.thevoicelibrary.net/Services.aspx?returnType=json&action=getRecording&recordingID=" + RecordingID + "&authToken=" + mRecordingDetailsAuthToken;
        StringRequest mRecordingDetailsNetworkRequest = new StringRequest(Request.Method.POST, mURLRecordingDetails, new Response.Listener<String>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onResponse(String response) {
                try {

                    //Main Json object response
                    JSONObject mJsonObjectRecordingDetails = new JSONObject(response);
                    String mIsErrorRecordingDetails = mJsonObjectRecordingDetails.isNull("errorsExist") ? "" : mJsonObjectRecordingDetails.optString("errorsExist");
                    if (mIsErrorRecordingDetails.equals("false")) {

                        //Main Data Object
                        JSONObject mJsonObjectRecordingDetailsData = mJsonObjectRecordingDetails.getJSONObject("data");
                        JSONObject mJsonObjectRecordingDetailsRecordingList = mJsonObjectRecordingDetailsData.getJSONObject("recording");
                        if (mJsonObjectRecordingDetailsRecordingList.length() > 0) {

                            RecordingID = mJsonObjectRecordingDetailsRecordingList.isNull("RecordingID") ? "" : mJsonObjectRecordingDetailsRecordingList.optString("RecordingID");
                            RecordingTitle = mJsonObjectRecordingDetailsRecordingList.isNull("RecordingTitle") ? "" : mJsonObjectRecordingDetailsRecordingList.optString("RecordingTitle");
                            CardID = mJsonObjectRecordingDetailsRecordingList.isNull("CardID") ? "" : mJsonObjectRecordingDetailsRecordingList.optString("CardID");
                            ImageDuration = mJsonObjectRecordingDetailsRecordingList.isNull("ImageDuration") ? "5" : mJsonObjectRecordingDetailsRecordingList.optString("ImageDuration");
                            ThumbnailImageID = mJsonObjectRecordingDetailsRecordingList.isNull("ThumbnailImageID") ? "" : mJsonObjectRecordingDetailsRecordingList.optString("ThumbnailImageID");
                            recDuration = mJsonObjectRecordingDetailsRecordingList.isNull("Duration") ? 0 : Long.parseLong(mJsonObjectRecordingDetailsRecordingList.optString("Duration"));
                            isLoopImage = !mJsonObjectRecordingDetailsRecordingList.isNull("LoopImages") && mJsonObjectRecordingDetailsRecordingList.optBoolean("LoopImages");
                            recTotalDuration.setText(getTimeString(recDuration));
                            editTextImgDuraton.setText(ImageDuration);


                            isAdvansImage = !mJsonObjectRecordingDetailsRecordingList.isNull("AdvancedImages") && mJsonObjectRecordingDetailsRecordingList.optBoolean("AdvancedImages");
                            checkboxAdvnc.setChecked(isAdvansImage);

                            checkboxAdvnc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    if (b) {
                                        LoonierControl(false);
                                        isAdvansImage = true;
                                        adapter.notifyDataSetChanged();
                                        EditRecoding("true", "CisAdvc");
                                    } else {
                                        LoonierControl(true);
                                        isAdvansImage = false;
                                        adapter.notifyDataSetChanged();
                                        EditRecoding("false", "CisAdvc");

                                    }
                                }
                            });
                            checkboxLoop.setChecked(isLoopImage);

                            if (isAdvansImage) {
                                LoonierControl(false);
                            } else {
                                equalizerBtn.setVisibility(View.GONE);
                            }

                            JSONArray mRecordingImagesArray = mJsonObjectRecordingDetailsRecordingList.getJSONArray("Images");
                            if (mRecordingImagesArray.length() > 0) {

                                imageListLayout.setVisibility(View.VISIBLE);
                                noImageLayout.setVisibility(View.GONE);
                                recImageItems.clear();
                                adapter.notifyDataSetChanged();

                                for (int i = 0; i < mRecordingImagesArray.length(); i++) {

                                    JSONObject mImagesObject = mRecordingImagesArray.getJSONObject(i);
                                    String mImageId = mImagesObject.isNull("ImageID") ? "" : mImagesObject.optString("ImageID");
                                    String mImagePath = mImagesObject.isNull("ImageURL") ? "" : mImagesObject.optString("ImageURL");
                                    String mDisplayIndex = mImagesObject.isNull("DisplayIndex") ? "0" : mImagesObject.optString("DisplayIndex");
                                    String mDuration = mImagesObject.isNull("Duration") ? "0" : mImagesObject.optString("Duration");
                                    String mCaption = mImagesObject.isNull("Caption") ? "" : mImagesObject.optString("Caption");
                                    recImageItems.add(new RecImageItem(mImageId, mImagePath, Long.parseLong(mDisplayIndex), Long.parseLong(mDuration), mCaption));
                                }

                                adapter.notifyDataSetChanged();
                            } else {
//                                floatingSaveButton.setVisibility(View.GONE);
                                imageListLayout.setVisibility(View.GONE);
                                noImageLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    if (mRecordingDetailsProgressDialog.isShowing())
                        mRecordingDetailsProgressDialog.dismiss();

                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception Error");
                    e.printStackTrace();
                    if (mRecordingDetailsProgressDialog.isShowing())
                        mRecordingDetailsProgressDialog.dismiss();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mRecordingDetailsProgressDialog.isShowing())
                    mRecordingDetailsProgressDialog.dismiss();
                Log.d(TAG, "Response Error");
            }
        });


        //Add Network Request in request queue
        RequestQueue mRecordingDetailsNetworkRequestQueue = Volley.newRequestQueue(context);
        mRecordingDetailsNetworkRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 99900,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRecordingDetailsNetworkRequest.setShouldCache(false);
        mRecordingDetailsNetworkRequestQueue.add(mRecordingDetailsNetworkRequest);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_image_menu, menu);
        final MenuItem item = menu.findItem(R.id.default_image);
        View actionView = MenuItemCompat.getActionView(item);
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(item);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.default_image) {
            addToDefault = true;
            if (checkAndRequestPermissions()) {
                ImagePicker();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void ImageUpload() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final ProgressDialog mRecordingDetailsProgressDialog = ProgressDialog.show(context, "Upload Image", "Uploading...", false, true);
        mRecordingDetailsProgressDialog.setCanceledOnTouchOutside(false);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                if (flage) {
                    editimageapi(possition);
                } else {
                    DoFileUpload();
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (ErrorExist.equals("false")) {
                            if (mRecordingDetailsProgressDialog.isShowing())
                                mRecordingDetailsProgressDialog.dismiss();
                            Toast.makeText(context, "Image Uploaded Sucessfully", Toast.LENGTH_SHORT).show();
                            GetRecordingDetails();
                        } else {
                            if (mRecordingDetailsProgressDialog.isShowing())
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

            HttpClient mImageUploadclient = new DefaultHttpClient();
            HttpPost mImageUploadPost = new HttpPost(mImageUploadURL);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("returnType", new StringBody("json"));
            builder.addPart("action", new StringBody("newImage"));
            builder.addPart("cardID", new StringBody(CardID));
            builder.addPart("recordingID", new StringBody(RecordingID));
            builder.addPart("authToken", new StringBody(authToken));
            builder.addPart("imageFile", mImageBody);

            if (recImageItems.size() != 0) {
                RecImageItem item = recImageItems.get(recImageItems.size() - 1);
                Long x = item.getDisplayIndex() + item.getDuration();
                builder.addPart("displayIndex", new StringBody(String.valueOf(x)));
            }

            if (!checkboxAdvnc.isChecked()) {
                /*duration*/
                builder.addPart("duration", new StringBody("5"));
            }
            HttpEntity entity = builder.build();
            mImageUploadPost.setEntity(entity);
            HttpResponse mImageUploadResponse = mImageUploadclient.execute(mImageUploadPost);
            HttpEntity resEntity = mImageUploadResponse.getEntity();
            final String mImageUploadResponseString = EntityUtils.toString(resEntity);
            if (resEntity != null) {
                JSONObject imageUploadJsonObject = new JSONObject(mImageUploadResponseString);
                ErrorExist = imageUploadJsonObject.isNull("errorsExist") ? "" : imageUploadJsonObject.optString("errorsExist");
            }
        } catch (Exception ex) {
            Log.e("Debug", "error: " + ex.getMessage(), ex);
            ex.printStackTrace();
        }
    }

    private void ImagePicker() {
        CropImage.startPickImageActivity(this);
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .setInitialCropWindowPaddingRatio(0)
                .start(this);
    }

    private void recrop(String url) {
        flage = true;
        imageDownloader imageDownloader = new imageDownloader();
        try {
            Uri uri = imageDownloader.execute(url).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class imageDownloader extends AsyncTask<String, Void, Uri> {

        @Override
        protected Uri doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                // Create imageDir
                File mypath = new File(directory, "profile.jpg");
                FileOutputStream fos = null;

                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                Uri o1 = Uri.fromFile(new File(directory.getAbsolutePath() + "/profile.jpg"));
                startCropImageActivity(o1);
                return o1;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

    }

    //Method for check permision in marshmallow
    private boolean checkAndRequestPermissions() {

        if (ContextCompat.checkSelfPermission(context, CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{CAMERA, READ_EXTERNAL_STORAGE}, REQUEST_ID_MULTIPLE_PERMISSIONS);
            //Toast.makeText(context, "Permission Not Granted", Toast.LENGTH_SHORT).show();
            return false;
        }
//        int permissioncamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
//        int storagepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
//
//        List<String> listPermissionsNeeded = new ArrayList<>();
//        if (storagepermission != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.CAMERA);
//        }
//        if (permissioncamera != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
//        }
//        if (!listPermissionsNeeded.isEmpty()) {
//            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
//            return false;
//        }
//        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                //requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //imageView.setImageURI(result.getUri());
                try {
                    if (addToDefault) {
                        try {
                            ContextWrapper cw = new ContextWrapper(context);
                            File dir = cw.getDir("default", Context.MODE_PRIVATE);
                            File myFile = new File(dir, "default_image.png");
                            FileOutputStream fos = new FileOutputStream(myFile);
                            Bitmap bitmap = ImageUtilities.getInstant().getCompressedBitmap(result.getUri().getPath());
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            Toast.makeText(context, "Default Image Selected", Toast.LENGTH_LONG).show();
                        } catch (FileNotFoundException e) {

//                            holder.recImage.setImageResource(R.drawable.ic_def_img);
//                holder.recImage.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        } catch (Exception ex) {
                            Log.e(TAG, "onActivityResult: ", ex);
                            Toast.makeText(context, "Error while selecting default image: " + ex, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        mUploadImagePath = result.getUri().getPath();
                        //mUploadImagePath = getPath(result.getUri());
                        mBitmapUploadImage = ImageUtilities.getInstant().getCompressedBitmap(mUploadImagePath);
                        mByteArrayOutputStreamUploadImage = new ByteArrayOutputStream();
                        mBitmapUploadImage.compress(Bitmap.CompressFormat.PNG, 100, mByteArrayOutputStreamUploadImage);
                        mByteArrayUploadImage = mByteArrayOutputStreamUploadImage.toByteArray();
                        mImageBody = new ByteArrayBody(mByteArrayUploadImage, "CaptureImage.png");
                        ImageUpload();
                    }
                } catch (Exception ex) {
                    Log.e("CropImageFunction", "onActivityResult: ", ex);
                    Toast.makeText(context, "Error: " + ex, Toast.LENGTH_LONG).show();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //Toast.makeText(getContext(), "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    boolean permissionToCamera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToRead = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToCamera && permissionToRead) {
//                        ImagePicker();
                        //Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                } else {
                    checkAndRequestPermissions();
                }
                break;

            }
        }

    }

    private long Indexvalidation(int position, long oldDisIndex, long newDisIndex) {


        long newTime = newDisIndex;
        if (newTime < oldDisIndex) {
            long mindiff = (recImageItems.get(position - 1).getDuration() + recImageItems.get(position - 1).getDisplayIndex());
            if (position != 0) {
                if (mindiff < newTime) {
                    return UpdateTime(getTimeString(newTime));
                } else {
                    return mindiff;
                }
            }

        } else if (newTime > oldDisIndex) {
            if (position != recImageItems.size() - 1) {
                if (recImageItems.get(position + 1).getDisplayIndex() > newTime) {

                    return UpdateTime(getTimeString(newTime));
                } else {
                    return(recImageItems.get(position + 1).getDisplayIndex() - 1);
                }
            } else {
                if (newTime<= recDuration) {
                    return UpdateTime(getTimeString(newTime));
                } else {
                    return UpdateTime(getTimeString(recDuration));
                }
            }
        }
        return oldDisIndex;
    }

    private long DurationValidation(int position, long oldDurationFor, long newDurationFor) {
//        max value check
        long newTime = newDurationFor;
/*
        if(newDurationFor <= 0){
            return  0;
        }*/
        if (oldDurationFor < newTime) {
//                                        value is ++
            if (position != recImageItems.size() - 1) {
                long maxdiff = recImageItems.get(position + 1).getDisplayIndex() - recImageItems.get(position).getDisplayIndex();
                if (newTime <= maxdiff) {
                    return UpdateTime(getTimeString(newTime));
                } else {
                    return UpdateTime(getTimeString(maxdiff));
                }
            } else {
                long maxdiff = recDuration - recImageItems.get(position).getDisplayIndex();

                if (newTime <= maxdiff) {
                    return UpdateTime(getTimeString(newTime));
                } else {
                    return UpdateTime(getTimeString(maxdiff));
                }
            }
        } else if (oldDurationFor > newTime) {
//                                        there is value is --
            return UpdateTime(getTimeString(newTime));
        }
        return oldDurationFor;
    }

    private class RecImageAdapter extends RecyclerView.Adapter<RecImageAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {

            LinearLayout imageItemLayout;
            ImageView imageItem;
            Button deletebtn, thumbtn;
            TextView imageDisplayFor, imageDisplayIndex, imageCaption, lblDisat, lablDisfor;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                deletebtn = itemView.findViewById(R.id.btndelete);
                thumbtn = itemView.findViewById(R.id.btnthum);
                imageItemLayout = itemView.findViewById(R.id.imageItemLayout);
                imageItem = itemView.findViewById(R.id.imageItem);
                imageDisplayFor = itemView.findViewById(R.id.imageDisplayFor);
                imageDisplayIndex = itemView.findViewById(R.id.imageDisplayIndex);
                imageCaption = itemView.findViewById(R.id.imageCaption);
                lblDisat = itemView.findViewById(R.id.lablDisIndex);
                lablDisfor = itemView.findViewById(R.id.labldisFor);
            }
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View myView = LayoutInflater.from(context).inflate(R.layout.manage_image_listitem, parent, false);
            return new MyViewHolder(myView);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            final RecImageItem item = recImageItems.get(position);

            Picasso.get().load(item.getImagePath()).networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE).into(holder.imageItem);
            holder.thumbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String imgID = item.getImageId();
                    holder.deletebtn.setVisibility(View.GONE);
                    holder.thumbtn.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    EditRecoding(imgID, "CID");
                }
            });
            holder.deletebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.deletebtn.setVisibility(View.GONE);
                    holder.thumbtn.setVisibility(View.GONE);
                    String imgID = item.getImageId();
                    recImageItems.remove(position);
                    adapter.notifyDataSetChanged();
                    DeleteAPi(imgID);

                }
            });

            holder.imageDisplayFor.setText(getTimeString(item.getDuration()));
            holder.imageDisplayFor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    View v = LayoutInflater.from(context).inflate(R.layout.update_duration, null, false);
                    final TextView hourTV = v.findViewById(R.id.hourTextView);
                    final TextView minuteTV = v.findViewById(R.id.minuteTextView);
                    final TextView secondTV = v.findViewById(R.id.secondTextView);

                    final long oldDurationFor = item.getDuration();

                    setUpdateTime(holder.imageDisplayFor.getText().toString(), hourTV, minuteTV, secondTV);

                    v.findViewById(R.id.incrSecond).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            long newDur = DurationValidation(position, oldDurationFor, item.getDuration() + 1);
                            setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                            item.setDuration(newDur);
                        }
                    });

                    v.findViewById(R.id.incrMinute).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur = DurationValidation(position, oldDurationFor, item.getDuration() + 60);
                            setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                            item.setDuration(newDur);
                        }
                    });

                    v.findViewById(R.id.incrHour).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur = DurationValidation(position, oldDurationFor, item.getDuration() + 3600);
                            setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                            item.setDuration(newDur);
                        }
                    });

                    v.findViewById(R.id.decrSecond).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur = DurationValidation(position, oldDurationFor, item.getDuration() - 1);
                            if (newDur >= 0) {
                                setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                                item.setDuration(newDur);
                            }
                        }
                    });

                    v.findViewById(R.id.decrMinute).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur = DurationValidation(position, oldDurationFor, item.getDuration() - 60);
                            if (newDur >= 0) {
                                setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                                item.setDuration(newDur);
                            }
                        }
                    });

                    v.findViewById(R.id.decrHour).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur = DurationValidation(position, oldDurationFor, item.getDuration() - 3600);
                            if (newDur >= 0) {
                                setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                                item.setDuration(newDur);
                            }
                        }
                    });

                    updDisplayFor = new AlertDialog.Builder(context)
//                            .setTitle("Edit Display For Duration")
                            .setView(v)
                            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    possition = position;
                                    long newTime = item.getDuration();
//                                    max value check
                                    if (oldDurationFor < newTime) {
//                                        value is ++
                                        if (position != recImageItems.size() - 1) {
                                            long maxdiff = recImageItems.get(position + 1).getDisplayIndex() - recImageItems.get(position).getDisplayIndex();
                                            if (newTime <= maxdiff) {
                                                recImageItems.get(possition).setDuration((long) UpdateTime(getTimeString(recImageItems.get(position).getDuration())));
                                            } else {
                                                recImageItems.get(possition).setDuration((long) UpdateTime(getTimeString(maxdiff)));
                                            }
                                        } else {
                                            long maxdiff = recDuration - recImageItems.get(position).getDisplayIndex();

                                            if (newTime <= maxdiff) {
                                                recImageItems.get(possition).setDuration((long) UpdateTime(getTimeString(recImageItems.get(position).getDuration())));
                                            } else {
                                                recImageItems.get(possition).setDuration((long) UpdateTime(getTimeString(maxdiff)));
                                            }
                                        }
                                    } else if (oldDurationFor > newTime) {
//                                        there is value is --
                                        recImageItems.get(possition).setDuration((long) UpdateTime(getTimeString(recImageItems.get(position).getDuration())));
                                    } else {
                                    }
                                    UpdateDisplay();
                                    adapter.notifyDataSetChanged();

//                                    holder.imageDisplayFor.setText(getTimeString(item.getDuration()));
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    item.setDuration(oldDurationFor);
                                    holder.imageDisplayFor.setText(getTimeString(oldDurationFor));
                                    adapter.notifyDataSetChanged();
                                }
                            }).create();
                    updDisplayFor.show();
                    updDisplayFor.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.ThemeColor));
                    updDisplayFor.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                }
            });

            holder.imageDisplayIndex.setText(getTimeString(item.getDisplayIndex()));
            holder.imageDisplayIndex.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View v = LayoutInflater.from(context).inflate(R.layout.update_duration, null, false);
                    final TextView hourTV = v.findViewById(R.id.hourTextView);
                    final TextView minuteTV = v.findViewById(R.id.minuteTextView);
                    final TextView secondTV = v.findViewById(R.id.secondTextView);

                    final long oldDisIndex = item.getDisplayIndex();

                    setUpdateTime(holder.imageDisplayIndex.getText().toString(), hourTV, minuteTV, secondTV);

                    v.findViewById(R.id.incrSecond).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur = Indexvalidation(position,oldDisIndex,item.getDisplayIndex() + 1);
                            setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                            item.setDisplayIndex(newDur);
                        }
                    });

                    v.findViewById(R.id.incrMinute).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur = Indexvalidation(position,oldDisIndex,item.getDisplayIndex() + 60);
                            setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                            item.setDisplayIndex(newDur);

                        }
                    });

                    v.findViewById(R.id.incrHour).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur =Indexvalidation(position,oldDisIndex, item.getDisplayIndex() + 3600);
                            setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                            item.setDisplayIndex(newDur);
                        }
                    });

                    v.findViewById(R.id.decrSecond).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur = Indexvalidation(position,oldDisIndex,item.getDisplayIndex() - 1);
                            if (newDur >= 0) {
                                setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                                item.setDisplayIndex(newDur);
                            }
                        }
                    });

                    v.findViewById(R.id.decrMinute).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur = Indexvalidation(position,oldDisIndex,item.getDisplayIndex() - 60);
                            if (newDur >= 0) {
                                setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                                item.setDisplayIndex(newDur);
                            }
                        }
                    });

                    v.findViewById(R.id.decrHour).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            long newDur =Indexvalidation(position,oldDisIndex, item.getDisplayIndex() - 3600);
                            if (newDur >= 0) {
                                setUpdateTime(getTimeString(newDur), hourTV, minuteTV, secondTV);
                                recImageItems.get(position).setDisplayIndex(newDur);
                            }
                        }
                    });

                    updDisplayAt = new AlertDialog.Builder(context)
                            .setView(v)
                            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    possition = position;
                                    long newTime = recImageItems.get(position).getDisplayIndex();
                                    if (newTime < oldDisIndex) {
//                                        time is decres
                                        long mindiff = (recImageItems.get(position - 1).getDuration() + recImageItems.get(position - 1).getDisplayIndex());
                                        if (position != 0) {
                                            if (mindiff < newTime) {
                                                recImageItems.get(possition).setDisplayIndex((long) UpdateTime(getTimeString(recImageItems.get(position).getDisplayIndex())));
                                                UpdateDisplay();
                                            } else {
                                                recImageItems.get(position).setDisplayIndex(mindiff);
                                                holder.imageDisplayIndex.setText(getTimeString(mindiff));
                                                UpdateDisplay();

                                            }
                                        }

                                    } else if (newTime > oldDisIndex) {
//                                        time is incress
                                        long diff = newTime - oldDisIndex;
                                        if (position != recImageItems.size() - 1) {
                                            if (recImageItems.get(position + 1).getDisplayIndex() > newTime) {
//
                                                recImageItems.get(possition).setDisplayIndex((long) UpdateTime(getTimeString(item.getDisplayIndex())));

                                                if (diff < recImageItems.get(position).getDuration()) {
                                                    recImageItems.get(position).setDuration(recImageItems.get(position).getDuration() - diff);
                                                } else {
                                                    recImageItems.get(position).setDuration((long) 0);
                                                }


                                            } else {
                                                recImageItems.get(position).setDisplayIndex(recImageItems.get(position + 1).getDisplayIndex() - 1);
                                                holder.imageDisplayIndex.setText(getTimeString(recImageItems.get(position + 1).getDisplayIndex() - 1));

                                                if (diff < recImageItems.get(position).getDuration()) {
                                                    recImageItems.get(position).setDuration(recImageItems.get(position).getDuration() - diff);
                                                } else {
                                                    recImageItems.get(position).setDuration((long) 0);
                                                }


                                            }
                                        } else {

                                            if (recImageItems.get(position).getDisplayIndex() <= recDuration) {
                                                recImageItems.get(possition).setDisplayIndex((long) UpdateTime(getTimeString(recImageItems.get(position).getDisplayIndex())));
                                            } else {
                                                recImageItems.get(possition).setDisplayIndex((long) UpdateTime(getTimeString(recDuration)));
                                            }

                                            if (diff < recImageItems.get(position).getDuration()) {
                                                recImageItems.get(position).setDuration(recImageItems.get(position).getDuration() - diff);
                                            } else {
                                                recImageItems.get(position).setDuration((long) 0);
                                            }


                                        }
                                        UpdateDisplay();
                                        adapter.notifyDataSetChanged();
                                    }

                                    UpdateDisplay();
                                    adapter.notifyDataSetChanged();
                                }

                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    item.setDisplayIndex(oldDisIndex);
                                    holder.imageDisplayIndex.setText(getTimeString(oldDisIndex));
                                    adapter.notifyDataSetChanged();
                                }
                            }).create();
                    updDisplayAt.show();

                    updDisplayAt.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.ThemeColor));
                    updDisplayAt.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                }
            });

            /*addvance image option id false*/
            if (!isAdvansImage) {
                holder.imageDisplayIndex.setVisibility(View.GONE);
                holder.imageDisplayFor.setVisibility(View.GONE);
                holder.lablDisfor.setVisibility(View.GONE);
                holder.lblDisat.setVisibility(View.GONE);
            } else {
                holder.imageDisplayIndex.setVisibility(View.VISIBLE);
                holder.imageDisplayFor.setVisibility(View.VISIBLE);
                holder.lablDisfor.setVisibility(View.VISIBLE);
                holder.lblDisat.setVisibility(View.VISIBLE);
            }
            holder.imageCaption.setText(item.getCaption());
            holder.imageCaption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View v = LayoutInflater.from(context).inflate(R.layout.update_caption, null, false);
                    final EditText captionTV = v.findViewById(R.id.updImageCaption);
                    captionTV.setText(item.getCaption());
                    captionTV.setSelection(captionTV.getText().length());

                    updCaption = new AlertDialog.Builder(context)
                            .setTitle("Edit Caption")
                            .setView(v)
                            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    possition = position;
                                    item.setCaption(captionTV.getText().toString().trim());
                                    recImageItems.get(possition).setCaption(captionTV.getText().toString().trim());
                                    UpdateDisplay();
                                    adapter.notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            }).create();
                    updCaption.show();
                    updCaption.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.ThemeColor));
                    updCaption.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                }
            });

            holder.imageItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    addToDefault = false;
                    if (checkAndRequestPermissions()) {
                        possition = position;
//                        String x = recImageItems.get(position).getImagePath();
                        recrop(recImageItems.get(position).getImagePath());
                        Toast.makeText(context, "Click Image", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return recImageItems.size();
        }

    }

    void UpdateDisplay() {
        try {
            Log.d(TAG, "run: ");
            //File file = new File(Path + outputFile + ".wav");
            HttpClient mImageUploadclient = new DefaultHttpClient();
            HttpPost mImageUploadPost = new HttpPost(mImageUploadURL);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart("returnType", new StringBody("json"));
            builder.addPart("action", new StringBody("editImage"));
            builder.addPart("authToken", new StringBody(authToken));
            builder.addPart("cardId", new StringBody(CardID));
            builder.addPart("recordingId", new StringBody(RecordingID));
            builder.addPart("imageId", new StringBody(String.valueOf(recImageItems.get(possition).getImageId())));
            builder.addPart("displayIndex", new StringBody(String.valueOf(recImageItems.get(possition).getDisplayIndex())));
            builder.addPart("duration", new StringBody(String.valueOf(recImageItems.get(possition).getDuration())));
            builder.addPart("caption", new StringBody(String.valueOf(recImageItems.get(possition).getCaption())));

            HttpEntity entity = builder.build();
            mImageUploadPost.setEntity(entity);
            HttpResponse mAudioUploadResponse = mImageUploadclient.execute(mImageUploadPost);
            HttpEntity resEntity = mAudioUploadResponse.getEntity();
            final String mImageUploadResponseString = EntityUtils.toString(resEntity);
            JSONObject imageUploadJsonObject = new JSONObject(mImageUploadResponseString);
            Log.d(TAG, "run() called" + imageUploadJsonObject.toString());
            ErrorExist = imageUploadJsonObject.isNull("errorsExist") ? "" : imageUploadJsonObject.optString("errorsExist");
            GetRecordingDetails();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "run: " + e.getMessage());
            System.out.println("File Not Found.");
            e.printStackTrace();
        } catch (IOException e1) {
            Log.e(TAG, "run: " + e1.getMessage());

            System.out.println("Error Reading The File.");
            e1.printStackTrace();
        } catch (Exception ex) {
            Log.e(TAG, "run: " + ex.getMessage());

            Log.e("Debug", "error: " + ex.getMessage(), ex);
            ex.printStackTrace();
        }
    }

    public void setUpdateTime(String source, TextView hourObj, TextView minObj, TextView secObj) {

        String[] splitArr = source.split(":");
        hourObj.setText(splitArr[0].trim());
        minObj.setText(splitArr[1].trim());
        secObj.setText(splitArr[2].trim());
    }

    private void LoonierControl(boolean isShow) {
        checkboxLoop.setVisibility(isShow ? View.VISIBLE : View.GONE);
        editTextImgDuraton.setVisibility(isShow ? View.VISIBLE : View.GONE);
        Lblduration.setVisibility(isShow ? View.VISIBLE : View.GONE);
        equalizerBtn.setVisibility(isShow ? View.GONE : View.VISIBLE);
    }
/*

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    //Image Open from Gallery
    public void openGallery(int req_code) {

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        try {
            startActivityForResult(Intent.createChooser(intent, "Select file to upload "), req_code);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error", e.getMessage());
        }
    }

    public void captureImage(int req_code) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Image File Name");
        mCaptureduri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCaptureduri);

        try {
            //CropImage.startPickImageActivity(this);
            //startActivityForResult(intent, req_code);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error", e.getMessage());
        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new android.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();

    }
*/

}
