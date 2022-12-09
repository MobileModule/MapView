package com.druid.mapgoogle.fence.circle;

import android.content.Context;
import android.graphics.Point;
import android.widget.RelativeLayout;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.bean.FenceType;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;
import com.druid.mapcore.fence.api.circle.FenceDrawCircleLayerApi;
import com.druid.mapcore.fence.layer.circle.FenceDrawCircleLayer;
import com.druid.mapcore.fence.view.circle.CircleView;
import com.druid.mapcore.interfaces.FenceCircleAreaChangedListener;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.setting.MapSetting;
import com.druid.mapcore.utils.DimenUtils;
import com.druid.mapcore.utils.MapUtils;
import com.druid.mapcore.utils.OSUtils;
import com.druid.mapcore.utils.turf.TurfConstantsUtils;
import com.druid.mapcore.utils.turf.TurfTransformationUtils;
import com.druid.mapgoogle.R;
import com.druid.mapgoogle.utils.LatLngUtils;
import com.druid.mapgoogle.utils.MapConstantUtils;
import com.druid.mapgoogle.utils.MapImageSettingUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class DrawFenceCircle extends FenceDrawCircleLayer implements
        FenceDrawCircleLayerApi<MapView, GoogleMap> {

    protected MapView mapView = null;
    protected GoogleMap googleMap = null;


    public DrawFenceCircle(Context context, RelativeLayout parentView) {
        super(context);
        addFenceCircleView(parentView);
    }

    @Override
    public CircleView getCircleView() {
        return circleView;
    }

    @Override
    public boolean completeDrawCircle() {
        return false;
    }

    private FenceCircleAreaChangedListener areaChangedListener;

    @Override
    public void setAreaChangedListener(FenceCircleAreaChangedListener listener) {
        this.areaChangedListener = listener;
    }

    FenceCoreBean fence;

    @Override
    public void setFenceCircle(FenceCoreBean fence) {
        this.fence = fence;
        drawFencePolygonLayer(true);
    }

    private void drawFencePolygonLayer(boolean needZoom) {
        if (fence != null) {
            if (fence.type.equals(FenceType.Round)) {
                if (fence.points.size() == 1) {
                    double lat = fence.points.get(0)[0];
                    double lng = fence.points.get(0)[1];
                    if (MapUtils.validLatLng(lat, lng)) {
                        int radius = fence.distance;
                        LatLng center = new LatLng(fence.points.get(0)[0], fence.points.get(0)[1]);
                        CircleOptions circleOptions = new CircleOptions();
                        circleOptions.radius(radius);
                        circleOptions.center(center);
                        circleOptions.zIndex(MapSetting.ZINDEX_GEOMETRY);
                        circleOptions.fillColor(MapImageSettingUtils.getFencePolygonFillColor(context));
                        circleOptions.strokeWidth(MapImageSettingUtils.FENCE_GOOGLE_LINE_WIDTH);
                        circleOptions.strokeColor(MapImageSettingUtils.getFencePolygonBorderColor(context));
                        Circle circle = googleMap.addCircle(circleOptions);
                        if (needZoom) {
                            zoomCenterFence(fence);
                        }
                    }
                }
            }

        }
    }

    private void createCircleCenterMarkerSource() {

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
        createCircleCenterMarkerSource();
        if (reloaded) {
            updateCenterCircleMarker();
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
                callBackAreaChanged();
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
        return false;
    }

    void callBackAreaChanged() {
        updateCenterCircleMarker();
        if (areaChangedListener != null) {
            LatLng centerLatLng = googleMap.getCameraPosition().target;
            LatLngBean centerDruidLatLng = LatLngUtils.mapLatLngParse(centerLatLng);
            double lengthR = getCircleRadiusR(centerLatLng);
            areaChangedListener.fenceCircleAreaChanged(lengthR, centerDruidLatLng);
        }
    }

    private double getCircleRadiusR(LatLng centerLatLng) {
        int radius = DimenUtils.getScreenWidth(context) / 4;
        Point centerPoint = googleMap.getProjection().toScreenLocation(centerLatLng);
        Point leftPoint = new Point(centerPoint.x - radius, centerPoint.y);
        LatLng leftLaLng = googleMap.getProjection().fromScreenLocation(leftPoint);
        LatLngBean centerDruidLatLng = LatLngUtils.mapLatLngParse(centerLatLng);
        LatLngBean leftDruidLatLng = LatLngUtils.mapLatLngParse(leftLaLng);
        double lengthR = MapUtils.computeDistanceBetween(centerDruidLatLng, leftDruidLatLng);
        return lengthR;
    }

    private void updateCenterCircleMarker() {
        LatLng centerLatLng = googleMap.getCameraPosition().target;
        addCenterMaker(centerLatLng);
    }

    private Marker centerMarker = null;

    private void addCenterMaker(LatLng latLng) {
        if (centerMarker != null) {
            centerMarker.remove();
        }
        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.fence_circle_center_marker))
                .position(latLng)
                .draggable(false)
                .zIndex(MapConstantUtils.ZINDEX_MARKER);
        centerMarker = googleMap.addMarker(markerOption);
    }

    private void zoomCenterFence(FenceCoreBean fence) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds latLngBounds = null;
        if (fence.type.equals(FenceType.Round)) {
            LatLngBean center = new LatLngBean(0,fence.points.get(0)[0], fence.points.get(0)[1]);
            List<List<LatLngBean>> polygon = TurfTransformationUtils.circle(center, fence.distance, TurfConstantsUtils.UNIT_METERS);
            if (polygon != null) {
                for (List<LatLngBean> points : polygon) {
                    for (LatLngBean point : points) {
                        LatLng latLng = new LatLng(point.getLat(), point.getLng());
                        builder.include(latLng);
                    }
                }
                latLngBounds = builder.build();
            }
        }
        if (latLngBounds != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, OSUtils.getScreenWidth(context) / 4);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onScaleEnd() {
        callBackAreaChanged();
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
