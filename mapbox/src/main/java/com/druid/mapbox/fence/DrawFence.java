package com.druid.mapbox.fence;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOutlineColor;
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
import android.graphics.PointF;
import android.text.TextUtils;

import com.druid.mapbox.utils.LatLngUtils;
import com.druid.mapbox.utils.MapConstantUtils;
import com.druid.mapbox.utils.MapImageSettingUtils;
import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.bean.FenceType;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.fence.layer.FenceListLayer;
import com.druid.mapcore.fence.view.marker.FenceTitleMarkView;
import com.druid.mapcore.interfaces.FenceClickListener;
import com.druid.mapcore.utils.MapUtils;
import com.google.gson.JsonObject;
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
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfTransformation;

import java.util.ArrayList;
import java.util.List;

public abstract class DrawFence extends FenceListLayer {
    protected MapView mapView = null;
    protected MapboxMap mapboxMap = null;

    public DrawFence(Context context) {
        super(context);
    }

    protected void createFenceSource(Style style) {
        createPolygonSource(style);
        createFillLineSource(style);
        createTitleManager(style);
    }

    private ArrayList<FenceCoreBean> sourcePolygon = new ArrayList<>();
    private ArrayList<FenceCoreBean> sourceCircle = new ArrayList<>();

    protected void drawFence() {
        for (FenceCoreBean fence : source) {
            if (fence.type.equals(FenceType.Polygon) || fence.type.equals(FenceType.Rectangle)) {
                sourcePolygon.add(fence);
            }

            if (fence.type.equals(FenceType.Round)) {
                sourceCircle.add(fence);
            }
        }
        drawFencePolygonLayer();
        drawFenceTitleManager();
    }

    //polygon
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

    private void drawFencePolygonLayer() {
        ArrayList<Feature> featuresPolygon = new ArrayList<>();
        ArrayList<Feature> featuresLine = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            List<List<Point>> mapboxPoints = new ArrayList<>();
            List<Point> mapboxPoint = new ArrayList<>();
            FenceCoreBean fence = source.get(i);
            Polygon polygon = null;
            if (fence.type.equals(FenceType.Polygon) || fence.type.equals(FenceType.Rectangle)) {
                if (fence.points.size() >= 3) {
                    for (int j = 0; j < fence.points.size(); j++) {
                        double[] point = fence.points.get(j);
                        double lat = point[0];
                        double lng = point[1];
                        if (MapUtils.validLatLng(lat, lng)) {
                            Point point_use = Point.fromLngLat(lng, lat);
                            mapboxPoint.add(point_use);
                        }
                    }
                    mapboxPoint.add(mapboxPoint.get(0));//绘制结束点
                    mapboxPoints.add(mapboxPoint);
                    polygon = Polygon.fromLngLats(mapboxPoints);
                }
            } else {
                if (fence.type.equals(FenceType.Round)) {
                    if (fence.points.size() == 1) {
                        double lat = fence.points.get(0)[0];
                        double lng = fence.points.get(0)[1];
                        if (MapUtils.validLatLng(lat, lng)) {
                            polygon = getCircleGeometry(Point.fromLngLat(lng, lat), fence.distance);
                        }
                    }
                }
            }
            if (polygon != null) {
                JsonObject properties = new JsonObject();
                properties.addProperty(MapConstantUtils.FIELD_FENCE_COLOR,
                        ColorUtils.colorToRgbaString(MapImageSettingUtils.getFencePolygonFillColor(context)));
                if(click_id.equals(fence.id)){
                    properties.addProperty(MapConstantUtils.FIELD_FENCE_COLOR,
                            ColorUtils.colorToRgbaString(MapImageSettingUtils.getFencePolygonBorderColor(context)));
                }
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
        }
        FeatureCollection featurePointCollection = FeatureCollection.fromFeatures(featuresPolygon);
        fencePolygonSource.setGeoJson(featurePointCollection);
        if(featuresLine!=null){
            fillLineSource.setGeoJson(FeatureCollection.fromFeatures(featuresLine));
        }
    }

