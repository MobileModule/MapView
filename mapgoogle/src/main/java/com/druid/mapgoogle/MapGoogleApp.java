package com.druid.mapgoogle;

import android.content.Context;

import androidx.annotation.NonNull;

import com.druid.mapcore.MapApp;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;

public class MapGoogleApp implements MapApp {
    protected static Context mapGoogleApp;

    @Override
    public void register(Context context) {
        if (mapGoogleApp == null) {
            mapGoogleApp = context;
            initializeMap();
        }
    }

    static void initializeMap() {
        MapsInitializer.Renderer preferredRenderer = null;//MapsInitializer.Renderer.LATEST;
        MapsInitializer.initialize(mapGoogleApp, preferredRenderer, new OnMapsSdkInitializedCallback() {
            @Override
            public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {

            }
        });

    }
}
