package com.druid.mapgaode.navigate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.druid.mapbaidu.R;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.navigate.NavigateLayer;
import com.druid.mapcore.navigate.NavigateLayerApi;
import com.druid.mapcore.utils.MapBitmapUtils;
import com.druid.mapcore.utils.MapUtils;
import com.druid.mapgaode.utils.LatLngUtils;
import com.druid.mapgaode.utils.MapConstantUtils;
import com.druid.mapgaode.utils.MapImageSettingUtils;

import java.util.ArrayList;

public class DefineNavigateLayer extends NavigateLayer implements NavigateLayerApi<MapView, AMap> {
    public static final String TAG = DefineNavigateLayer.class.getName();

    public DefineNavigateLayer(Context context) {
        super(context);
        this.endMarker = context.getDrawable(R.drawable.icon_navigate_map_point);
    }

    private LatLngBean point;

    @Override
    public void setLineSource(LatLngBean point) {
        this.point = point;
    }

    protected MapView mapView = null;
    protected AMap aMap = null;

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
        setLocationFollow(locationFollow);
        drawPoint();
        locationChanged(preLocation);
    }

    private int lineColor = MapImageSettingUtils.getFencePolylineBorderColor(context);
    protected Drawable endMarker = null;

    @Override
    public NavigateLayerApi setLineColor(int color, Drawable endMarker) {
        this.lineColor = color;
        this.endMarker = endMarker;
        return this;
    }

    private boolean locationFollow = true;

    @Override
    public void setLocationFollow(boolean follow) {
        this.locationFollow = follow;
        CameraPosition animatedPosition = null;
        if (locationFollow) {
            animatedPosition = new CameraPosition.Builder()
                    .target(aMap.getCameraPosition().target)
                    .tilt(45)
                    .zoom(aMap.getCameraPosition().zoom)
                    .bearing(0)
                    .build();
        } else {
            animatedPosition = new CameraPosition.Builder()
                    .target(aMap.getCameraPosition().target)
                    .tilt(0)
                    .zoom(aMap.getCameraPosition().zoom)
                    .bearing(0)
                    .build();
        }
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(animatedPosition));
    }

    private LatLngBounds lineLayerLatLngBounds = null;

    Polyline trackerLine;

    private void drawLine(LocationBean location) {
        ArrayList<LatLng> linePoints = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        double lat = point.getLat();
        double lng = point.getLng();
        if (MapUtils.validLatLng(lat, lng)) {
            LatLng point_use = new LatLng(lng, lat);
            builder.include(point_use);
            LatLng point_location = new LatLng(location.position.getLng(), location.position.getLat());
            builder.include(point_location);
            //
            linePoints.add(point_location);
            linePoints.add(point_use);
        }
        if (linePoints.size() > 1) {
            lineLayerLatLngBounds = builder.build();
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.width(MapImageSettingUtils.FENCE_LINE_WIDTH * 2.5f);
            polylineOptions.color(lineColor);
            polylineOptions.addAll(linePoints);
            polylineOptions.zIndex(MapImageSettingUtils.ZINDEX_GEOMETRY);
            trackerLine = aMap.addPolyline(polylineOptions);
        }
    }

    private void drawPoint() {
        if (point != null) {
            Bitmap bitmap = MapBitmapUtils.drawableToBitmap(endMarker);
            if (bitmap != null) {
                String title = "marker-" + 0;
                LatLng point_start = new LatLng(point.getLat(), point.getLng());
                BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                MarkerOptions markerOption = new MarkerOptions().icon(descriptor)
                        .position(point_start)
                        .title(title)
                        .anchor(0.5f, 0.5f)
                        .draggable(false)
                        .zIndex(MapConstantUtils.ZINDEX_MARKER);
                Marker marker = aMap.addMarker(markerOption);
            }
        }
    }

    private MapLocationChangedListener locationChangedListener;

    @Override
    public void setLocationChangedListener(MapLocationChangedListener listener) {
        this.locationChangedListener = listener;
    }


    private LocationBean preLocation;

    @Override
    public void locationChanged(LocationBean location) {
        if (location != null) {
            preLocation = location;
            if (aMap != null) {
                if (point != null) {
                    if (locationChangedListener != null) {
                        locationChangedListener.locationChanged(location);
                    }
                    drawLine(location);
                    if (locationFollow) {
                        CameraPosition animatedPosition = new CameraPosition.Builder()
                                .target(LatLngUtils.mapLatLngFrom(location.position))
                                .tilt(45)
                                .zoom(aMap.getCameraPosition().zoom)
                                .bearing(0)
                                .build();
                        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(animatedPosition));
                    }
                }
            }
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
    public MapLocationChangedListener getLocationChangedListener() {
        return this;
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
