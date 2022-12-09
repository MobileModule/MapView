package com.druid.mapcore.fence.api.polygon;


import com.druid.mapcore.fence.view.marker.FenceCenterMark;
import com.druid.mapcore.interfaces.FencePointMarkerClickListener;

public interface FenceDrawPolygonByHandLayer <T1,T2> extends FencePolygonLayerApi<T1,T2> {
    FenceCenterMark getFenceCenterMark();

    void setFencePointMarkerClickListener(FencePointMarkerClickListener listener);//
}
