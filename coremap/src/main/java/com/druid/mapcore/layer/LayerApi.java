package com.druid.mapcore.layer;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;

/**
 * Layer 基础功能
 */
public interface LayerApi<T1, T2> {
    void bindMap(T1 mapview, T2 map);

    void attachDruidMap(DruidMapView druidMapView);

    MapLoadedListener getMapLoadedListener();

    MapClickListener getMapClickListener();

    MapOnScaleListener getMapOnScaleListener();

    MapCameraIdleListener getMapCameraIdleListener();

    MapMarkerClickListener getMapMarkerClickListener();

    MapInfoWindowClickListener getMapInfoWindowClickListener();

    boolean setLayerVisible(boolean visible);//图层是否显示

    void cameraToLayer();

    void onDestroy();//销毁
}
