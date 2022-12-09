package com.druid.mapgoogle.fence.rectangle;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.widget.RelativeLayout;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;
import com.druid.mapcore.fence.api.rectangle.FenceDrawRectangleLayerApi;
import com.druid.mapcore.fence.layer.rectangle.FenceDrawRectangleLayer;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapgoogle.utils.LatLngUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class DrawFenceRectangle extends FenceDrawRectangleLayer implements
        FenceDrawRectangleLayerApi<MapView, GoogleMap> {
    public static final String TAG = DrawFenceRectangle.class.getName();

    protected MapView mapView = null;
    protected GoogleMap googleMap = null;

    public DrawFenceRectangle(Context context, RelativeLayout parentView) {
        super(context);
        addFenceRectangleView(parentView);
    }

    @Override
    public void confirmRectangleCropView() {
        setFenceRectangleCropViewGone();
    }

    @Override
    public void resetRectangleCropView() {
        setFenceRectangleCropViewVisible();
    }

    @Override
    public List<LatLngBean> confirmDrawFenceRectangle() {
        return getFenceRectangle();
    }

    protected List<LatLngBean> getFenceRectangle() {
        List<LatLngBean> fencePoints = new ArrayList<>();
        if(canDrawRectangleFence) {
            LatLng up_left_latlng = googleMap.getProjection().fromScreenLocation(new Point(up_left));
            fencePoints.add(LatLngUtils.mapLatLngParse(up_left_latlng));
            LatLng up_right_latlng = googleMap.getProjection().fromScreenLocation(new Point(up_right));
            fencePoints.add(LatLngUtils.mapLatLngParse(up_right_latlng));
            LatLng bottom_right_latlng = googleMap.getProjection().fromScreenLocation(new Point(bottom_right));
            fencePoints.add(LatLngUtils.mapLatLngParse(bottom_right_latlng));
            LatLng bottom_left_latlng = googleMap.getProjection().fromScreenLocation(new Point(bottom_left));
            fencePoints.add(LatLngUtils.mapLatLngParse(bottom_left_latlng));
        }
        return fencePoints;
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
    public void mapReadyLoad(boolean reloaded) {
        //todo
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

    }


    @Override
    public void locationChanged(LocationBean location) {

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

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public boolean mapMarkerClick(Object marker) {
        return false;
    }
}
