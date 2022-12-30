package com.druid.mapgoogle.line;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.interfaces.LineTrackMoveListener;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.line.LineDrawLayerApi;
import com.druid.mapcore.utils.MapSettingConstantUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;

import java.util.ArrayList;

public class DefineLineLayer extends GoogleLineLayer implements LineDrawLayerApi<MapView, GoogleMap> {
    public DefineLineLayer(Context context) {
        super(context);
    }

    @Override
    public void bindMap(MapView mapview_, GoogleMap map_) {
        this.mapView = mapview_;
        this.googleMap = map_;
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {
        this.druidMapView=druidMapView;
    }

    @Override
    public void mapReadyLoad(boolean reloaded) {

    }

    @Override
    public void setLineSource(ArrayList<LatLngBean> points) {
        this.source = points;
        createMapLayer();
    }

    @Override
    public LineDrawLayerApi setLineColor(int color, Drawable arrowDrawable) {
        this.lineColor = color;
        if (arrowDrawable != null) {
            this.arrowDrawable = arrowDrawable;
        }
        return this;
    }


    @Override
    public void markStartEndPosition(Drawable startDrawable, Drawable endDrawable) {
        this.startDrawable = startDrawable;
        this.endDrawable = endDrawable;
    }

    @Override
    public void mapCameraIdle() {
        drawArrowPoint();
    }

    @Override
    public void openArrowLine() {

    }

    @Override
    public boolean playTracker(boolean play) {
        return super.playTracker(play);
    }

    @Override
    public void clearPlayTracker() {
        super.clearPlayTracker();
    }

    @Override
    public void setPlayTrackListener(LineTrackMoveListener listener) {
        super.setPlayTrackListener(listener);
    }

    @Override
    public MapLoadedListener getMapLoadedListener() {
        return this;
    }

    @Override
    public MapClickListener getMapClickListener() {
        return null;
    }

    @Override
    public MapOnScaleListener getMapOnScaleListener() {
        return null;
    }

    @Override
    public MapCameraIdleListener getMapCameraIdleListener() {
        return this;
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
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(lineLayerLatLngBounds, MapSettingConstantUtils.MAP_BOUNDS_PADDING));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroyLayer();
    }
}
