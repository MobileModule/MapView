package com.druid.mapgoogle.fence.polygon;

import android.content.Context;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByAutoLocationLayer;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class DrawFencePolygonByAutoLocation extends DrawFencePolygon implements
        FenceDrawPolygonByAutoLocationLayer<MapView, GoogleMap> {

    public DrawFencePolygonByAutoLocation(Context context) {
        super(context);
    }

    @Override
    public void bindMap(MapView mapview_, GoogleMap map) {
        this.mapView = mapview_;
        this.googleMap = map;
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
        if (googleMap != null) {
            CameraPosition cameraPosition = googleMap.getCameraPosition();
            if (cameraPosition != null) {
                //todo
            }
        }
    }

    @Override
    public void mapClick(LatLngBean latLngBean) {
        if (fenceDrawStatus()) {

        }
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
        if (googleMap != null && mapView != null) {
            if (location != null) {
                if (checkValidLocationPoint(location.position)) {
                    addFencePoint(location.position, false);
                }
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
