package com.druid.mapcore.heatmap;

import com.druid.mapcore.bean.HeatMapSetBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.layer.LayerApi;

import java.util.List;

public interface HeatMapLayerApi<T1, T2> extends LayerApi<T1, T2> {
    void setHeatMapSource(List<LatLngBean> source);
    void setHeatMapSet(HeatMapSetBean set);
}
