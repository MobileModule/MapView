package com.druid.mapcore.circle;

import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.layer.LayerApi;

public interface CircleDrawLayerApi<T1, T2> extends LayerApi<T1, T2> {
    void setFenceCircle(FenceCoreBean fence);
}
