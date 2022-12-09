package com.druid.mapbox.fence.circle;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOutlineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.content.Context;
import android.graphics.PointF;
import android.widget.RelativeLayout;

import com.druid.mapbox.R;
import com.druid.mapbox.utils.LatLngUtils;
import com.druid.mapbox.utils.MapConstantUtils;
import com.druid.mapbox.utils.MapImageSettingUtils;
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
import com.druid.mapcore.utils.DimenUtils;
import com.druid.mapcore.utils.MapUtils;
import com.druid.mapcore.utils.OSUtils;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfTransformation;

import java.util.ArrayList;
import java.util.List;

public class DrawFenceCircle extends FenceDrawCircleLayer implements
        FenceDrawCircleLayerApi<MapView, MapboxMap> {

    protected MapView mapView = null;
    protected MapboxMap mapboxMap = null;
    protected Style style = null;

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

    private GeoJsonSource fenceCircleSource = null;
    private CircleLayer circleLayer = null;

    private void createCircleCenterMarkerSource(Style style) {
        fenceCircleSource = new GeoJsonSource(MapConstantUtils.SOURCE_FENCE_CIRCLE_CENTER_ID);
        style.addSource(fenceCircleSource);
        style.addLayer(getCircleLayer());
    }

    private CircleLayer getCircleLayer() {
        circleLayer = new CircleLayer(MapConstantUtils.LAYER_FENCE_CIRCLE_CENTER_ID,
                MapConstantUtils.SOURCE_FENCE_CIRCLE_CENTER_ID);
        circleLayer.withProperties(
                circleColor(context.getResources().getColor(R.color.white)),
                circleStrokeColor(context.getResources().getColor(R.color.fence_polyline)),
                circleStrokeWidth(2f),
                circleRadius(3f)
        );
        circleLayer.withProperties(visibility(Property.VISIBLE));
        return circleLayer;
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
        createCircleCenterMarkerSource(style);
        createPolygonSource(style);
        createFillLineSource(style);
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
        if (mapboxMap != null) {
            CameraPosition cameraPosition = mapboxMap.getCameraPosition();
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
            LatLng centerLatLng = mapboxMap.getCameraPosition().target;
            LatLngBean centerDruidLatLng = LatLngUtils.mapLatLngParse(centerLatLng);
            double lengthR = getCircleRadiusR(centerLatLng);
            areaChangedListener.fenceCircleAreaChanged(lengthR, centerDruidLatLng);
        }
    }

    private double getCircleRadiusR(LatLng centerLatLng) {
        int radius = DimenUtils.getScreenWidth(context) / 4;
        PointF centerPoint = mapboxMap.getProjection().toScreenLocation(centerLatLng);
        PointF leftPoint = new PointF(centerPoint.x - radius, centerPoint.y);
        LatLng leftLaLng = mapboxMap.getProjection().fromScreenLocation(leftPoint);
        LatLngBean centerDruidLatLng = LatLngUtils.mapLatLngParse(centerLatLng);
        LatLngBean leftDruidLatLng = LatLngUtils.mapLatLngParse(leftLaLng);
        double lengthR = MapUtils.computeDistanceBetween(centerDruidLatLng, leftDruidLatLng);
        return lengthR;
    }

    private void updateCenterCircleMarker() {
        LatLng centerLatLng = mapboxMap.getCameraPosition().target;
        JsonObject properties = new JsonObject();
        com.mapbox.geojson.Point point_use = Point.fromLngLat(centerLatLng.getLongitude(),
                centerLatLng.getLatitude());
        Feature feature = Feature.fromGeometry(point_use, properties);
        fenceCircleSource.setGeoJson(feature);
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

    private GeoJsonSource fencePolygonSource = null;
    private FillLayer polygonLayer;

    private void createPolygonSource(Style style) {
        fencePolygonSource = new GeoJsonSource(MapConstantUtils.SOURCE_FENCE_POLYGON_ID);
        style.addSource(fencePolygonSource);
        style.addLayer(createPolygonLayer());
    }

    private FillLayer createPolygonLayer() {
        polygonLayer = new FillLayer(MapConstantUtils.LAYER_FENCE_POLYGON_ID, MapConstantUtils.SOURCE_FENCE_POLYGON_ID);
        polygonLayer.withProperties(fillColor(get(MapConstantUtils.FIELD_FENCE_COLOR)),
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
    public void setFenceCircle(FenceCoreBean fence) {
        this.fence = fence;
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
                        polygon =getCircleGeometry(Point.fromLngLat(lng, lat), fence.distance);
                    }
                }
            }
            if (polygon != null) {
                JsonObject properties = new JsonObject();
                properties.addProperty(MapConstantUtils.FIELD_FENCE_COLOR,
                        ColorUtils.colorToRgbaString(MapImageSettingUtils.getFencePolygonFillColor(context)));
                properties.addProperty(MapConstantUtils.FIELD_ID, fence.id);
                Feature featurePolygon = Feature.fromGeometry(
                        polygon, properties);
                featuresPolygon.add(featurePolygon);
                //
                for (List<Point> linePoints : polygon.coordinates()) {
                    Feature featureLine = Feature.fromGeometry(
                            LineString.fromLngLats(linePoints)
                    );
                    featuresLine.add(featureLine);
                }
            }
            FeatureCollection featurePointCollection = FeatureCollection.fromFeatures(featuresPolygon);
            fencePolygonSource.setGeoJson(featurePointCollection);
            if(featuresLine!=null){
                fillLineSource.setGeoJson(FeatureCollection.fromFeatures(featuresLine));
            }
            if (needZoom) {
                zoomCenterFence(fence);
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
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, OSUtils.getScreenWidth(context) / 4);
            mapboxMap.animateCamera(cameraUpdate);
        }
    }

}
