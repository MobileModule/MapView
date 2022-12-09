package com.druid.map.app;

import android.content.Context;

import com.druid.mapcore.MapApp;
import com.druid.mapcore.utils.BuildConfigUtils;

public class DruidMapApp {
    public static void initializeMap(Context context) {
        try {
            Class mapBoxAppClass = Class.forName("com.druid.mapbox.MapBoxApp");
            MapApp mapBoxApp = (MapApp) mapBoxAppClass.newInstance();
            mapBoxApp.register(context);
            if (!BuildConfigUtils.buildChina(context)) {
                Class mapGoogleAppClass = Class.forName("com.druid.mapgoogle.MapGoogleApp");
                MapApp mapGoogleApp = (MapApp) mapGoogleAppClass.newInstance();
                mapGoogleApp.register(context);
            }
        } catch (Exception ex) {

        }
    }
}
