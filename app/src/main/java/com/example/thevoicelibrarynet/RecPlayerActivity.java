package com.example.thevoicelibrarynet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.util.Log;
import android.util.Range;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.thevoicelibrarynet.Activity.ManageImageActivity;
import com.example.thevoicelibrarynet.Activity.RecordingListActivity;
import com.example.thevoicelibrarynet.Model.RecImageItem;
import com.example.thevoicelibrarynet.Utilities.NetworkUtilities;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.NumericShaper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RecPlayerActivity extends AppCompatActivity {

    private String mImageUploadAuthToken, authToken, mRecordingDetailsAuthToken, RecordingID, mUploadImagePath, CardID, ErrorExist, mEncodedauthToken;
    private int mUserType;
    private MediaPlayer mediaPlayer;
    private ProgressDialog mRecordingDetailsProgressDialog;
    private SeekBar seekBar;
    private Timer timer;
    private NonSwipeableViewpager mParallaxViewPager;
    private RecordingImagesAdapter mGalleryAdapter;
    private int page;
    List<RecImageItem> recImageItems = new ArrayList<>();
    AlertDialog al;
    ImageView recPlayerMoreOption, recPlayerHelp, defaultImage;
    LinearLayout toggleControls, controls, mainControl;
    String[] options = {"Rename", "Manage Images", "Delete Recording"};
    Context context;
    BottomSheetBehavior sheetBehavior;
    Long ImageDuration;
    Boolean LoopImages;
    TextView currentTime;
    private boolean isAdvansImage;
    private static final String TAG = "RecPlayerActivity";

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_player);
        context = RecPlayerActivity.this;
        currentTime = (TextView) findViewById(R.id.currentTime);

        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        //getSupportActionBar().setTitle("Recording List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);

        SharedPreferences mAuthTokenInformationSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        authToken = mAuthTokenInformationSharedPreference.getString("authToken", "");
        mUserType = mAuthTokenInformationSharedPreference.getInt("userType", 2);

        mParallaxViewPager = findViewById(R.id.viewpager_recordingDetails);
        mGalleryAdapter = new RecordingImagesAdapter();
//        mParallaxViewPager.setAdapter(mGalleryAdapter);

        hideSoftKeyboard();

        defaultImage = findViewById(R.id.defaultImage);
        mainControl = findViewById(R.id.mainControl);
        sheetBehavior = BottomSheetBehavior.from(mainControl);
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                Log.d(TAG, "onStateChanged() called with: view = [" + view + "], i = [" + i + "]");
                switch (i) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if (mediaPlayer.isPlaying()) {
                            findViewById(R.id.miniPause).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.miniPlay).setVisibility(View.VISIBLE);
                        }
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        findViewById(R.id.miniPause).setVisibility(View.GONE);
                        findViewById(R.id.miniPlay).setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                Log.d(TAG, "onSlide() called with: view = [" + view + "], v = [" + v + "]");

            }
        });

        try {
            RecordingID = getIntent().getStringExtra("recordingid");
            CardID = getIntent().getStringExtra("cardid");

        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d(TAG, "Null Pointer Exception");
        }

        if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
