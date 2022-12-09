package com.druid.mapcore.cluster;

import android.content.Context;

import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;


public abstract class ClusterPointLayer<T> implements
        MapClickListener, MapLoadedListener, MapOnScaleListener , MapCameraIdleListener,
        MapMarkerClickListener<T>, MapInfoWindowClickListener<T> {
    protected Context context;

    public ClusterPointLayer(Context context) {
        this.context = context;
    }

    public abstract void onDestroy();
}
