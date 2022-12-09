package com.druid.mapcore.utils;

import android.content.Context;

public class ResourceUtils {
    public static int getResourceIdFromName(String imageName, Context context) {
        int resId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
        return resId;
    }

    public static int getMarkerLargeResourceId(String imgNormalName, Context context) {
        String imgLargeName = imgNormalName + "_large";
        return getResourceIdFromName(imgLargeName, context);
    }
}
