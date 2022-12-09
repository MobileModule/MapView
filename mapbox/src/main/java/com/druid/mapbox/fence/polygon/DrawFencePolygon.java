package com.druid.mapbox.fence.polygon;

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

import com.druid.mapbox.utils.LatLngUtils;
import com.druid.mapbox.utils.MapConstantUtils;
import com.druid.mapbox.utils.MapImageSettingUtils;
import com.druid.mapcore.bean.FencePolygonPointsBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.fence.layer.polygon.FenceDrawPolygonLayer;
import com.druid.mapcore.interfaces.FencePointMarkerClickListener;
import com.druid.mapcore.utils.MapSettingConstantUtils;
import com.druid.mapcore.utils.MapUtils;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.camera.CameraPosition;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class DrawFencePolygon extends FenceDrawPolygonLayer {
    protected int minSize = 3;
    protected FencePolygonPointsBean fencePoints = new FencePolygonPointsBean();
    protected MapView mapView = null;
    protected MapboxMap mapboxMap = null;
    protected Style style = null;

    protected GeoJsonSource fenceLineSource = null;
    protected LineLayer fenceLineLayer = null;
    protected GeoJsonSource fencePointSource = null;
    protected SymbolLayer fencePointLayer = null;
    protected GeoJsonSource fenceFillSource = null;
    protected FillLayer fenceFillLayer = null;


    public DrawFencePolygon(Context context) {
        super(context);
    }

    public void createFenceSource(Style style) {
        createFencePointSource(style);
        createFenceLineSource(style);
        createFenceFillSource(style);
    }

    private void createFencePointSource(Style style) {
        fencePointSource = new GeoJsonSource(MapConstantUtils.SOURCE_FENCE_POLYGON_POINT_ID);
        style.addSource(fencePointSource);
        style.addLayer(createFencePointLayer());
    }

    private SymbolLayer createFencePointLayer() {
        fencePointLayer = new SymbolLayer(MapConstantUtils.LAYER_FENCE_POLYGON_POINT_ID,
                MapConstantUtils.SOURCE_FENCE_POLYGON_POINT_ID);
        fencePointLayer.withProperties(
                iconImage(get(MapConstantUtils.FIELD_FENCE_IMAGE)),
                iconAllowOverlap(true),
                iconOffset(new Float[]{0f, MapImageSettingUtils.getFenceMarkerOffsetY(context,true)}),
                iconIgnorePlacement(true)
        );
        fencePointLayer.withProperties(visibility(Property.VISIBLE));
        return fencePointLayer;
    }

    private void createFenceLineSource(Style style) {
        fenceLineSource = new GeoJsonSource(MapConstantUtils.SOURCE_FENCE_POLYGON_LINE_ID);
        style.addSource(fenceLineSource);
        style.addLayerBelow(createFenceLineLayer(), MapConstantUtils.LAYER_FENCE_POLYGON_POINT_ID);
    }

    private LineLayer createFenceLineLayer() {
        fenceLineLayer = new LineLayer(MapConstantUtils.LAYER_FENCE_POLYGON_LINE_ID,
                MapConstantUtils.SOURCE_FENCE_POLYGON_LINE_ID);
        fenceLineLayer.withProperties(
                lineColor(MapImageSettingUtils.getFencePolylineBorderColor(context)),
                lineOpacity(1f),
                lineWidth(MapImageSettingUtils.FENCE_LINE_WIDTH),
                lineCap(LINE_CAP_ROUND),
                lineJoin(LINE_JOIN_ROUND)
        );
        fenceLineLayer.withProperties(visibility(Property.VISIBLE));
        return fenceLineLayer;
    }

    private void createFenceFillSource(Style style) {
        fenceFillSource = new GeoJsonSource(MapConstantUtils.SOURCE_FENCE_POLYGON_FILL_ID);
        style.addSource(fenceFillSource);
        style.addLayerBelow(createFenceFillLayer(), MapConstantUtils.LAYER_FENCE_POLYGON_POINT_ID);
    }

    private FillLayer createFenceFillLayer() {
        fenceFillLayer = new FillLayer(MapConstantUtils.LAYER_FENCE_POLYGON_FILL_ID,
                MapConstantUtils.SOURCE_FENCE_POLYGON_FILL_ID);
        fenceFillLayer.withProperties(
                fillColor(MapImageSettingUtils.getFencePolygonFillColor(context)),
                fillOutlineColor(MapImageSettingUtils.getFencePolygonBorderColor(context))
        );
        fenceFillLayer.withProperties(visibility(Property.NONE));
        return fenceFillLayer;
    }

    private LatLngBounds fenceLayerLatLngBounds = null;

    private void updateDrawFenceLine(boolean drawEndLine) {
        List<Point> mapPoints = new ArrayList<>();
        ArrayList<Feature> featuresPoint = new ArrayList<>();
        ArrayList<Feature> featuresLine = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        List<LatLngBean> points = fencePoints.getFencePointList(editorFencePointIndex, tempClickPoint);
        for (int index = 0; index < points.size(); index++) {
            LatLngBean point = points.get(index);
            double lat = point.getLat();
            double lng = point.getLng();
            if (MapUtils.validLatLng(lat, lng)) {
                JsonObject properties = new JsonObject();
                properties.addProperty(MapConstantUtils.FIELD_ID, index + "");
                properties.addProperty(MapConstantUtils.FIELD_INDEX, index);
                if (index == 0) {
                    properties.addProperty(MapConstantUtils.FIELD_FENCE_IMAGE, "icon_fence_point_start");
                } else {
                    properties.addProperty(MapConstantUtils.FIELD_FENCE_IMAGE, "icon_fence_point_end");
                }
                Point point_use = Point.fromLngLat(lng, lat);
                builder.include(new LatLng(lat, lng));
                mapPoints.add(point_use);
                Feature featurePoint = Feature.fromGeometry(point_use, properties);
                featuresPoint.add(featurePoint);
                if (index != 0) {
                    LatLngBean prePoint = points.get(index - 1);
                    double lat_pre = prePoint.getLat();
                    double lng_pre = prePoint.getLng();
                    List<Point> linePoints = new ArrayList<>();
                    Point point_pre = Point.fromLngLat(lng_pre, lat_pre);
                    linePoints.add(point_pre);
                    linePoints.add(point_use);
                    Feature featureLine = Feature.fromGeometry(
                            LineString.fromLngLats(linePoints)
                    );
                    featuresLine.add(featureLine);
                }
            }
        }
        if (featuresPoint.size() > 1) {
            fenceLayerLatLngBounds = builder.build();
        }
        //绘制结束线
        if (drawEndLine) {
            List<Point> linePoints = new ArrayList<>();
            linePoints.add(mapPoints.get(mapPoints.size() - 1));
            linePoints.add(mapPoints.get(0));
            featuresLine.add(Feature.fromGeometry(
                    LineString.fromLngLats(linePoints)));
        }

        FeatureCollection featureLineCollection = FeatureCollection.fromFeatures(featuresLine);
        fenceLineSource.setGeoJson(featureLineCollection);

        FeatureCollection featurePointCollection = FeatureCollection.fromFeatures(featuresPoint);
        fencePointSource.setGeoJson(featurePointCollection);
    }

    private void updateDrawFencePoint() {
        List<Point> mapPoints = new ArrayList<>();
        ArrayList<Feature> featuresPoint = new ArrayList<>();
        Iterator<Map.Entry<Integer, LatLngBean>> it = fencePoints.getFencePointMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, LatLngBean> entry = it.next();
            LatLngBean point = entry.getValue();
            int index = entry.getKey();
            double lat = point.getLat();
            double lng = point.getLng();
            if (MapUtils.validLatLng(lat, lng)) {
                JsonObject properties = new JsonObject();
                properties.addProperty(MapConstantUtils.FIELD_ID, index + "");
                properties.addProperty(MapConstantUtils.FIELD_INDEX, index);
                if (index == 0) {
                    properties.addProperty(MapConstantUtils.FIELD_FENCE_IMAGE, "icon_fence_point_start");
                } else {
                    properties.addProperty(MapConstantUtils.FIELD_FENCE_IMAGE, "icon_fence_point_end");
                }
                Point point_use = Point.fromLngLat(lng, lat);
                mapPoints.add(point_use);
                Feature featurePoint = Feature.fromGeometry(point_use, properties);
                featuresPoint.add(featurePoint);
            }
        }
        FeatureCollection featurePointCollection = FeatureCollection.fromFeatures(featuresPoint);
        fencePointSource.setGeoJson(featurePointCollection);
    }

    public void updateDrawFence(boolean drawEndLine) {
        updateDrawFenceLine(drawEndLine);
        updateDrawFencePoint();
    }

    public boolean completeDrawFence(boolean needMoveCamera) {
        if (this.editorFencePointIndex != -1) {
            if (fencePoints.size() < 2) {
                return false;
            }
        } else {
            if (fencePoints.size() < minSize) {
                return false;
            }
        }
        fenceLineLayer.withProperties(visibility(Property.VISIBLE));
        fenceFillLayer.withProperties(visibility(Property.VISIBLE));
        updateDrawFence(true);
        //
        ArrayList<Feature> featuresPolygon = new ArrayList<>();
        List<List<Point>> mapboxPoints = new ArrayList<>();
        List<Point> mapboxPoint = new ArrayList<>();
        List<LatLngBean> points = fencePoints.getFencePointList(editorFencePointIndex, tempClickPoint);
        for (int i = 0; i < points.size(); i++) {
            LatLngBean point = points.get(i);
            double lat = point.getLat();
            double lng = point.getLng();
            if (MapUtils.validLatLng(lat, lng)) {
                Point point_use = Point.fromLngLat(lng, lat);
                mapboxPoint.add(point_use);
            }
        }
        mapboxPoint.add(mapboxPoint.get(0));//绘制结束点
        mapboxPoints.add(mapboxPoint);
        Polygon polygon = Polygon.fromLngLats(mapboxPoints);
        JsonObject properties = new JsonObject();
        Feature featurePolygon = Feature.fromGeometry(
                polygon, properties);
        featuresPolygon.add(featurePolygon);
        //
        FeatureCollection featureFillCollection = FeatureCollection.fromFeatures(featuresPolygon);
        this.fenceFillSource.setGeoJson(featureFillCollection);
        this.drawComplete = true;
        if (needMoveCamera) {
            cameraToFenceBounds();
        }
        if (centerMarkView != null) {
            if (this.editorFencePointIndex == -1) {
                centerMarkView.setCenterMarkerViewVisible(false);
                if (fencePointMarkerClickListener != null) {
                    fencePointMarkerClickListener.fencePointMarkerClick(false);
                }
            } else {
                centerMarkView.setCenterMarkerViewVisible(true);
                if (fencePointMarkerClickListener != null) {
                    fencePointMarkerClickListener.fencePointMarkerClick(true);
                }
            }
        }
        return true;
    }

    protected void reset() {
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<>());
        fencePointSource.setGeoJson(featureCollection);
        fenceLineSource.setGeoJson(featureCollection);
        fenceFillSource.setGeoJson(featureCollection);
        this.editorFencePointIndex = -1;
        this.tempClickPoint = null;
    }

    protected void queryMarkerClick(LatLngBean latLngBean) {
        LatLng latLng = new LatLng(latLngBean.getLat(), latLngBean.getLng());
        PointF point = mapboxMap.getProjection().toScreenLocation(latLng);
        List<Feature> features = mapboxMap.queryRenderedFeatures(point,
                MapConstantUtils.LAYER_FENCE_POLYGON_POINT_ID);
        if (!features.isEmpty()) {
            Feature feature = features.get(0);
            if (feature.hasProperty(MapConstantUtils.FIELD_INDEX)) {
                int click_index =
                        feature.getNumberProperty(MapConstantUtils.FIELD_INDEX).intValue();
                Point point_geo = (Point) feature.geometry();
                if (editorFencePointIndex != -1 && tempClickPoint != null) {
                    fencePoints.addFencePoint(editorFencePointIndex, tempClickPoint);
                }
                editorFencePoint(click_index);
            }
        }
    }

    private int editorFencePointIndex = -1;
    private LatLngBean tempClickPoint;

    private void editorFencePoint(int index) {
        LatLngBean removeLatLng = fencePoints.removeFencePoint(index);
        if (removeLatLng != null) {
            this.editorFencePointIndex = index;
            tempClickPoint = removeLatLng;//fencePoints.removeFencePoint(index);
            updateDrawFencePoint();
            cameraMoveToFencePoint(LatLngUtils.mapLatLngFrom(tempClickPoint));
        }
    }

    private void cameraMoveToFencePoint(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 200,
                new MapboxMap.CancelableCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onFinish() {
                        if (centerMarkView != null) {
                            centerMarkView.setCenterMarkerViewVisible(true);
                            if (fencePointMarkerClickListener != null) {
                                fencePointMarkerClickListener.fencePointMarkerClick(true);
                            }
                        }
                    }
                });
    }

    public void cameraFlatMoveToPoint(LatLngBean latLngBean) {
        LatLng latLng = new LatLng(latLngBean.getLat(), latLngBean.getLng());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(mapboxMap.getCameraPosition().zoom)
                .build();
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    protected boolean completeEditorFencePoint() {
        if (editorFencePointIndex != -1) {
            fencePoints.addFencePoint(editorFencePointIndex, LatLngUtils.mapLatLngParse(mapboxMap.getCameraPosition().target));
            this.editorFencePointIndex = -1;
            this.tempClickPoint = null;
            completeDrawFence(true);
            return true;
        }
        return false;
    }

    protected boolean removeFencePoint() {
        if (this.drawComplete) {
            if (editorFencePointIndex != -1) {
                fencePoints.removeFencePoint(editorFencePointIndex);
                this.editorFencePointIndex = -1;
                this.tempClickPoint = null;
                completeDrawFence(true);
                return true;
            }
        }
        return false;
    }

    protected void cameraToFenceBounds() {
        if (fenceLayerLatLngBounds != null) {
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(fenceLayerLatLngBounds, MapSettingConstantUtils.MAP_BOUNDS_PADDING));
        }
    }

    private FencePointMarkerClickListener fencePointMarkerClickListener;

    public void setFencePointMarkerClickListener(FencePointMarkerClickListener listener) {
        this.fencePointMarkerClickListener = listener;
    }
}
