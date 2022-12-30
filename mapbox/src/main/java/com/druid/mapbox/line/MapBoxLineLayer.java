package com.druid.mapbox.line;

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
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.symbolPlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.symbolSpacing;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.druid.mapbox.utils.LatLngEvaluator;
import com.druid.mapbox.utils.MapConstantUtils;
import com.druid.mapbox.utils.MapImageSettingUtils;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.interfaces.LineTrackMoveListener;
import com.druid.mapcore.line.LineDrawLayer;
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
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;

public abstract class MapBoxLineLayer extends LineDrawLayer {
    public static final String TAG = MapBoxLineLayer.class.getName();

    public MapBoxLineLayer(Context context) {
        super(context);
    }

    protected MapView mapView = null;
    protected MapboxMap mapboxMap = null;
    protected Style style = null;
    protected LatLngBounds lineLayerLatLngBounds = null;
    protected ArrayList<LatLngBean> source = new ArrayList<>();

    protected Drawable startDrawable;
    protected Drawable endDrawable;
    protected boolean layerVisible = true;

    protected boolean setLayerVisible(boolean visible) {
        this.layerVisible = visible;
        if (visible) {
            if (lineLayer != null) {
                lineLayer.withProperties(visibility(Property.VISIBLE));
            }
            if (pointLayer != null) {
                pointLayer.withProperties(visibility(Property.VISIBLE));
            }
            if (arrowLayer != null) {
                arrowLayer.withProperties(visibility(Property.VISIBLE));
            }
            if (playPointLayer != null) {
                playPointLayer.withProperties(visibility(Property.VISIBLE));
            }
            if (playLineLayer != null) {
                playLineLayer.withProperties(visibility(Property.VISIBLE));
            }
        } else {
            if (lineLayer != null) {
                lineLayer.withProperties(visibility(Property.NONE));
            }
            if (pointLayer != null) {
                pointLayer.withProperties(visibility(Property.NONE));
            }
            if (arrowLayer != null) {
                arrowLayer.withProperties(visibility(Property.NONE));
            }
            if (playPointLayer != null) {
                playPointLayer.withProperties(visibility(Property.NONE));
            }
            if (playLineLayer != null) {
                playLineLayer.withProperties(visibility(Property.NONE));
            }
            playTracker(false);
            if (listener != null) {
                listener.lineTrackMoveStop();
            }
        }
        return visible;
    }

    protected void createMapLayer() {
        createLineSource(style);
        createArrowSource(style);
        createLinePointSource(style);
        createPlayLineSource(style);
        createPlayPointSource(style);
        setLayerVisible(layerVisible);
    }

    //region line
    private GeoJsonSource lineSource;
    private LineLayer lineLayer;

    private void createLineSource(Style style) {
        lineSource = new GeoJsonSource(MapConstantUtils.SOURCE_POLYGON_LINE_ID);
        style.addSource(lineSource);
        style.addLayer(createLineLayer());
    }

    protected int lineColor = MapImageSettingUtils.getFencePolylineBorderColor(context);

    private LineLayer createLineLayer() {
        lineLayer = new LineLayer(MapConstantUtils.LAYER_POLYGON_LINE_ID,
                MapConstantUtils.SOURCE_POLYGON_LINE_ID);
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
                if (i != 0) {
                    LatLngBean prePoint = source.get(i - 1);
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
        }
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
        pointSource = new GeoJsonSource(MapConstantUtils.SOURCE_POLYGON_POINT_ID);
        style.addSource(pointSource);
        boolean contains = false;
        for (Layer layer : style.getLayers()) {
            if (layer.getId().equals(MapConstantUtils.LAYER_UNCLUSTERED_ID)) {
                contains = true;
                break;
            }
        }
        if (contains) {
            style.addLayerAbove(createLineArrowLayer(), MapConstantUtils.LAYER_UNCLUSTERED_ID);
        } else {
            style.addLayer(createLinePointLayer());
        }
    }

