package com.druid.mapcore.utils;

import android.content.Context;

public class MapSettingConstantUtils {
    public static final int MAP_BOUNDS_PADDING = 200;
    public static float getRectangleWidthMask(Context context){
        return DimenUtils.dip2px(context,3);
    }
}
