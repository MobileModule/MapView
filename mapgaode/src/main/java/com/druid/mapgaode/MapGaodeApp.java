package com.druid.mapgaode;

import android.content.Context;

import com.amap.api.maps.MapsInitializer;
import com.druid.mapcore.MapApp;


public class MapGaodeApp implements MapApp {
    protected static Context mapGaodeApp;

    @Override
    public void register(Context context) {
        if (mapGaodeApp == null) {
            mapGaodeApp = context;
            initializeMap();
        }
    }

    static void initializeMap() {
        MapsInitializer.initialize(mapGaodeApp);
    }
}
