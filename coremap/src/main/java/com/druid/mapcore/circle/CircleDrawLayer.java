package com.druid.mapcore.circle;

import android.content.Context;

import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.layer.Layer;

public abstract class CircleDrawLayer extends Layer implements MapLoadedListener {
    public CircleDrawLayer(Context context) {
        super(context);
    }
}
