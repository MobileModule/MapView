package com.druid.mapcore.capture;

import android.content.Context;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.MarkerBean;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.layer.Layer;

import java.util.ArrayList;

public abstract class CaptureDrawLayer extends Layer implements MapLoadedListener,
        MapClickListener {
    protected ArrayList<LatLngBean> source = new ArrayList<>();
    protected ArrayList<MarkerBean> markers = new ArrayList<>();

    public CaptureDrawLayer(Context context) {
        super(context);
    }
}
