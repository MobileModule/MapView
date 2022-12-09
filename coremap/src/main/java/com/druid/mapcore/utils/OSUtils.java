package com.druid.mapcore.utils;

import android.content.Context;
import android.view.WindowManager;

public class OSUtils {
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        return width;
    }
}
