package com.druid.mapgoogle.utils;

import android.animation.TypeEvaluator;

import com.google.android.gms.maps.model.LatLng;

public class LatLngEvaluator implements TypeEvaluator<LatLng> {

    @Override
    public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
        double latitude = (startValue.latitude + ((endValue.latitude - startValue.latitude) * fraction));
        double longitude = (startValue.longitude + ((endValue.longitude - startValue.longitude) * fraction));
        return new LatLng(latitude, longitude);
    }
}
