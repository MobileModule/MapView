package com.druid.mapgaode.cluster;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.collections.MarkerManager;

public class GoogleClusterManager<T> extends ClusterManager {
    public interface GoogleClusterMarkerClickListener {
        boolean onGoogleMarkerClick(Marker marker);
    }

    public GoogleClusterManager(Context context, GoogleMap map) {
        super(context, map);
    }

    public GoogleClusterManager(Context context, GoogleMap map, MarkerManager markerManager) {
        super(context, map, markerManager);
    }

    private GoogleClusterMarkerClickListener clusterMarkerClickListener;

    public void setClusterMarkerClickListener(GoogleClusterMarkerClickListener listener) {
        this.clusterMarkerClickListener = listener;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (clusterMarkerClickListener != null) {
            clusterMarkerClickListener.onGoogleMarkerClick(marker);//事件传递
        }
        return super.onMarkerClick(marker);
    }
}
