package com.druid.mapgaode.fence.polygon;

import android.content.Context;
import android.widget.RelativeLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Marker;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByHandLayer;
import com.druid.mapcore.fence.view.marker.FenceCenterMark;
import com.druid.mapcore.interfaces.FencePointMarkerClickListener;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapgaode.utils.LatLngUtils;

import java.util.List;

public class DrawFencePolygonHand extends DrawFencePolygon implements
        FenceDrawPolygonByHandLayer<MapView, AMap> {

    public DrawFencePolygonHand(Context context, RelativeLayout parentView) {
        super(context);
        addFenceCenterMarkerView(parentView);
    }

    @Override
    public void bindMap(MapView mapView, AMap map) {
        this.mapView = mapView;
        this.aMap = map;
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {

    }

    @Override
    public void mapReadyLoad(boolean reloaded) {
        createFenceSource();
        if (!reloaded) {
            if (centerMarkView != null) {
                centerMarkView.setCenterMarkerViewVisible(true);
            }
        } else {
            if (fenceDrawStatus()) {
                super.completeDrawFence(false);
            } else {
                updateDrawFence(false);
            }
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
                if (centerMarkView != null) {
                    centerMarkView.setCenterMarkerPosition(
                            LatLngUtils.mapLatLngParse(cameraPosition.target));
                }
            }
        }
    }

    @Override
    public void mapClick(LatLngBean latLngBean) {
        if (fenceDrawStatus()) {
            // todo
        }
    }

    @Override
    public void setMinFencePointSize(int size) {
        this.minSize = size;
    }

    @Override
    public boolean addFencePoint(LatLngBean latLngBean,boolean flatZoom) {
        if (!fenceDrawStatus()) {
            fencePoints.addFencePoint(latLngBean);
            updateDrawFence(false);
            if(flatZoom) {
                cameraFlatMoveToPoint(latLngBean);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean removeFencePoint() {
        if (this.drawComplete) {
            if (fencePoints.getFencePointList().size() >= minSize) {
                return super.removeFencePoint();
            }
        }
        return false;
    }

    @Override
    public boolean completeDrawFence() {
        if(centerMarkView!=null){
            centerMarkView.setClickEnabled(false);
        }
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
        if (centerMarkView != null) {
            centerMarkView.setCenterMarkerViewVisible(true);
            centerMarkView.setClickEnabled(true);
        }
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
        return this;
    }

    @Override
    public boolean mapMarkerClick(Marker marker) {
        if (fenceDrawStatus()) {
            queryMarkerClick(marker);
        }
        return false;
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

    }

    @Override
    public FenceCenterMark getFenceCenterMark() {
        return centerMarkView;
    }

    @Override
    public void setFencePointMarkerClickListener(FencePointMarkerClickListener listener) {
        super.setFencePointMarkerClickListener(listener);
    }
}
