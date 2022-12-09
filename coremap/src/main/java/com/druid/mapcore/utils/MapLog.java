package com.druid.mapcore.utils;

import android.util.Log;

public class MapLog {
    public static void warning(String tag,String message){
        Log.w(tag, message);
    }

    public static void error(String tag,String message){
        Log.e(tag, message);
    }
}
