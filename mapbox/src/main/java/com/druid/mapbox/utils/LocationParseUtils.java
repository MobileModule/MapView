package com.druid.mapbox.utils;

import android.location.Location;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;

public class LocationParseUtils {
    public static LocationBean getLocation(Location lastLocation) {
        if (lastLocation != null) {
            LatLngBean latLngBean = LatLngUtils.mapLatLngParse(lastLocation);
            LocationBean centerLocation = new LocationBean(latLngBean);
            centerLocation.accuracy = lastLocation.getAccuracy();
            return centerLocation;
        }
        return null;
    }
}
