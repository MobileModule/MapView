package com.druid.mapcore.utils;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

public class BuildConfigUtils {
    public static boolean buildChina(Context context){
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().
                    getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String value = applicationInfo.metaData.getString("build_type");
            if (!TextUtils.isEmpty(value)) {
                if (value.equals("CHINA")) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }
}
