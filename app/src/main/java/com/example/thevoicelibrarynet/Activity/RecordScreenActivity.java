package com.example.thevoicelibrarynet.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.thevoicelibrarynet.Network.NetworkBaseClass;
import com.example.thevoicelibrarynet.R;
import com.example.thevoicelibrarynet.Utilities.NetworkUtilities;

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
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class RecordScreenActivity extends AppCompatActivity {

    String mRecordingTitle;
    ImageView mImageViewStart, mImageViewPause, mImageViewStop;
    Chronometer new_recording_chronometer;
    MediaRecorder myAudioRecorder;
    String outputFile = "", Path = "", ErrorExist = "", authToken, CardID = "", RecordingID = "", mFileName = "";
    ProgressDialog mRecordingDetailsProgressDialog;
    File mAudioFile;
    File audiofile = null;
    String FinalAudioFile;
    FileInputStream fileInputStream;
    long timeWhenStopped = 0;
    Uploadclass12 op = new Uploadclass12();
    //PauseResumeAudioRecorder mediaRecorder;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    ImageView recStartStop;
    Button playBtn, uploadBtn;
    TextView recMsg, recTime;

    Boolean start = false;
    Boolean recorded = false;

    Context context;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_screen);

        context = RecordScreenActivity.this;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);


        NetworkUtilities.mMediaRecorderState = "";



        if(CheckPermissions()) {
            Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recording/";
            File mydir = new File(Path);
            if(!mydir.exists())
                mydir.mkdirs();
            else
                Log.d("error", "dir. already exists");

//            Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Recording/";
            Date date = new Date();
            String x = String.valueOf(date.getTime());
            outputFile = "Audio_" + x.replace(" ", "_").replace(":", "_").replace("+","0");
//            mFileName = Path + outputFile + ".wav";
            mFileName = Path + outputFile + ".wav";


            myAudioRecorder = new MediaRecorder();
            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            myAudioRecorder.setOutputFile(mFileName);
//            myAudioRecorder.setOutputFile(audiofile.getAbsolutePath());
            try {
                myAudioRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //mediaRecorder.setAudioFile(Path + outputFile);
        //Get authToken
        SharedPreferences mAuthTokenInformationSharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        authToken = mAuthTokenInformationSharedPreference.getString("authToken", "");

        try {

            mRecordingTitle = getIntent().getStringExtra("new_recording_title");
            getSupportActionBar().setTitle(mRecordingTitle);
            CardID = getIntent().getStringExtra("cardid");

        } catch (NullPointerException e) {
            e.printStackTrace();
            Log.d("Error", e.getMessage());
        }

        playBtn = findViewById(R.id.playBtn);

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ProgressDialog pd = new ProgressDialog(context);
                pd.setMessage("Playing recording....");
                pd.show();

                try {
                    if(CheckPermissions()) {

                        MediaPlayer mPlayer = new MediaPlayer();
                        mPlayer.setDataSource(mFileName);
                        mPlayer.prepare();
                        mPlayer.start();

                        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                pd.dismiss();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    pd.dismiss();
                }
            }
        });

        uploadBtn = findViewById(R.id.uploadBtn);
        recMsg = findViewById(R.id.recMsg);
        new_recording_chronometer = findViewById(R.id.rec_time);

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtilities.isNetworkAvailable(getApplicationContext())) {
                    RecordingUpload();

                } else {
                    Toast.makeText(context, "No internet connectivity", Toast.LENGTH_SHORT).show();
                }
            }
        });


        recStartStop = findViewById(R.id.rec_start_stop);
        recStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(start){
                    //mediaRecorder.pauseRecording();
                        myAudioRecorder.stop();
                        myAudioRecorder.release();

                    //Toast.makeText(context, "Stop & Release", Toast.LENGTH_SHORT).show();
                    timeWhenStopped = new_recording_chronometer.getBase() - SystemClock.elapsedRealtime();
                    new_recording_chronometer.stop();

                    recStartStop.setImageResource(R.drawable.ic_rec_start);
                    recMsg.setText("Tap to Record");
                    start = false;

                    recorded = true;

                    playBtn.setVisibility(View.VISIBLE);
                    uploadBtn.setVisibility(View.VISIBLE);

                } else {

                    try {
                        if (myAudioRecorder instanceof MediaRecorder &&
                                NetworkUtilities.mMediaRecorderState.equals("")) {
                            //mediaRecorder.startRecording();
                            myAudioRecorder.start();
                            //Toast.makeText(context, "Start", Toast.LENGTH_SHORT).show();

                            recStartStop.setImageResource(R.drawable.ic_rec_stop);
                            recMsg.setText("Recording...");
                            start = true;

                            recorded = false;

                            playBtn.setVisibility(View.INVISIBLE);
                            uploadBtn.setVisibility(View.INVISIBLE);

                            NetworkUtilities.mMediaRecorderState = "1";
                            new_recording_chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                                @Override
                                public void onChronometerTick(Chronometer chronometer) {
                                    long time = SystemClock.elapsedRealtime() - chronometer.getBase();

                                    int h = (int)(time/3600000);
                                    int m = (int)(time - h * 3600000) / 60000;
                                    int s = (int)(time - h * 3600000 - m * 60000) / 1000;

                                    String timeFormat = "";

                                    if(h > 0){
                                        String hh = h < 10 ? "0" + h : h + "";
                                        timeFormat += hh+"h ";
                                    }

                                    if(m > 0){
                                        String mm = m < 10 ? "0" + m : m + "";
                                        timeFormat += mm+"m ";
                                    }

                                    if(s > 0){
                                        String ss = s < 10 ? "0" + s : s + "";
                                        timeFormat += ss+"s";
                                    }
                                    chronometer.setText(timeFormat);
                                }
                            });
                            new_recording_chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                            new_recording_chronometer.start();
                        }
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mediaRecorder.getCurrentState() == PauseResumeAudioRecorder.RECORDING_STATE || mediaRecorder.getCurrentState() == PauseResumeAudioRecorder.PAUSED_STATE) {
//            mediaRecorder.stopRecording();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length> 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] ==  PackageManager.PERMISSION_GRANTED;

                    if (permissionToRecord && permissionToStore) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }
    public boolean CheckPermissions() {
        if(ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(context, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            Toast.makeText(context, "Permission Not Granted", Toast.LENGTH_SHORT).show();
            //finish();
            return false;
        }
    }

    public void RecordingUpload() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mRecordingDetailsProgressDialog = ProgressDialog.show(context, "Upload Recording", "Uploading...", false, true);
        mRecordingDetailsProgressDialog.setCanceledOnTouchOutside(false);
        op.execute();

       /* Thread thread = new Thread(new Runnable() {
            public void run() {
                DoFileUpload();
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (ErrorExist.equals("false")) {
                            if (mRecordingDetailsProgressDialog != null)
                                mRecordingDetailsProgressDialog.dismiss();
                            Toast.makeText(context, "Recording Uploaded Sucessfully", Toast.LENGTH_SHORT).show();
                            NetworkUtilities.mMediaRecorderState = "";

                            *//*Intent RecordingList = new Intent(context, RecordingListActivity.class);
                            RecordingList.putExtra("cardid", CardID);
                            startActivity(RecordingList);
                            finish();*//*
                        } else {
                            if (mRecordingDetailsProgressDialog != null)
                                mRecordingDetailsProgressDialog.dismiss();
                            Toast.makeText(context, "Please try again later", Toast.LENGTH_SHORT).show();

                        }

                    }
                });
            }
        });
        thread.start();*/
    }

   private  class Uploadclass12 extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {
            DoFileUpload();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//                pd = ProgressDialog.show(MainActivity.this, "", "Loading", true,
//                        false); // Create and show Progress dialog
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (ErrorExist.equals("false")) {
                if (mRecordingDetailsProgressDialog != null)
                    mRecordingDetailsProgressDialog.dismiss();
                Toast.makeText(context, "Recording Uploaded Sucessfully", Toast.LENGTH_SHORT).show();
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
//                pd.dismiss();
//                tvData.setText(result);
        }
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
