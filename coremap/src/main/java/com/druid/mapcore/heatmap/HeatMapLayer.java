package com.druid.mapcore.heatmap;

import android.content.Context;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.layer.Layer;

import java.util.ArrayList;
import java.util.List;

public abstract class HeatMapLayer extends Layer implements MapLoadedListener {
    protected List<LatLngBean> source = new ArrayList<>();

    public HeatMapLayer(Context context) {
        super(context);
    }

}