    private SymbolLayer createLinePointLayer() {
        pointLayer = new SymbolLayer(MapConstantUtils.LAYER_POLYGON_POINT_ID,
                MapConstantUtils.SOURCE_POLYGON_POINT_ID);
        pointLayer.withProperties(
                iconImage(get(MapConstantUtils.FIELD_IMAGE)),
                iconAllowOverlap(true),
//                iconOffset(new Float[]{0f, MapImageSettingUtils.getFenceMarkerOffsetY(context, true)}),
                iconIgnorePlacement(true)
        );
        pointLayer.withProperties(visibility(Property.VISIBLE));
        return pointLayer;
    }

    protected void drawPoint() {
        if (startDrawable != null && endDrawable != null) {
            mapboxMap.getStyle().addImage("line_point_start", startDrawable);
            mapboxMap.getStyle().addImage("line_point_end", endDrawable);

            List<Feature> featuresPoint = new ArrayList<>();
            if (source.size() > 0) {
                JsonObject properties = new JsonObject();
                properties.addProperty(MapConstantUtils.FIELD_IMAGE, "line_point_start");
                Point point_start = Point.fromLngLat(source.get(0).getLng(), source.get(0).getLat());
                Feature featurePoint = Feature.fromGeometry(point_start, properties);
                featuresPoint.add(featurePoint);
            }
            if (source.size() > 1) {
                JsonObject properties = new JsonObject();
                properties.addProperty(MapConstantUtils.FIELD_IMAGE, "line_point_end");
                Point point_end = Point.fromLngLat(source.get(source.size() - 1).getLng(),
                        source.get(source.size() - 1).getLat());
                Feature featurePoint = Feature.fromGeometry(point_end, properties);
                featuresPoint.add(featurePoint);
            }
            FeatureCollection featurePointCollection = FeatureCollection.fromFeatures(featuresPoint);
            pointSource.setGeoJson(featurePointCollection);
        }
    }
    //endregion

    //region arrow-line
    private GeoJsonSource arrowSource;
    private SymbolLayer arrowLayer;
    protected Drawable arrowDrawable = null;

    private void createArrowSource(Style style) {
        arrowSource = new GeoJsonSource(MapConstantUtils.SOURCE_LINE_ARROW_ID);
        style.addSource(arrowSource);
        style.addLayer(createLineArrowLayer());
    }

    private SymbolLayer createLineArrowLayer() {
        arrowLayer = new SymbolLayer(MapConstantUtils.LAYER_LINE_ARROW_ID,
                MapConstantUtils.SOURCE_LINE_ARROW_ID);
        arrowLayer.withProperties(
                iconAllowOverlap(true),
                iconIgnorePlacement(false),
                symbolPlacement(Property.SYMBOL_PLACEMENT_LINE),
                symbolSpacing(50f),
                iconImage(MapConstantUtils.FIELD_ARROW_IMAGE)
        );
        arrowLayer.withProperties(visibility(Property.VISIBLE));
        return arrowLayer;
    }

