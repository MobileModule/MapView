package com.druid.mapbox;

import android.content.Context;

import com.druid.mapcore.MapApp;
import com.mapbox.mapboxsdk.MapStrictMode;
import com.mapbox.mapboxsdk.Mapbox;

public class MapBoxApp implements MapApp {
    protected static Context mapBoxApp;

    @Override
    public void register(Context context) {
        if(mapBoxApp==null) {
            mapBoxApp = context;
            initializeMap();
        }
    }

    static void initializeMap() {
        String accessToken =
                "pk.eyJ1IjoiMDk4NzM2MyIsImEiOiJjajc2eGl5dTYxMzNnMndtdGhvYWZ3YnNpIn0.weCf8k385xoNj7WAV4V1MA";
        //"pk.eyJ1IjoiMDk4NzM2MyIsImEiOiJjamxvdG95MmcxeGw4M3huM3BlNDBwZXl0In0.WPfmosoz6qujmJXjxOTU6g";
        Mapbox.getInstance(mapBoxApp, accessToken);
        MapStrictMode.setStrictModeEnabled(true);
    }
}
