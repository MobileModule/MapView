package com.druid.mapcore.fence.api.circle;

import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.fence.view.circle.CircleView;
import com.druid.mapcore.interfaces.FenceCircleAreaChangedListener;
import com.druid.mapcore.layer.LayerApi;

public interface FenceDrawCircleLayerApi<T1, T2> extends LayerApi<T1, T2> {
    CircleView getCircleView();

    boolean completeDrawCircle();

    void setAreaChangedListener(FenceCircleAreaChangedListener listener);

    void setFenceCircle(FenceCoreBean fence);
}
