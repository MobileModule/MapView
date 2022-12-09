package com.druid.mapbox.navigate;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.druid.mapbox.R;
import com.druid.mapbox.utils.LatLngUtils;
import com.druid.mapbox.utils.MapConstantUtils;
import com.druid.mapbox.utils.MapImageSettingUtils;
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
import com.druid.mapcore.utils.MapUtils;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public class DefineNavigateLayer extends NavigateLayer implements NavigateLayerApi<MapView, MapboxMap> {
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
    protected MapboxMap mapboxMap = null;
    protected Style style = null;

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
        style.addImage(MapConstantUtils.FIELD_NAVIGATE_END_IMAGE, this.endMarker);
        createLineSource(style);
        createFencePointSource(style);
        setLocationFollow(locationFollow);
        drawPoint();
        locationChanged(preLocation);
    }

    private GeoJsonSource lineSource;
    private LineLayer lineLayer;

    private void createLineSource(Style style) {
        lineSource = new GeoJsonSource(MapConstantUtils.SOURCE_NAVIGATE_LINE_ID);
        style.addSource(lineSource);
        style.addLayer(createLineLayer());
    }

    private int lineColor = MapImageSettingUtils.getFencePolylineBorderColor(context);

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
                    .target(mapboxMap.getCameraPosition().target)
                    .tilt(45)
                    .zoom(mapboxMap.getCameraPosition().zoom)
                    .bearing(0)
                    .build();
        } else {
            animatedPosition = new CameraPosition.Builder()
                    .target(mapboxMap.getCameraPosition().target)
                    .tilt(0)
                    .zoom(mapboxMap.getCameraPosition().zoom)
                    .bearing(0)
                    .build();
        }
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(animatedPosition));
    }

    private MapLocationChangedListener locationChangedListener;

    @Override
    public void setLocationChangedListener(MapLocationChangedListener listener) {
        this.locationChangedListener = listener;
    }

    private LineLayer createLineLayer() {
        lineLayer = new LineLayer(MapConstantUtils.LAYER_NAVIGATE_LINE_ID,
                MapConstantUtils.SOURCE_NAVIGATE_LINE_ID);
        lineLayer.withProperties(
                lineColor(get(MapConstantUtils.FIELD_COLOR)),
                lineOpacity(1f),
                lineWidth(MapImageSettingUtils.FENCE_LINE_WIDTH),
                lineCap(LINE_CAP_ROUND),
                lineJoin(LINE_JOIN_ROUND)
        );
        lineLayer.withProperties(visibility(Property.VISIBLE));
        return lineLayer;
    }

    private GeoJsonSource pointSource;
    private SymbolLayer pointLayer;

    private void createFencePointSource(Style style) {
        pointSource = new GeoJsonSource(MapConstantUtils.SOURCE_FENCE_POLYGON_POINT_ID);
        style.addSource(pointSource);
        style.addLayer(createFencePointLayer());
    }

    private SymbolLayer createFencePointLayer() {
        pointLayer = new SymbolLayer(MapConstantUtils.LAYER_FENCE_POLYGON_POINT_ID,
                MapConstantUtils.SOURCE_FENCE_POLYGON_POINT_ID);
        pointLayer.withProperties(
                iconImage(MapConstantUtils.FIELD_NAVIGATE_END_IMAGE),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, MapImageSettingUtils.getFenceMarkerOffsetY(context, true)}),
                iconIgnorePlacement(true)
        );
        pointLayer.withProperties(visibility(Property.VISIBLE));
        return pointLayer;
    }

    private LocationBean preLocation;

    @Override
    public void locationChanged(LocationBean location) {
        if (location != null) {
            preLocation = location;
            if (mapboxMap != null && mapboxMap.getStyle().isFullyLoaded()) {
                if (point != null) {
                    if (locationChangedListener != null) {
                        locationChangedListener.locationChanged(location);
                    }
                    drawLine(location);
                    if (locationFollow) {
                        CameraPosition animatedPosition = new CameraPosition.Builder()
                                .target(LatLngUtils.mapLatLngFrom(location.position))
                                .tilt(45)
                                .zoom(mapboxMap.getCameraPosition().zoom)
                                .bearing(0)
                                .build();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(animatedPosition));
                    }
                }
            }
        }
    }

    private LatLngBounds lineLayerLatLngBounds = null;
    protected Drawable endMarker = null;

    private void drawLine(LocationBean location) {
        ArrayList<Feature> featuresLine = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        double lat = point.getLat();
        double lng = point.getLng();
        if (MapUtils.validLatLng(lat, lng)) {
            JsonObject properties = new JsonObject();
            properties.addProperty(MapConstantUtils.FIELD_COLOR,
                    ColorUtils.colorToRgbaString(lineColor));
            Point point_use = Point.fromLngLat(lng, lat);
            builder.include(new LatLng(lat, lng));
            Point point_location = Point.fromLngLat(location.position.getLng(), location.position.getLat());
            builder.include(new LatLng(point_location.latitude(), point_location.longitude()));
            List<Point> linePoints = new ArrayList<>();
            linePoints.add(point_location);
            linePoints.add(point_use);
            Feature featureLine = Feature.fromGeometry(
                    LineString.fromLngLats(linePoints), properties
            );
            featuresLine.add(featureLine);
        }
        lineLayerLatLngBounds = builder.build();
        FeatureCollection featureLineCollection = FeatureCollection.fromFeatures(featuresLine);
        lineSource.setGeoJson(featureLineCollection);
    }

    private void drawPoint() {
        if (point != null) {
            List<Feature> featuresPoint = new ArrayList<>();
            JsonObject properties = new JsonObject();
            properties.addProperty(MapConstantUtils.FIELD_IMAGE, "icon_navigate_map_point");
            Point point_use = Point.fromLngLat(point.getLng(), point.getLat());
            Feature featurePoint = Feature.fromGeometry(point_use);
            featuresPoint.add(featurePoint);
            FeatureCollection featurePointCollection = FeatureCollection.fromFeatures(featuresPoint);
            pointSource.setGeoJson(featurePointCollection);
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
