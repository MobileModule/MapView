package com.druid.mapcore.fence.api.polygon;

import com.druid.mapcore.bean.LatLngBean;

import java.util.List;

public interface FenceDrawPolygonByInputLayer<T1, T2> extends FencePolygonLayerApi<T1, T2> {
    boolean addFencePoints(List<LatLngBean> points);
}
