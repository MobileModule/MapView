package com.druid.mapgaode.bean;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class GoogleClusterMarkerBean implements ClusterItem {
    private GoogleClusterMarkerItem marker;
    private int index=0;

    public GoogleClusterMarkerBean(int index,GoogleClusterMarkerItem marker) {
        this.index=index;
        this.marker = marker;
    }

    public int getIndex(){
        return index;
    }

    @Override
    public LatLng getPosition() {
        return marker.latLng;
    }

    @Override
    public String getTitle() {
        return marker.mTitle;
    }

    @Override
    public String getSnippet() {
        return marker.mSnippet;
    }


    public GoogleClusterMarkerItem getMarker() {
        return marker;
    }
}
