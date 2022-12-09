package com.druid.mapbox.fence.polygon;

import android.content.Context;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByManualLocationLayer;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.List;

public class DrawFencePolygonByManualLocation extends DrawFencePolygon implements
        FenceDrawPolygonByManualLocationLayer<MapView, MapboxMap> {

    public DrawFencePolygonByManualLocation(Context context) {
        super(context);
    }

    @Override
    public void bindMap(MapView mapview_, MapboxMap map) {
        this.mapView = mapview_;
        this.mapboxMap = map;
        this.style = mapboxMap.getStyle();
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {

    }

    @Override
    public void mapReadyLoad(boolean reloaded) {
        this.style = mapboxMap.getStyle();
        createFenceSource(style);
        if (fenceDrawStatus()) {
            super.completeDrawFence(false);
        } else {
            updateDrawFence(false);
        }
    }

    @Override
    public MapCameraIdleListener getMapCameraIdleListener() {
        return this;
    }

    @Override
    public void mapCameraIdle() {
        if (mapboxMap != null) {
            CameraPosition cameraPosition = mapboxMap.getCameraPosition();
            if (cameraPosition != null) {
                //todo
            }
        }
    }

    @Override
    public void mapClick(LatLngBean latLngBean) {
        if (fenceDrawStatus()) {
            queryMarkerClick(latLngBean);
        }
    }

    @Override
    public void setMinFencePointSize(int size) {

    }

    @Override
    public boolean addFencePoint(LatLngBean latLngBean,boolean flatZoom) {
        if (!fenceDrawStatus()) {
            fencePoints.addFencePoint(latLngBean);
            updateDrawFence(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeFencePoint() {
        return false;
    }

    @Override
    public boolean addLocationPoint() {
        if (!fenceDrawStatus()) {
            LatLngBean latLng = getManualLocation();
            if (latLng != null) {
                fencePoints.addFencePoint(latLng);
                updateDrawFence(false);
                return true;
            }else {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean completeDrawFence() {
        return super.completeDrawFence(true);
    }

    @Override
    public boolean fenceDrawStatus() {
        return this.drawComplete;
    }

    @Override
    public boolean confirmEditorDrawFence() {
        return super.completeEditorFencePoint();
    }

    @Override
    public void resetDrawFence() {
        //todo
        fencePoints.clearFencePoints();
        this.drawComplete = false;
        this.reset();
    }

    @Override
    public List<LatLngBean> getFencePoints() {
        return fencePoints.getFencePointList();
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
        cameraToFenceBounds();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void locationChanged(LocationBean location) {
        if(mapboxMap!=null&&mapboxMap!=null) {
            if(mapboxMap.getStyle().isFullyLoaded()) {
                if (location != null) {
                    this.curLocation = location.position;
                }
            }
        }
    }

    @Override
    public MapLocationChangedListener getLocationChangedListener() {
        return this;
    }

    @Override
    public boolean mapMarkerClick(Object marker) {
        return false;
    }
}
