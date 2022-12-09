package com.druid.mapcore.fence.api.polygon;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.layer.LayerApi;

import java.util.List;

public interface FencePolygonLayerApi<T1,T2> extends LayerApi<T1,T2> {
    void setMinFencePointSize(int size);

    boolean addFencePoint(LatLngBean latlng,boolean flatZoom);

    boolean removeFencePoint();

    boolean completeDrawFence();

    boolean fenceDrawStatus();//绘制是否结束

    boolean confirmEditorDrawFence();//确认修改围栏

    void resetDrawFence();

    List<LatLngBean> getFencePoints();
}
