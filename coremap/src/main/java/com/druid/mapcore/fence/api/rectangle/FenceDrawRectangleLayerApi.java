package com.druid.mapcore.fence.api.rectangle;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.layer.LayerApi;

import java.util.List;

public interface FenceDrawRectangleLayerApi<T1, T2> extends LayerApi<T1, T2> {
    void confirmRectangleCropView();

    void resetRectangleCropView();

    List<LatLngBean> confirmDrawFenceRectangle();
}
