package com.druid.mapbox.utils;

import android.view.Gravity;

import com.druid.mapcore.setting.MapConstant;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.UiSettings;

public class MapConstantUtils extends MapConstant {
    public static void configMapUISetting(MapboxMap mapboxMap) {
        mapboxMap.setMaxZoomPreference(MapConstantUtils.MAP_MAX_ZOOM);
        UiSettings uiSettings = mapboxMap.getUiSettings();
        uiSettings.setCompassGravity(Gravity.LEFT | Gravity.TOP);
        uiSettings.setLogoEnabled(false);
        uiSettings.setAttributionEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
    }

    public static void enableGesture(MapboxMap mapboxMap, boolean enable) {
        UiSettings uiSettings = mapboxMap.getUiSettings();
        if (enable) {
            uiSettings.setScrollGesturesEnabled(true);
            uiSettings.setZoomGesturesEnabled(true);
        } else {
            uiSettings.setScrollGesturesEnabled(false);
            uiSettings.setZoomGesturesEnabled(false);
            uiSettings.setAllGesturesEnabled(false);
        }
    }

    public static final String Street = "mapbox://styles/0987363/cl1bxbjt0000215myqqyo12ey";//"mapbox://styles/mapbox/streets-v10";
    public static final String Satellite = "mapbox://styles/0987363/cl1bxb42h000v15kvvp26c27j";
    public static final String Outdoors = //"mapbox://styles/0987363/cl1bx9et0000214swgg9av42f";
            "mapbox://styles/0987363/cl1bxbjt0000215myqqyo12ey";
}
