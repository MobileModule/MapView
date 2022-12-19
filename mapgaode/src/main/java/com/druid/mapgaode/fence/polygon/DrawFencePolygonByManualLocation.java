package com.druid.mapgaode.fence.polygon;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Marker;
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

import java.util.List;

public class DrawFencePolygonByManualLocation extends DrawFencePolygon implements
        FenceDrawPolygonByManualLocationLayer<MapView, AMap> {

    public DrawFencePolygonByManualLocation(Context context) {
        super(context);
    }

    @Override
    public void bindMap(MapView mapview_, AMap map) {
        this.mapView = mapview_;
        this.aMap = map;
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {

    }

    @Override
    public void mapReadyLoad(boolean reloaded) {
        createFenceSource();
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
        if (aMap != null) {
            CameraPosition cameraPosition = aMap.getCameraPosition();
            if (cameraPosition != null) {
                //todo
            }
        }
    }

    @Override
    public void mapClick(LatLngBean latLngBean) {

    }

    @Override
    public void setMinFencePointSize(int size) {

    }

    @Override
    public boolean addFencePoint(LatLngBean latLngBean, boolean flatZoom) {
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
            } else {
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
        if (aMap != null && mapView != null) {
            if (location != null) {
                this.curLocation = location.position;
            }
        }
    }

    @Override
    public MapLocationChangedListener getLocationChangedListener() {
        return this;
    }

    @Override
    public boolean mapMarkerClick(Marker marker) {
        if (fenceDrawStatus()) {
            queryMarkerClick(marker);
        }
        return false;
    }
}