    protected void drawArrowPoint() {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            LatLngBean point = source.get(i);
            double lat = point.getLat();
            double lng = point.getLng();
            if (MapUtils.validLatLng(lat, lng)) {
                Point point_start = Point.fromLngLat(source.get(i).getLng(), source.get(i).getLat());
                points.add(point_start);
            }
        }
        LineString lineString = LineString.fromLngLats(points);
        Feature featurePoint = Feature.fromGeometry(lineString);
        FeatureCollection featureLineCollection = FeatureCollection.fromFeature(featurePoint);
        arrowSource.setGeoJson(featureLineCollection);
    }
    //endregion

    private LineTrackMoveListener listener;

    public void setPlayTrackListener(LineTrackMoveListener listener) {
        this.listener = listener;
    }

    private boolean isPlayTracker = false;
    protected ValueAnimator markerIconAnimator;
    private LatLng markerIconCurrentLocation;
    private Handler handler;
    private Runnable runnable;

    protected boolean playTracker(boolean isPlay) {
        this.isPlayTracker = isPlay;
        if (!isPlay) {
            onDestroyLayer();
            return true;
        }
        if (counter >= steps) {
            if (listener != null) {
                listener.lineTrackMoveStop();
            }
            return false;
        }
        if (newRoute.size() <= 1) {
            if (listener != null) {
                listener.lineTrackMoveStop();
            }
            return false;
        }
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if ((newRoute.size() - 1 > counter)) {
                    ValueAnimator.AnimatorUpdateListener listener = new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            LatLng progressLatLng = (LatLng) animation.getAnimatedValue();
                            if (isPlayTracker) {
                                if (mapboxMap != null) {
                                    if (mapboxMap.getStyle().isFullyLoaded()) {
                                        drawPlayLine(progressLatLng);
                                        drawPlayPoint(progressLatLng);
                                    }
                                }
                                preLatLng = progressLatLng;
                            }
                        }
                    };
                    Point nextLocation = newRoute.get(counter + 1);
                    if (markerIconAnimator != null && markerIconAnimator.isStarted()) {
                        markerIconCurrentLocation = (LatLng) markerIconAnimator.getAnimatedValue();
                        markerIconAnimator.cancel();
                    }
                    LatLng firstLocation = new LatLng(newRoute.get(counter).latitude(), newRoute.get(counter).longitude());
                    markerIconAnimator = ObjectAnimator
                            .ofObject(new LatLngEvaluator(), counter == 0 || markerIconCurrentLocation == null
                                            ? firstLocation
                                            : markerIconCurrentLocation,
                                    new LatLng(nextLocation.latitude(), nextLocation.longitude()))
                            .setDuration(10);
                    markerIconAnimator.setInterpolator(new LinearInterpolator());
                    markerIconAnimator.addUpdateListener(listener);
                    markerIconAnimator.start();
                    counter = counter + 1;
                    if (isPlayTracker) {
                        handler.postDelayed(this, 100);
                    }
                }
            }
        };
        handler.postDelayed(runnable, 100);
        return true;
    }

    int counter = 0;
    int steps = 0;
    int aLength = 0;
    List<Point> newRoute = new ArrayList<>();

    protected void setRouteData() {
        List<Point> points = new ArrayList<>();
        if (source.size() > 1) {
            for (LatLngBean latLng : source) {
                if (MapUtils.validLatLng(latLng.getLat(), latLng.getLng())) {
                    points.add(Point.fromLngLat(latLng.getLng(), latLng.getLat()));
                }
            }
            aLength = points.size();
            newRoute = resetRouteSource(points, 1000, TurfConstants.UNIT_KILOMETERS);
            steps = newRoute.size();
        }
    }

    private List<Point> resetRouteSource(List<Point> route, int nstep, String units) {
        List<Point> newroute = new ArrayList<>();
        double lineDistance = TurfMeasurement.length(route, units);
        double nDistance = lineDistance / nstep;
        for (int i = 0; i < route.size() - 1; i++) {
            Point from = route.get(i);
            Point to = route.get(i + 1);
            double lDistance = TurfMeasurement.distance(from, to, units);
            if (i == 0) {
                newroute.add(route.get(0));
            }
            if (lDistance > nDistance) {
                List<Point> rings = lineMore(from, to, lDistance, nDistance, units);
                newroute.addAll(rings);
            } else {
                newroute.add(route.get(i + 1));
            }
        }
        return newroute;
    }

    private List<Point> lineMore(Point from, Point to, double distance, double splitLength, String units) {
        List<Point> route = new ArrayList<>();
        route.add(from);
        route.add(to);
        //
        int step = (int) (distance / splitLength);
        double leftLength = distance - step * splitLength;
        List<Point> rings = new ArrayList<>();
        for (int i = 1; i <= step; i++) {
            double nlength = i * splitLength;
            Point pnt = TurfMeasurement.along(route, nlength, units);
            rings.add(pnt);
        }
        if (leftLength > 0) {
            rings.add(to);
        }
        return rings;
    }

    //region play point
    private GeoJsonSource playPointSource;
    private SymbolLayer playPointLayer;

    protected void createPlayPointSource(Style style) {
        playPointSource = new GeoJsonSource(MapConstantUtils.SOURCE_PLAY_POINT_ID);
        style.addSource(playPointSource);
        style.addLayer(createPlayPointLayer());
    }

    private SymbolLayer createPlayPointLayer() {
        playPointLayer = new SymbolLayer(MapConstantUtils.LAYER_PLAY_POINT_ID,
                MapConstantUtils.SOURCE_PLAY_POINT_ID);
        playPointLayer.withProperties(
                iconImage(MapConstantUtils.FIELD_MOVE_POSITION),
                iconRotate(get(MapConstantUtils.FIELD_BEARING)),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        playPointLayer.withProperties(visibility(Property.VISIBLE));
        return playPointLayer;
    }

    private LatLng preLatLng;

    protected void drawPlayPoint(LatLng latLng) {
        if (preLatLng != null) {
            JsonObject properties = new JsonObject();
            float bearing = getBearing(preLatLng, latLng);
            Log.i(TAG, bearing + "");
            properties.addProperty(MapConstantUtils.FIELD_BEARING, bearing);
            Feature feature = Feature.fromGeometry(
                    Point.fromLngLat(
                            latLng.getLongitude(),
                            latLng.getLatitude()), properties);
            playPointSource.setGeoJson(feature);
        }
    }

    private float getBearing(LatLng from, LatLng to) {
        float bear = (float) TurfMeasurement.bearing(
                Point.fromLngLat(from.getLongitude(), from.getLatitude(), from.getAltitude()),
                Point.fromLngLat(to.getLongitude(), to.getLatitude(), to.getAltitude())
        );
        bear -= mapboxMap.getCameraPosition().bearing;
        return bear;
    }
    //endregion

    //region play line
    private GeoJsonSource playLineSource;
    private LineLayer playLineLayer;
    private List<Point> playLines = new ArrayList<>();

    private void createPlayLineSource(Style style) {
        playLineSource = new GeoJsonSource(MapConstantUtils.SOURCE_PLAY_LINE_ID);
        style.addSource(playLineSource);
        style.addLayer(createPlayLineLayer());
    }

    protected int playLineColor = MapImageSettingUtils.getFencePolylineBorderColor(context);

    private LineLayer createPlayLineLayer() {
        playLineLayer = new LineLayer(MapConstantUtils.LAYER_PLAY_LINE_ID,
                MapConstantUtils.SOURCE_PLAY_LINE_ID);
        playLineLayer.withProperties(
                lineColor(get(MapConstantUtils.FIELD_PLAY_COLOR)),
                lineOpacity(1f),
                lineWidth(MapImageSettingUtils.FENCE_LINE_WIDTH),
                lineCap(LINE_CAP_ROUND),
                lineJoin(LINE_JOIN_ROUND)
        );
        playLineLayer.withProperties(visibility(Property.VISIBLE));
        return playLineLayer;
    }

    protected void drawPlayLine(LatLng latLng) {
        Point endPoint = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude(), latLng.getAltitude());
        playLines.add(endPoint);
        JsonObject properties = new JsonObject();
        properties.addProperty(MapConstantUtils.FIELD_PLAY_COLOR,
                ColorUtils.colorToRgbaString(playLineColor));
        Feature feature = Feature.fromGeometry(
                LineString.fromLngLats(playLines), properties
        );
        playLineSource.setGeoJson(feature);
    }

    protected void clearPlayTracker() {
        playTracker(false);
        playLines.clear();
        JsonObject properties = new JsonObject();
        properties.addProperty(MapConstantUtils.FIELD_PLAY_COLOR,
                ColorUtils.colorToRgbaString(playLineColor));
        Feature feature = Feature.fromGeometry(
                LineString.fromLngLats(playLines), properties
        );
        playLineSource.setGeoJson(feature);
        //
        newRoute.clear();
        this.counter = 0;
        this.steps = 0;
        this.aLength = 0;
        Feature[] features=new Feature[0];
        FeatureCollection fc=FeatureCollection.fromFeatures(features);
        playPointSource.setGeoJson(fc);
        //
    }
    //endregion

    public void onDestroyLayer() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
