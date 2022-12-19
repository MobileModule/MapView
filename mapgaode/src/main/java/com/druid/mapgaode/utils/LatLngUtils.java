package com.druid.mapgaode.utils;

import android.location.Location;

import com.amap.api.maps.model.LatLng;
import com.druid.mapcore.bean.LatLngBean;

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
