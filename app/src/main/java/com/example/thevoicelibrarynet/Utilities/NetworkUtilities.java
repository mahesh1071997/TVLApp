package com.example.thevoicelibrarynet.Utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtilities {


    public static String mMediaRecorderState = "";
    public static String mMediaRecorder = "";

    public static boolean isNetworkAvailable(Context context) {

        try{
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connectivityManager != null;
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();

        }catch (Exception e){
            return false;
        }
    }

}
