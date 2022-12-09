package com.druid.mapbox.capture;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.druid.mapbox.utils.MapConstantUtils;
import com.druid.mapbox.utils.MapImageSettingUtils;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.MarkerBean;
import com.druid.mapcore.capture.CaptureDrawLayer;
import com.druid.mapcore.capture.view.CaptureMarkerView;
import com.druid.mapcore.interfaces.CaptureClickListener;
import com.druid.mapcore.utils.MapUtils;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class MapBoxCaptureLayer extends CaptureDrawLayer {
    public static final String TAG = MapBoxCaptureLayer.class.getName();

    public MapBoxCaptureLayer(Context context) {
        super(context);
    }

    protected MapView mapView = null;
    protected MapboxMap mapboxMap = null;
    protected Style style = null;
    protected LatLngBounds lineLayerLatLngBounds = null;
    protected boolean layerVisible = true;

    protected boolean setLayerVisible(boolean visible) {
        this.layerVisible = visible;
        return visible;
    }

    protected void createMapLayer() {
        createLineSource(style);
        createLinePointSource(style);
    }

    //region line
    private GeoJsonSource lineSource;
    private LineLayer lineLayer;

    private void createLineSource(Style style) {
        lineSource = new GeoJsonSource(MapConstantUtils.SOURCE_CAPTURE_LINE_ID);
        style.addSource(lineSource);
        style.addLayer(createLineLayer());
    }

    protected int lineColor = MapImageSettingUtils.getFencePolylineBorderColor(context);

    private LineLayer createLineLayer() {
        lineLayer = new LineLayer(MapConstantUtils.LAYER_CAPTURE_LINE_ID,
                MapConstantUtils.SOURCE_CAPTURE_LINE_ID);
        lineLayer.withProperties(
                lineColor(get(MapConstantUtils.FIELD_COLOR)),
                lineOpacity(1f),
                lineWidth(MapImageSettingUtils.FENCE_LINE_WIDTH),
                lineCap(LINE_CAP_ROUND),
                lineJoin(LINE_JOIN_ROUND)
        );
//        lineLayer.withProperties(visibility(Property.VISIBLE));
        return lineLayer;
    }

    protected void drawLine() {
        ArrayList<Feature> featuresLine = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < source.size(); i++) {
            LatLngBean point = source.get(i);
            double lat = point.getLat();
            double lng = point.getLng();
            if (MapUtils.validLatLng(lat, lng)) {
                JsonObject properties = new JsonObject();
                properties.addProperty(MapConstantUtils.FIELD_COLOR,
                        ColorUtils.colorToRgbaString(lineColor));
                Point point_use = Point.fromLngLat(lng, lat);
                builder.include(new LatLng(lat, lng));
                LatLngBean prePoint = null;
                if (i != 0) {
                    prePoint = source.get(i - 1);
                } else {
                    prePoint = source.get(source.size() - 1);
                }
                double lat_pre = prePoint.getLat();
                double lng_pre = prePoint.getLng();
                List<Point> linePoints = new ArrayList<>();
                Point point_pre = Point.fromLngLat(lng_pre, lat_pre);
                linePoints.add(point_pre);
                linePoints.add(point_use);
                Feature featureLine = Feature.fromGeometry(
                        LineString.fromLngLats(linePoints), properties
                );
                featuresLine.add(featureLine);
            }
        }
        //

        if (featuresLine.size() > 1) {
            lineLayerLatLngBounds = builder.build();
        }
        FeatureCollection featureLineCollection = FeatureCollection.fromFeatures(featuresLine);
        lineSource.setGeoJson(featureLineCollection);
    }
    //endregion

    //region point
    private GeoJsonSource pointSource;
    private SymbolLayer pointLayer;

    private void createLinePointSource(Style style) {
        pointSource = new GeoJsonSource(MapConstantUtils.SOURCE_CAPTURE_POINT_ID);
        style.addSource(pointSource);
        boolean contains = false;
        for (Layer layer : style.getLayers()) {
            if (layer.getId().equals(MapConstantUtils.LAYER_UNCLUSTERED_ID)) {
                contains = true;
                break;
            }
        }
        if (contains) {
            style.addLayerAbove(createLinePointLayer(), MapConstantUtils.LAYER_UNCLUSTERED_ID);
        } else {
            style.addLayer(createLinePointLayer());
        }
    }

    private SymbolLayer createLinePointLayer() {
        pointLayer = new SymbolLayer(MapConstantUtils.LAYER_CAPTURE_POINT_ID,
                MapConstantUtils.SOURCE_CAPTURE_POINT_ID);
        pointLayer.withProperties(
                iconImage(get(MapConstantUtils.FIELD_IMAGE)),
                iconAllowOverlap(true),
                iconRotate(get(MapConstantUtils.FIELD_BEARING)),
//                iconOffset(new Float[]{0f, MapImageSettingUtils.getFenceMarkerOffsetY(context, true)}),
                iconIgnorePlacement(true)
        );
        pointLayer.withProperties(visibility(Property.VISIBLE));
        return pointLayer;
    }

    protected void drawPoint() {
        for (MarkerBean marker : markers) {
            CaptureMarkerView view = new CaptureMarkerView(context);
            view.setMarker(marker);
            mapboxMap.getStyle().addImage(marker.name, view.getBitmapView());
        }

        List<Feature> featuresPoint = new ArrayList<>();
        for (MarkerBean marker : markers) {
            JsonObject properties = new JsonObject();
            properties.addProperty(MapConstantUtils.FIELD_IMAGE, marker.name);
            double bearing = 360f - marker.angle - mapboxMap.getCameraPosition().bearing;
            properties.addProperty(MapConstantUtils.FIELD_BEARING, bearing);
            properties.addProperty(MapConstantUtils.FIELD_ID, marker.id);
            Point point_start = Point.fromLngLat(marker.location.getLng(), marker.location.getLat());
            Feature featurePoint = Feature.fromGeometry(point_start, properties);
            featuresPoint.add(featurePoint);
        }
        FeatureCollection featurePointCollection = FeatureCollection.fromFeatures(featuresPoint);
        pointSource.setGeoJson(featurePointCollection);
    }
    //endregion

    protected void queryCaptureClick(LatLngBean latLngBean) {
        LatLng latLng = new LatLng(latLngBean.getLat(), latLngBean.getLng());
        PointF point = mapboxMap.getProjection().toScreenLocation(latLng);
        List<Feature> features = mapboxMap.queryRenderedFeatures(point,
                MapConstantUtils.LAYER_CAPTURE_POINT_ID);
        if (!features.isEmpty()) {
            Feature feature = features.get(0);
            if (feature.hasProperty(MapConstantUtils.FIELD_ID)) {
                String marker_id =
                        feature.getStringProperty(MapConstantUtils.FIELD_ID);
                if (!TextUtils.isEmpty(marker_id)) {
                    if (captureClickListener != null) {
                        captureClickListener.captureOnClick(marker_id);
                    }
                }
            }
        }
    }

    CaptureClickListener captureClickListener = null;

    protected void setCaptureClickListener(CaptureClickListener listener) {
        this.captureClickListener = listener;
    }

    protected Drawable captureDrawable = null;

    public void onDestroyLayer() {

    }
}
