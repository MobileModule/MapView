package com.druid.mapcore.cluster;

import android.content.Context;

import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.layer.Layer;

public abstract class ClusterItemLayer<T> extends Layer implements MapLoadedListener,
        MapInfoWindowClickListener<T>, MapMarkerClickListener<T> {

    public ClusterItemLayer(Context context) {
        super(context);
    }
}