//            GetRecordingDetails();
        } else {
            Toast.makeText(context, "No internet connectivity", Toast.LENGTH_SHORT).show();
        }

        recPlayerMoreOption = findViewById(R.id.recPlayerMoreOption);

        if (mUserType == 1) {
            recPlayerMoreOption.setVisibility(View.VISIBLE);
        }

        recPlayerMoreOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick() called with: view = [" + view + "]");

                AlertDialog optionAlertDialog;

                ListAdapter optionAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                        options) {

                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                        View myView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null, false);
                        TextView tv = myView.findViewById(android.R.id.text1);
                        tv.setText("   " + options[position]);
                        if (position == 2) {
                            tv.setTextColor(Color.WHITE);
                            tv.setBackgroundColor(Color.RED);
                        }
                        return myView;

                    }
                };

                AlertDialog.Builder adb = new AlertDialog.Builder(context);
                adb.setTitle("Recording Options");
                adb.setAdapter(optionAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int pos) {

                        //Toast.makeText(context, "Pos: " + pos, Toast.LENGTH_LONG).show();
                        if (pos == 0) {

                            dialogInterface.dismiss();

                            View v = LayoutInflater.from(context).inflate(R.layout.new_rec, null, false);

                            final EditText recTitle = v.findViewById(R.id.recTitle);

                            recTitle.setText(getSupportActionBar().getTitle());
                            Button doneBtn = v.findViewById(R.id.doneBtn);
                            Button cancelBtn = v.findViewById(R.id.cancelBtn);

                            doneBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //getSupportActionBar().setTitle(recTitle.getText().toString());
                                    EditRecording(recTitle.getText().toString());
                                    al.dismiss();
                                }
                            });

                            cancelBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    al.dismiss();
                                }
                            });

                            AlertDialog.Builder adb = new AlertDialog.Builder(context);
                            adb.setView(v);
                            al = adb.create();
                            al.show();

                        } else if (pos == 1) {
                            Intent i = new Intent(context, ManageImageActivity.class);
                            i.putExtra("recordingid", RecordingID);
                            i.putExtra("cardid", CardID);
                            startActivity(i);
                        } else if (pos == 2) {
                            dialogInterface.dismiss();
                            AlertDialog.Builder adb = new AlertDialog.Builder(context);
                            adb.setTitle("Delete Recording");
                            adb.setMessage("Are you sure you want to delete recording?");
                            adb.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    DeleteRecording();
                                }
                            });

                            adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            AlertDialog al2 = adb.create();
                            al2.show();

                            al2.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.ThemeColor));
                            al2.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.RED);
                        }
                    }
                });

                optionAlertDialog = adb.create();
                optionAlertDialog.show();

            }
        });
    }

    public void hideSoftKeyboard() {
        Log.d(TAG, "hideSoftKeyboard() called");
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private String getTimeString(long millis) {
//        Log.d(TAG, "getTimeString() called with: millis = [" + millis + "]");
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

    long oldIndex = 1;/*1 value set becouse the allways index staet  is 0*/
    long oldDura = 0;

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {

                //set max value
                int mDuration = mediaPlayer.getDuration();
                seekBar.setMax(mDuration);

                //update total time text view
                TextView totalTime = (TextView) findViewById(R.id.totalTime);
                totalTime.setText(getTimeString(mDuration));

                //set progress to current position
                final int mCurrentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(mCurrentPosition);

                //update current time text view
                currentTime.setText(getTimeString(mCurrentPosition));

                //handle drag on seekbar
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Log.d(TAG, "onStopTrackingTouch() called with: seekBar = [" + seekBar + "]");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Log.d(TAG, "onStartTrackingTouch() called with: seekBar = [" + seekBar + "]");
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        this method is not use
                        if (mediaPlayer != null && fromUser) {
                            mediaPlayer.seekTo(progress);
                        }

                        //showing image after duration
                        // progrss on milisecond
                        if ((progress / 1000) != 0) {

                            long lStart = mCurrentPosition / 1000;
                            for (int i = 0; i < recImageItems.size(); i++) {
                                long displayat = recImageItems.get(i).getDisplayIndex();
                                long currentDuration = recImageItems.get(i).getDuration() + displayat;
                                if (lStart == displayat) {
                                    if (oldIndex != displayat) {
                                        oldIndex = displayat;
                                        mParallaxViewPager.getRootView().findViewById(R.id.viewpager_recordingDetails).setVisibility(View.VISIBLE);
                                        if (i != 0) {
                                            Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                                            mParallaxViewPager.getRootView().findViewById(R.id.viewpager_recordingDetails).startAnimation(animFadeOut);
                                            mParallaxViewPager.setCurrentItem(i,false);
                                        }
                                    }
                                } else if (lStart == currentDuration)
                                {
                                    if (oldDura != currentDuration) {
                                        oldDura = currentDuration;
                                        if (mParallaxViewPager.getRootView().findViewById(R.id.viewpager_recordingDetails).isShown()) {
                                            Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                                            mParallaxViewPager.getRootView().findViewById(R.id.viewpager_recordingDetails).startAnimation(animFadeOut);
                                        }
                                        mParallaxViewPager.getRootView().findViewById(R.id.viewpager_recordingDetails).setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                    }
                });
            }
            //repeat above code every second
            mHandler.postDelayed(this, 100);
        }
    };


    private void EditRecording(final String mRecordingText) {

        final ProgressDialog mProgressDialog = ProgressDialog.show(context, "", "Please wait...", false, true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        String mSubscriptionListAuthToken = "", mTiTle = "";
        try {

            mSubscriptionListAuthToken = URLEncoder.encode(authToken, "utf-8");
            mTiTle = URLEncoder.encode(mRecordingText, "utf-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        String url = "http://mobile.thevoicelibrary.net/Services.aspx?returnType=json&action=editRecording&authToken=" + mSubscriptionListAuthToken + "&recordingID=" + RecordingID + "&recordingTitleText=" + mTiTle + "&recordingNumber=";
        String url = "https://app.thevoicelibrary.net/Services.aspx?returnType=json&action=editRecording&authToken=" + mSubscriptionListAuthToken + "&recordingID=" + RecordingID + "&recordingTitleText=" + mTiTle + "&recordingNumber=";

        StringRequest mNetworkRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //Main Json object response
                    JSONObject mJsonObjectList = new JSONObject(response);
                    String mIsErrorSubscriptionList = mJsonObjectList.isNull("errorsExist") ? "" : mJsonObjectList.optString("errorsExist");
                    if (mIsErrorSubscriptionList.equals("false")) {
                        //Main Data Object
                        JSONObject mJsonObjectListData = mJsonObjectList.getJSONObject("data");
                        getSupportActionBar().setTitle(mRecordingText);

                    } else {
                        Toast.makeText(context, "Please try again later", Toast.LENGTH_SHORT).show();
                    }

                    if (mProgressDialog != null) mProgressDialog.dismiss();

                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception Error");
                    e.printStackTrace();
                    if (mProgressDialog != null) mProgressDialog.dismiss();
                    Toast.makeText(context, "Please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Response Error");
                if (mProgressDialog != null) mProgressDialog.dismiss();
                Toast.makeText(context, "Please try again later", Toast.LENGTH_SHORT).show();
            }
        });

        //Add Network Request in request queue
        RequestQueue mtNetworkRequesttQueue = Volley.newRequestQueue(context);
        mNetworkRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mNetworkRequest.setShouldCache(false);
        mtNetworkRequesttQueue.add(mNetworkRequest);

    }

    public void play(View view) {
        Log.d(TAG, "play() called with: view = [" + view + "]");
        mediaPlayer.start();

        findViewById(R.id.pause).setVisibility(View.VISIBLE);
        findViewById(R.id.play).setVisibility(View.GONE);

        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            findViewById(R.id.miniPause).setVisibility(View.VISIBLE);
            findViewById(R.id.miniPlay).setVisibility(View.GONE);
        }
    }

    public void pause(View view) {
        Log.d(TAG, "pause() called with: view = [" + view + "]");
        mediaPlayer.pause();
        findViewById(R.id.play).setVisibility(View.VISIBLE);
        findViewById(R.id.pause).setVisibility(View.GONE);

        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            findViewById(R.id.miniPlay).setVisibility(View.VISIBLE);
            findViewById(R.id.miniPause).setVisibility(View.GONE);
        }

    }

    public void seekForward(View view) {
        Log.d(TAG, "seekForward() called with: view = [" + view + "]");
        //set seek time
        int seekForwardTime = 10000;

        // get current song position
        int currentPosition = mediaPlayer.getCurrentPosition();
        // check if seekForward time is lesser than song duration
        if (currentPosition + seekForwardTime <= mediaPlayer.getDuration()) {
            // forward song
            mediaPlayer.seekTo(currentPosition + seekForwardTime);
        } else {
            // forward to end position
            mediaPlayer.seekTo(mediaPlayer.getDuration());
        }

    }

    public void seekBackward(View view) {
//        mediaPlayer.pause();
        Log.d(TAG, "seekBackward() called with: view = [" + view + "]");
        //set seek time
        int seekBackwardTime = 10000;
        // get current song position
        int currentPosition = mediaPlayer.getCurrentPosition();
        // check if seekBackward time is greater than 0 sec
        if (currentPosition - seekBackwardTime >= 0) {
            // forward song
            mediaPlayer.seekTo(currentPosition - seekBackwardTime);
        } else {
            // backward to starting position
            mediaPlayer.seekTo(0);
        }
//        mediaPlayer.start();
    }

    public void onBackPressed() {
        Log.d(TAG, "onBackPressed() called");
        super.onBackPressed();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;

        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() called");
        super.onResume();
        recImageItems.clear();
        mGalleryAdapter.notifyDataSetChanged();
        if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
            GetRecordingDetails();
        } else {
            Toast.makeText(context, "No internet connectivity", Toast.LENGTH_SHORT).show();
        }
    }

    private void GetRecordingDetails() {
        //mRecordingDetailsProgressDialog = ProgressDialog.show(context, "Getting Recording Details", "Loading...", false, true);
        //mRecordingDetailsProgressDialog.setCanceledOnTouchOutside(false);
        try {
            mRecordingDetailsAuthToken = URLEncoder.encode(authToken, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String mURLRecordingDetails = "https://app.thevoicelibrary.net/Services.aspx?returnType=json&action=getRecording&recordingID=" + RecordingID + "&authToken=" + mRecordingDetailsAuthToken;
        StringRequest mRecordingDetailsNetworkRequest = new StringRequest(Request.Method.POST, mURLRecordingDetails, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    Log.d(TAG, "onResponse() called with: response = [" + response + "]");
                    //Main Json object response
                    JSONObject mJsonObjectRecordingDetails = new JSONObject(response);
                    String mIsErrorRecordingDetails = mJsonObjectRecordingDetails.isNull("errorsExist") ? "" : mJsonObjectRecordingDetails.optString("errorsExist");
                    if (mIsErrorRecordingDetails.equals("false")) {
                        //Main Data Object
                        JSONObject mJsonObjectRecordingDetailsData = mJsonObjectRecordingDetails.getJSONObject("data");
                        //CardList Array
                        JSONObject mJsonObjectRecordingDetailsRecordingList = mJsonObjectRecordingDetailsData.getJSONObject("recording");
                        if (mJsonObjectRecordingDetailsRecordingList.length() > 0) {

                            RecordingID = mJsonObjectRecordingDetailsRecordingList.isNull("RecordingID") ? "" : mJsonObjectRecordingDetailsRecordingList.optString("RecordingID");
                            final String RecordingTitle = mJsonObjectRecordingDetailsRecordingList.isNull("RecordingTitle") ? "" : mJsonObjectRecordingDetailsRecordingList.optString("RecordingTitle");
                            String AudioPath = mJsonObjectRecordingDetailsRecordingList.isNull("AudioURL") ? "" : mJsonObjectRecordingDetailsRecordingList.optString("AudioURL");
                            CardID = mJsonObjectRecordingDetailsRecordingList.isNull("CardID") ? "" : mJsonObjectRecordingDetailsRecordingList.optString("CardID");
                            ImageDuration = (long) (mJsonObjectRecordingDetailsRecordingList.isNull("ImageDuration") ? 5 : mJsonObjectRecordingDetailsRecordingList.optInt("ImageDuration"));
//                            LoopImages =mJsonObjectRecordingDetailsRecordingList.isNull("LoopImages") ? 0 : mJsonObjectRecordingDetailsRecordingList.optInt("LoopImages");
                            LoopImages = !mJsonObjectRecordingDetailsRecordingList.isNull("LoopImages") && mJsonObjectRecordingDetailsRecordingList.optBoolean("LoopImages");

                            isAdvansImage = !mJsonObjectRecordingDetailsRecordingList.isNull("AdvancedImages") && mJsonObjectRecordingDetailsRecordingList.optBoolean("AdvancedImages");

                            long auduiDuration = Long.valueOf(mJsonObjectRecordingDetailsRecordingList.isNull("Duration") ? 0 : mJsonObjectRecordingDetailsRecordingList.optInt("Duration"));


                            //Media Player
                            // create a media player
                            mediaPlayer = new MediaPlayer();
                            // try to load data and play
                            try {
                                // give data to mediaPlayer
                                if (AudioPath.length() > 0) {
                                    mediaPlayer.setDataSource(AudioPath);
                                    // media player asynchronous preparation
                                    mediaPlayer.prepareAsync();
                                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        public void onPrepared(final MediaPlayer mp) {
//                                            //start media player
//                                            mp.start();
//                                            findViewById(R.id.play).setVisibility(View.GONE);
                                            // link seekbar to bar view
                                            Log.d(TAG, "onPrepared() called with: mp = [" + mp + "]");
                                            seekBar = (SeekBar) findViewById(R.id.seekBar);

                                            //update seekbar
                                            mRunnable.run();
                                        }
                                    });

                                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mediaPlayer) {
//                                            Log.d(TAG, "onCompletion() called with: mediaPlayer = [" + mediaPlayer + "]");
                                            findViewById(R.id.pause).setVisibility(View.GONE);
                                            findViewById(R.id.play).setVisibility(View.VISIBLE);
                                            mediaPlayer.seekTo(0);
                                            mParallaxViewPager.setCurrentItem(0,false);
                                        }
                                    });

                                }
                                if (AudioPath.equals("")) {
                                    findViewById(R.id.controls).setVisibility(View.GONE);
                                    findViewById(R.id.controls_bar).setVisibility(View.GONE);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            //----------------------------------------------------------------------------

                            JSONArray mRecordingImagesArray = mJsonObjectRecordingDetailsRecordingList.getJSONArray("Images");

                            if (mRecordingImagesArray.length() > 0) {

                                mParallaxViewPager.setVisibility(View.VISIBLE);
                                defaultImage.setVisibility(View.GONE);

                                for (int i = 0; i < mRecordingImagesArray.length(); i++) {

                                    JSONObject mImagesObject = mRecordingImagesArray.getJSONObject(i);
                                    String mImageId = mImagesObject.isNull("ImageID") ? "" : mImagesObject.optString("ImageID");
                                    String mImagePath = mImagesObject.isNull("ImageURL") ? "" : mImagesObject.optString("ImageURL");
                                    String mDisplayIndex = mImagesObject.isNull("DisplayIndex") ? "" : mImagesObject.optString("DisplayIndex");
                                    String mDuration;

                                    if (!isAdvansImage) {
                                        mDuration = String.valueOf(ImageDuration);
                                        int x = Integer.parseInt(mDuration) * i;
                                        mDisplayIndex = String.valueOf(x);
                                    } else {
                                        mDuration = mImagesObject.isNull("Duration") ? "" : mImagesObject.optString("Duration");
                                    }
                                    String mCaption = mImagesObject.isNull("Caption") ? "" : mImagesObject.optString("Caption");
                                    if (!mDuration.equals("0")) {
                                        recImageItems.add(new RecImageItem(mImageId, mImagePath, Long.parseLong(mDisplayIndex), Long.parseLong(mDuration), mCaption));
                                    }

                                }

//                                calulation for addition image and loop image

                                if (LoopImages && !isAdvansImage) {
                                    long actualtime = ImageDuration * mRecordingImagesArray.length();
                                    if (actualtime < auduiDuration) {
                                        int addindex = (int) ((auduiDuration - actualtime) / ImageDuration);
                                        for (int i = 0; i <= addindex; i++) {
                                            RecImageItem item = recImageItems.get(i);
                                            Long arrsize = recImageItems.get(recImageItems.size() - 1).getDisplayIndex() + ImageDuration;

                                            recImageItems.add(new RecImageItem(item.getImageId(),
                                                    item.getImagePath(),
                                                    arrsize,
                                                    item.getDuration(),
                                                    item.getCaption()));
                                        }
                                    }
                                }

                                mGalleryAdapter.notifyDataSetChanged();
                                mParallaxViewPager.setAdapter(mGalleryAdapter);
                            } else {
                                mParallaxViewPager.setVisibility(View.GONE);
                                defaultImage.setVisibility(View.VISIBLE);
                                ContextWrapper cw = new ContextWrapper(context);
                                File dir = cw.getDir("default", Context.MODE_PRIVATE);
                                File myFile = new File(dir, "default_image.png");
                                try {
                                    Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(myFile));
                                    defaultImage.setImageBitmap(bitmap);
                                } catch (Exception ex) {
                                    Log.e(TAG, "onResponse: Error while loading default image: ", ex);
                                }
                            }
                            getSupportActionBar().setTitle(RecordingTitle);
                        }
                    }
                    if (mRecordingDetailsProgressDialog != null)
                        mRecordingDetailsProgressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (mRecordingDetailsProgressDialog != null)
                        mRecordingDetailsProgressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse() called with: error = [" + error + "]");
                if (mRecordingDetailsProgressDialog != null)
                    mRecordingDetailsProgressDialog.dismiss();
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

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
        try {
            mediaPlayer.stop();
        } catch (Exception e) {
        }
