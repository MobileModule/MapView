package com.druid.mapbox.circle;

import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOutlineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.content.Context;

import com.druid.mapbox.utils.MapConstantUtils;
import com.druid.mapbox.utils.MapImageSettingUtils;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.bean.FenceType;
import com.druid.mapcore.circle.CircleDrawLayer;
import com.druid.mapcore.circle.CircleDrawLayerApi;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.utils.MapUtils;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfTransformation;

import java.util.ArrayList;
import java.util.List;

public class DefineCircleLayer extends CircleDrawLayer implements
        CircleDrawLayerApi<MapView, MapboxMap> {
    public static final String TAG = DefineCircleLayer.class.getName();

    public DefineCircleLayer(Context context) {
        super(context);
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
        createFillSource(style);
        createFillLineSource(style);
    }

    protected GeoJsonSource fillSource = null;
    protected FillLayer polygonLayer;

    private void createFillSource(Style style) {
        fillSource = new GeoJsonSource(MapConstantUtils.SOURCE_POLYGON_FILL_ID);
        style.addSource(fillSource);
        style.addLayer(createFillLayer());
    }

    private FillLayer createFillLayer() {
        polygonLayer = new FillLayer(MapConstantUtils.LAYER_POLYGON_FILL_ID,
                MapConstantUtils.SOURCE_POLYGON_FILL_ID);
        polygonLayer.withProperties(fillColor(MapImageSettingUtils.getFencePolygonFillColor(context)),
                fillOutlineColor(MapImageSettingUtils.getFencePolygonBorderColor(context)));
        return polygonLayer;
    }

    protected GeoJsonSource fillLineSource;
    protected LineLayer fillLineLayer;

    private void createFillLineSource(Style style) {
        fillLineSource = new GeoJsonSource(MapConstantUtils.SOURCE_POLYGON_FILL_LINE_ID);
        style.addSource(fillLineSource);
        style.addLayerBelow(createFillLineLayer(), MapConstantUtils.LAYER_FENCE_POLYGON_POINT_ID);
    }

    private LineLayer createFillLineLayer() {
        fillLineLayer = new LineLayer(MapConstantUtils.LAYER_POLYGON_FILL_LINE_ID,
                MapConstantUtils.SOURCE_POLYGON_FILL_LINE_ID);
        fillLineLayer.withProperties(
                lineColor(MapImageSettingUtils.getFencePolylineBorderColor(context)),
                lineOpacity(1f),
                lineWidth(MapImageSettingUtils.FENCE_LINE_WIDTH),
                lineCap(LINE_CAP_ROUND),
                lineJoin(LINE_JOIN_ROUND)
        );
        fillLineLayer.withProperties(visibility(Property.VISIBLE));
        return fillLineLayer;
    }

    FenceCoreBean fence;

    @Override
    public void setFenceCircle(FenceCoreBean fence_) {
        this.fence = fence_;
        drawFencePolygonLayer(true);
    }

    private void drawFencePolygonLayer(boolean needZoom) {
        if (fence != null) {
            ArrayList<Feature> featuresPolygon = new ArrayList<>();
            ArrayList<Feature> featuresLine = new ArrayList<>();
            Polygon polygon = null;
            if (fence.type.equals(FenceType.Round)) {
                if (fence.points.size() == 1) {
                    double lat = fence.points.get(0)[0];
                    double lng = fence.points.get(0)[1];
                    if (MapUtils.validLatLng(lat, lng)) {
                        polygon = getCircleGeometry(Point.fromLngLat(lng, lat), fence.distance);
                        if (polygon != null) {
                            for (List<Point> linePoints : polygon.coordinates()) {
                                Feature featureLine = Feature.fromGeometry(
                                        LineString.fromLngLats(linePoints)
                                );
                                featuresLine.add(featureLine);
                            }
                        }
                    }
                }
            }
            if (polygon != null) {
                featuresPolygon.add(Feature.fromGeometry(polygon));
                FeatureCollection featurePointCollection = FeatureCollection.fromFeatures(featuresPolygon);
                fillSource.setGeoJson(featurePointCollection);
                if (needZoom) {
                    zoomCenterFence(fence);
                }
            }
            if(featuresLine!=null){
                fillLineSource.setGeoJson(FeatureCollection.fromFeatures(featuresLine));
            }
        }
    }

    private Polygon getCircleGeometry(Point point, double radius) {
        Polygon polygon = TurfTransformation.circle(point, radius, TurfConstants.UNIT_METERS);
        return polygon;
    }

    private void zoomCenterFence(FenceCoreBean fence) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds latLngBounds = null;
        if (fence.type.equals(FenceType.Round)) {
            LatLng center = new LatLng(fence.points.get(0)[0], fence.points.get(0)[1]);
            Polygon polygon = getCircleGeometry(Point.fromLngLat(center.getLongitude(), center.getLatitude()), fence.distance);
            if (polygon != null) {
                for (List<Point> points : polygon.coordinates()) {
                    for (Point point : points) {
                        LatLng latLng = new LatLng(point.latitude(), point.longitude());
                        builder.include(latLng);
                    }
                }
                latLngBounds = builder.build();
            }
        }
        if (latLngBounds != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100);//OSUtils.getScreenWidth(context) / 4);
            mapboxMap.animateCamera(cameraUpdate);
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
