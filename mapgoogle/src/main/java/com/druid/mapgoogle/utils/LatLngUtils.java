package com.druid.mapgoogle.utils;

import android.location.Location;

import com.druid.mapcore.bean.LatLngBean;
import com.google.android.gms.maps.model.LatLng;

public class LatLngUtils {
    public static LatLngBean mapLatLngParse(LatLng latLng){
        return new LatLngBean(latLng.latitude, latLng.longitude);
    }

    public static LatLngBean mapLatLngParse(Location location){
        return new LatLngBean(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    public static LatLng mapLatLngFrom(LatLngBean latLng){
        return new LatLng(latLng.getLat(), latLng.getLng());
    }
}
