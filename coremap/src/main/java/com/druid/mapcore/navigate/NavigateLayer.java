package com.druid.mapcore.navigate;

import android.content.Context;

import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.layer.Layer;

public abstract class NavigateLayer extends Layer implements MapLoadedListener, MapLocationChangedListener {
    public NavigateLayer(Context context) {
        super(context);
    }

    protected abstract MapLocationChangedListener getLocationChangedListener();
}
