package com.druid.mapcore.cluster;

import android.content.Context;

import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;

public abstract class BulkPointLayer implements
        MapClickListener, MapLoadedListener, MapOnScaleListener {
    protected Context context;

    public BulkPointLayer(Context context) {
        this.context = context;
    }

    public abstract void onDestroy();
}
