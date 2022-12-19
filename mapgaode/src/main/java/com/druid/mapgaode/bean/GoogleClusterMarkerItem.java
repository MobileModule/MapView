package com.druid.mapgaode.bean;

import com.amap.api.maps.model.LatLng;

import java.io.Serializable;

public class GoogleClusterMarkerItem implements Serializable {
    public LatLng latLng;
    public Object object;
    public int icon = 0;
    public String mTitle;
    public String mSnippet;
}
