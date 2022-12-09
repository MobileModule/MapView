package com.druid.mapbox.fence;

import android.content.Context;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.bean.FenceType;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.fence.api.FenceListLayerApi;
import com.druid.mapcore.interfaces.FenceClickListener;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;

public class DrawFenceList extends DrawFence implements FenceListLayerApi<MapView, MapboxMap> {
    public static final String TAG = DrawFenceList.class.getName();

    protected Style style = null;

    public DrawFenceList(Context context) {
        super(context);
    }

    @Override
    public void bindMap(MapView mapview_, MapboxMap map_) {
        this.mapView = mapview_;
        this.mapboxMap = map_;
        this.style = mapboxMap.getStyle();
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {

    }

    @Override
    public void mapReadyLoad(boolean reloaded) {
        this.style = mapboxMap.getStyle();
        createFenceSource(style);
    }

    @Override
    public void setFenceSource(ArrayList<FenceCoreBean> fenceSource, boolean supportClick) {
        this.source = fenceSource;
        this.supportClick = supportClick;
        drawFence();
    }

    @Override
    public void clearFenceSelected() {
        super.clearFenceSelected();
    }

    @Override
    public void setFenceClickListener(FenceClickListener listener) {
        super.setFenceClickListener(listener);
    }

    @Override
    public void setFenceSelected(String fence_id) {
        super.setFenceSelected(fence_id);
    }

    @Override
    public String getSelectedFenceId() {
        return super.getSelectedFenceId();
    }

    @Override
    public MapLoadedListener getMapLoadedListener() {
        return this;
    }

    @Override
    public MapClickListener getMapClickListener() {
        return this;
    }

    @Override
    public MapOnScaleListener getMapOnScaleListener() {
        return null;
    }

    @Override
    public MapCameraIdleListener getMapCameraIdleListener() {
        return null;
    }

    @Override
    public MapMarkerClickListener getMapMarkerClickListener() {
        return null;
    }

    @Override
    public MapInfoWindowClickListener getMapInfoWindowClickListener() {
        return null;
    }

    @Override
    public boolean setLayerVisible(boolean visible) {
        return false;
    }

    @Override
    public void cameraToLayer() {
        zoomLayer();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void mapClick(LatLngBean latLngBean) {
        if(supportClick) {
            super.queryFenceClick(latLngBean);
        }
    }

    @Override
    public void onPolygonClick(Object object) {

    }
}
