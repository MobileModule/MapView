package com.druid.mapgoogle.fence;

import android.content.Context;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.fence.api.FenceListLayerApi;
import com.druid.mapcore.interfaces.FenceClickListener;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.interfaces.PolygonClickListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import java.util.ArrayList;

public class DrawFenceList extends DrawFence implements FenceListLayerApi<MapView, GoogleMap> {
    public static final String TAG = DrawFenceList.class.getName();

    public DrawFenceList(Context context) {
        super(context);
    }

    @Override
    public void bindMap(MapView mapview_, GoogleMap map_) {
        this.mapView = mapview_;
        this.googleMap = map_;
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {

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
    public void mapClick(LatLngBean latLngBean) {

    }

    @Override
    public void mapReadyLoad(boolean reloaded) {

    }

    @Override
    public void onPolygonClick(Object object) {
        if (supportClick) {
            super.queryFenceClick(object);
        }
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

    public PolygonClickListener getPolygonClickListener(){
        return this;
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
}
