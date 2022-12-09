package com.druid.mapcore.fence.layer;

import android.content.Context;

import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.layer.Layer;

public abstract class FenceDrawLayer<T> extends Layer implements MapLoadedListener,
        MapCameraIdleListener , MapClickListener , MapLocationChangedListener , MapMarkerClickListener<T> {

    public FenceDrawLayer(Context context) {
        super(context);
    }
}
