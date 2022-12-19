package com.druid.mapcore.utils;

public class ColorUtils {
    public static int getColorR(int color){
        int r = ((color >> 16) & 0xff);
        return r;
    }

    public static int getColorG(int color){
        int g = ((color >> 8) & 0xff);
        return g;
    }

    public static int getColorB(int color){
        int b = ((color) & 0xff);
        return b;
    }

    public static int getColorA(int color){
        int a = ((color >> 24) & 0xff);
        return a;
    }
}
