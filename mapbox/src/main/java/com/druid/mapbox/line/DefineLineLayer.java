package com.druid.mapbox.line;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.druid.mapbox.R;
import com.druid.mapbox.utils.MapConstantUtils;
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
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;

public class DefineLineLayer extends MapBoxLineLayer implements LineDrawLayerApi<MapView, MapboxMap> {

    public DefineLineLayer(Context context) {
        super(context);
        this.arrowDrawable = context.getDrawable(R.drawable.line_arrow);
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
        style.addImage(MapConstantUtils.FIELD_ARROW_IMAGE, this.arrowDrawable);
        style.addImage(MapConstantUtils.FIELD_MOVE_POSITION, context.getDrawable(R.drawable.icon_move_position));
        createMapLayer();
        if (source.size() > 0) {
            setLineSource(source);
        }
    }

    @Override
    public void setLineSource(ArrayList<LatLngBean> points) {
        this.source = points;
        clearPlayTracker();
        drawLine();
        drawPoint();
        drawArrowPoint();
        setRouteData();
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