//        if (mediaPlayer.isPlaying()){
//        }
    }

    private void DeleteRecording() {
        try {
            mEncodedauthToken = URLEncoder.encode(authToken, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mRecordingDetailsProgressDialog = ProgressDialog.show(context, "Delete Recording", "Deleting...", false, true);
        mRecordingDetailsProgressDialog.setCanceledOnTouchOutside(false);

//        String mDeleteRecording = "http://mobile.thevoicelibrary.net/Services.aspx?returnType=json&action=deleteRecording&authToken=" + mEncodedauthToken + "&recordingID=" + RecordingID;
        String mDeleteRecording = "https://app.thevoicelibrary.net/Services.aspx?returnType=json&action=deleteRecording&authToken=" + mEncodedauthToken + "&recordingID=" + RecordingID;
        StringRequest mDeleteRecordingRequest = new StringRequest(Request.Method.POST, mDeleteRecording, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject responseRecordingDelete = new JSONObject(response);
                    String errorExist = responseRecordingDelete.isNull("errorsExist") ? "" : responseRecordingDelete.optString("errorsExist");

                    if (mRecordingDetailsProgressDialog != null)
                        mRecordingDetailsProgressDialog.dismiss();

                    Toast.makeText(context, "Recording deleted Successfully", Toast.LENGTH_SHORT).show();
                    RecPlayerActivity.super.onBackPressed();
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception Error");
                    e.printStackTrace();
                    if (mRecordingDetailsProgressDialog != null)
                        mRecordingDetailsProgressDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mRecordingDetailsProgressDialog != null)
                    mRecordingDetailsProgressDialog.dismiss();
                Log.d(TAG, "Response Error");
            }
        });

        RequestQueue mDeleteRecordingRequestQueue = Volley.newRequestQueue(context);
        mDeleteRecordingRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mDeleteRecordingRequestQueue.add(mDeleteRecordingRequest);
    }

    public class RecordingImagesAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return recImageItems.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = View.inflate(container.getContext(), R.layout.images_adapter, null);
            ImageView imageGallery = (ImageView) view.findViewById(R.id.image_recording);
            Picasso.get().load(recImageItems.get(position).getImagePath()).networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE).into(imageGallery);
            container.addView(view, LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);

            TextView descText = view.findViewById(R.id.desc_recording);
            if (!recImageItems.get(position).getCaption().trim().equals("")) {
                descText.setVisibility(View.VISIBLE);
                descText.setText(recImageItems.get(position).getCaption().trim());
            }
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}

