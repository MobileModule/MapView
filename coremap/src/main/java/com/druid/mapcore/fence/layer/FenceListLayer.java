package com.druid.mapcore.fence.layer;

import android.content.Context;

import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.PolygonClickListener;
import com.druid.mapcore.layer.Layer;

import java.util.ArrayList;

public abstract class FenceListLayer extends Layer implements MapLoadedListener,
        MapClickListener, PolygonClickListener {

    protected boolean  supportClick=true;

    protected ArrayList<FenceCoreBean> source=new ArrayList<>();

    public FenceListLayer(Context context) {
        super(context);
    }
}
