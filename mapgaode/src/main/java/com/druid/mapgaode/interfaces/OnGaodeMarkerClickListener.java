package com.druid.mapgaode.interfaces;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;


public class OnGaodeMarkerClickListener implements AMap.OnMarkerClickListener {
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }
}
