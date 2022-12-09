package com.druid.mapgoogle.interfaces;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class OnGoogleMarkerClickListener implements GoogleMap.OnMarkerClickListener {
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }
}