    private Polygon getCircleGeometry(Point point, double radius) {
        Polygon polygon = TurfTransformation.circle(point, radius, TurfConstants.UNIT_METERS);
        return polygon;
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

    //
    protected GeoJsonSource fenceTitleSource = null;
    protected SymbolLayer fenceTitleLayer = null;

    private void createTitleManager(Style style) {
        fenceTitleSource = new GeoJsonSource(MapConstantUtils.SOURCE_FENCE_TITLE_ID);
        style.addSource(fenceTitleSource);
        style.addLayer(createFenceTitleLayer());
    }

    private SymbolLayer createFenceTitleLayer() {
        fenceTitleLayer = new SymbolLayer(MapConstantUtils.LAYER_FENCE_TITLE_ID,
                MapConstantUtils.SOURCE_FENCE_TITLE_ID);
        fenceTitleLayer.withProperties(
                iconImage(get(MapConstantUtils.FIELD_FENCE_IMAGE)),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, MapImageSettingUtils.getFenceMarkerOffsetY(context,true)}),
                iconIgnorePlacement(true)
        );
        fenceTitleLayer.withProperties(visibility(Property.VISIBLE));
        return fenceTitleLayer;
    }

    protected ArrayList<LatLng> centers = new ArrayList<>();

    private void drawFenceTitleManager() {
        ArrayList<Feature> featuresPoint = new ArrayList<>();
        for (FenceCoreBean fence : source) {
            LatLng center = null;
            if (fence.type.equals(FenceType.Polygon) || fence.type.equals(FenceType.Rectangle)) {
                if (fence.points.size() >= 3) {
                    ArrayList<LatLngBean> latLngs = new ArrayList<>();
                    for (int j = 0; j < fence.points.size(); j++) {
                        double[] point = fence.points.get(j);
                        double lat = point[0];
                        double lng = point[1];
                        if (MapUtils.validLatLng(lat, lng)) {
                            LatLngBean latLng = new LatLngBean(lat, lng);
                            latLngs.add(latLng);
                        }
                    }
                    LatLngBean latLngCenter = MapUtils.getCenterOfGravityPoint(latLngs);
                    center = LatLngUtils.mapLatLngFrom(latLngCenter);
                }
            }

            if (fence.type.equals(FenceType.Round)) {
                if (fence.points.size() == 1) {
                    double lat = fence.points.get(0)[0];
                    double lng = fence.points.get(0)[1];
                    if (MapUtils.validLatLng(lat, lng)) {
                        center = new LatLng(lat, lng);
                    }
                }
            }
            if (center != null) {
                centers.add(center);
                FenceTitleMarkView titleView = new FenceTitleMarkView(context).setFenceBean(fence);
                mapboxMap.getStyle().addImage(fence.id, titleView.getBitmapView());
                //todo
                JsonObject properties = new JsonObject();
                properties.addProperty(MapConstantUtils.FIELD_FENCE_IMAGE, fence.id);
                Feature featurePoint = Feature.fromGeometry(Point.fromLngLat(center.getLongitude(), center.getLatitude()), properties);
                featuresPoint.add(featurePoint);
            }
        }
        FeatureCollection featurePointCollection = FeatureCollection.fromFeatures(featuresPoint);
        fenceTitleSource.setGeoJson(featurePointCollection);
    }

    //
    private String click_id = "";

    protected void queryFenceClick(LatLngBean latLngBean) {
        LatLng latLng = new LatLng(latLngBean.getLat(), latLngBean.getLng());
        PointF point = mapboxMap.getProjection().toScreenLocation(latLng);
        List<Feature> features = mapboxMap.queryRenderedFeatures(point,
                MapConstantUtils.LAYER_FENCE_POLYGON_ID);
        if (!features.isEmpty()) {
            Feature feature = features.get(0);
            if (feature.hasProperty(MapConstantUtils.FIELD_ID)) {
                String fence_id =
                        feature.getStringProperty(MapConstantUtils.FIELD_ID);
                Polygon polygon = (Polygon) feature.geometry();
                if (!TextUtils.isEmpty(fence_id)) {
                    this.click_id = fence_id;
                    drawFencePolygonLayer();
                    if (fenceClickListener != null) {
                        fenceClickListener.fenceOnClick(click_id);
                    }
                }
            }
        }
    }

    protected void clearFenceSelected() {
        this.click_id = "";
        drawFencePolygonLayer();
    }

    private FenceClickListener fenceClickListener;

    protected void setFenceClickListener(FenceClickListener listener) {
        this.fenceClickListener = listener;
    }

    protected void setFenceSelected(String fence_id) {
        int index = -1;
        for (int i = 0; i < source.size(); i++) {
            FenceCoreBean mapFence = source.get(i);
            if (mapFence.id.equals(fence_id)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            this.click_id = fence_id;
            drawFencePolygonLayer();
            zoomCenterFence(source.get(index));
        }
    }

    private void zoomCenterFence(FenceCoreBean fence) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds latLngBounds = null;
        if (fence.type.equals(FenceType.Round)) {
            LatLng center = new LatLng(fence.points.get(0)[0], fence.points.get(0)[1]);
            Polygon polygon = getCircleGeometry(Point.fromLngLat(center.getLongitude(), center.getLatitude()),
                    fence.distance);
            if (polygon != null) {
                for (List<Point> points : polygon.coordinates()) {
                    for (Point point : points) {
                        LatLng latLng = new LatLng(point.latitude(), point.longitude());
                        builder.include(latLng);
                    }
                }
                latLngBounds = builder.build();
            }
        } else {
            ArrayList<double[]> points = fence.points;
            for (int i = 0; i < points.size(); i++) {
                double[] doubles = points.get(i);
                LatLng latLng = new LatLng(doubles[0], doubles[1]);
                builder.include(latLng);
            }
            latLngBounds = builder.build();
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100);
        mapboxMap.animateCamera(cameraUpdate);
    }

    protected void zoomLayer() {
        try {
            if (source.size() > 1) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng latLng : centers) {
                    builder.include(latLng);
                }
                LatLngBounds latLngBounds = builder.build();
                if (latLngBounds != null) {
                    CameraUpdate mapStatusUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds,
                            100);
                    mapboxMap.animateCamera(mapStatusUpdate);
                }
            } else {
                if (source.size() == 1) {
                    zoomCenterFence(source.get(0));
                }
            }
        } catch (Exception ex) {

        }
    }

    protected String getSelectedFenceId() {
        return click_id;
    }
}
