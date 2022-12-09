package com.druid.mapbox.capture;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.MarkerBean;
import com.druid.mapcore.capture.CaptureDrawLayerApi;
import com.druid.mapcore.interfaces.CaptureClickListener;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.utils.MapSettingConstantUtils;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;

public class DefineCaptureLayer extends MapBoxCaptureLayer implements CaptureDrawLayerApi<MapView, MapboxMap> {
    public DefineCaptureLayer(Context context) {
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
        //
        createMapLayer();
        if (markers.size() > 0) {
            setLineSource(source);
            setMarkerSource(markers);
        }
    }

    @Override
    public void setLineSource(ArrayList<LatLngBean> points) {
        this.source=points;
        drawLine();
    }

    @Override
    public void setMarkerSource(ArrayList<MarkerBean> markers) {
        this.markers = markers;
        drawPoint();
    }

    @Override
    public void setCaptureClickListener(CaptureClickListener listener) {
        super.setCaptureClickListener(listener);
    }

    @Override
    public CaptureDrawLayerApi setLineColor(int color, Drawable captureDrawable) {
        this.lineColor = color;
        if (captureDrawable != null) {
            this.captureDrawable = captureDrawable;
        }
        return this;
    }


    @Override
    public void mapClick(LatLngBean latLngBean) {
        super.queryCaptureClick(latLngBean);
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
        return super.setLayerVisible(visible);
    }

    @Override
    public void cameraToLayer() {
        if (lineLayerLatLngBounds != null) {
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(lineLayerLatLngBounds, MapSettingConstantUtils.MAP_BOUNDS_PADDING));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroyLayer();
    }
}
