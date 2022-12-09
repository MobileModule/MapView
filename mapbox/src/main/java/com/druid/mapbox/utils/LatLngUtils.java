package com.druid.mapbox.utils;

import android.location.Location;

import com.druid.mapcore.bean.LatLngBean;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class LatLngUtils {
    public static LatLngBean mapLatLngParse(LatLng latLng){
        return new LatLngBean(latLng.getLatitude(), latLng.getLongitude(), latLng.getAltitude());
    }

    public static LatLngBean mapLatLngParse(Location location){
        return new LatLngBean(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    public static LatLng mapLatLngFrom(LatLngBean latLng){
        return new LatLng(latLng.getLat(), latLng.getLng(), latLng.getAlt());
    }
}
