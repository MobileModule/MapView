package com.druid.mapgaode.circle;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.bean.FenceType;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.circle.CircleDrawLayer;
import com.druid.mapcore.circle.CircleDrawLayerApi;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.setting.MapSetting;
import com.druid.mapcore.utils.MapUtils;
import com.druid.mapcore.utils.turf.TurfConstantsUtils;
import com.druid.mapcore.utils.turf.TurfTransformationUtils;
import com.druid.mapgaode.utils.MapImageSettingUtils;

import java.util.List;

public class DefineCircleLayer extends CircleDrawLayer implements CircleDrawLayerApi<MapView, AMap> {
    public static final String TAG = DefineCircleLayer.class.getName();

    public DefineCircleLayer(Context context) {
        super(context);
    }

    protected MapView mapView = null;
    protected AMap aMap = null;
    protected  DruidMapView druidMapView=null;

    @Override
    public void bindMap(MapView mapview_, AMap map_) {
        this.mapView = mapview_;
        this.aMap = map_;
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {

    }

    @Override
    public void mapReadyLoad(boolean reloaded) {

    }

    FenceCoreBean fence;

    @Override
    public void setFenceCircle(FenceCoreBean fence_) {
        this.fence = fence_;
        if(circle!=null){
            circle.remove();
            circle=null;
        }
        drawFencePolygonLayer(true);
    }

    private Circle circle;
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
                        circle = aMap.addCircle(circleOptions);
                    }
                }
            }
            if (needZoom) {
                zoomCenterFence(fence);
            }
        }
    }

    private void zoomCenterFence(FenceCoreBean fence) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds latLngBounds = null;
        if (fence.type.equals(FenceType.Round)) {
            LatLngBean center = new LatLngBean(fence.points.get(0)[0], fence.points.get(0)[1]);
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
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100);//OSUtils.getScreenWidth(context) / 4);
            aMap.animateCamera(cameraUpdate);
        }
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
        return false;
    }

    @Override
    public void cameraToLayer() {

    }

    @Override
    public void onDestroy() {

    }
}
