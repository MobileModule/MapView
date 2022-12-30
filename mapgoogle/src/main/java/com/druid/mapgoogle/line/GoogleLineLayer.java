package com.druid.mapgoogle.line;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.MapEngineType;
import com.druid.mapcore.interfaces.LineTrackMoveListener;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.line.LineDrawLayer;
import com.druid.mapcore.utils.AverageLineUtils;
import com.druid.mapcore.utils.MapBitmapUtils;
import com.druid.mapcore.utils.MapUtils;
import com.druid.mapcore.utils.turf.TurfConstantsUtils;
import com.druid.mapcore.utils.turf.TurfMeasurementUtils;
import com.druid.mapgoogle.R;
import com.druid.mapgoogle.utils.LatLngEvaluator;
import com.druid.mapgoogle.utils.LatLngUtils;
import com.druid.mapgoogle.utils.MapConstantUtils;
import com.druid.mapgoogle.utils.MapImageSettingUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public abstract class GoogleLineLayer extends LineDrawLayer implements MapCameraIdleListener {
    public static final String TAG = GoogleLineLayer.class.getName();
    protected MapView mapView = null;
    protected GoogleMap googleMap = null;
    protected DruidMapView druidMapView = null;

    public GoogleLineLayer(Context context) {
        super(context);
    }

    protected LatLngBounds lineLayerLatLngBounds = null;
    protected ArrayList<LatLngBean> source = new ArrayList<>();

    protected Drawable startDrawable;
    protected Drawable endDrawable;
    protected Drawable arrowDrawable = null;
    protected boolean layerVisible = true;

    protected boolean setLayerVisible(boolean visible) {
        try {
            if (trackerLine != null) {
                if (visible) {
                    trackerLine.setVisible(true);
                } else {
                    trackerLine.setVisible(false);
                }
            }
            for (Marker marker : pointMarkers) {
                if (visible) {
                    marker.setVisible(true);
                } else {
                    marker.setVisible(false);
                }
            }
            for (Marker marker : arrowMakers) {
                if (visible) {
                    marker.setVisible(true);
                } else {
                    marker.setVisible(false);
                }
            }
            if (playMarker != null) {
                if (visible) {
                    playMarker.setVisible(true);
                } else {
                    playMarker.setVisible(false);
                }
            }
            if (playLine != null) {
                if (visible) {
                    playLine.setVisible(true);
                } else {
                    playLine.setVisible(false);
                }
            }
            if (visible == false) {
                playTracker(false);
                if (listener != null) {
                    listener.lineTrackMoveStop();
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        this.layerVisible = visible;
        return visible;
    }

    protected void createMapLayer() {
        clearMapDraw();
        drawArrowPoint();
        drawLine();
        drawPoint();
        setRouteData();
    }

    private void clearMapDraw() {
        if (trackerLine != null) {
            trackerLine.remove();
        }
        for (Marker marker : pointMarkers) {
            marker.remove();
        }
        pointMarkers.clear();
        clearArrowMaker();
        clearPlayTracker();
    }

    //region line
    protected int lineColor = MapImageSettingUtils.getFencePolylineBorderColor(context);

    private Polyline trackerLine;

    private void drawLine() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        ArrayList<LatLng> points = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            LatLngBean point = source.get(i);
            double lat = point.getLat(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
            double lng = point.getLng(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
            if (MapUtils.validLatLng(lat, lng)) {
                LatLng latLng = new LatLng(lat, lng);
                points.add(latLng);
                builder.include(latLng);

            }
        }
        if (points.size() > 1) {
            lineLayerLatLngBounds = builder.build();
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.width(MapImageSettingUtils.FENCE_LINE_WIDTH * 2.5f);
            polylineOptions.color(lineColor);
            polylineOptions.addAll(points);
            polylineOptions.zIndex(MapImageSettingUtils.ZINDEX_GEOMETRY);
            trackerLine = googleMap.addPolyline(polylineOptions);
        }
    }
    //endregion

    //region point
    private ArrayList<Marker> pointMarkers = new ArrayList<>();

    private void drawPoint() {
        if (startDrawable != null) {
            Bitmap bitmap = MapBitmapUtils.drawableToBitmap(startDrawable);
            if (bitmap != null) {
                if (source.size() > 0) {
                    String title = "marker-" + 0;
                    LatLng point_start = new LatLng(source.get(0).getLat(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle),
                            source.get(0).getLng(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle));
                    BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                    MarkerOptions markerOption = new MarkerOptions().icon(descriptor)
                            .position(point_start)
                            .title(title)
                            .anchor(0.5f, 0.5f)
                            .draggable(false)
                            .zIndex(MapConstantUtils.ZINDEX_MARKER);
                    Marker marker = googleMap.addMarker(markerOption);
                    pointMarkers.add(marker);
                }
            }
        }
        if (endDrawable != null) {
            Bitmap bitmap = MapBitmapUtils.drawableToBitmap(endDrawable);
            if (bitmap != null) {
                if (source.size() > 1) {
                    String title = "marker-" + (source.size() - 1);
                    LatLng point_end = new LatLng(source.get(source.size() - 1).getLat(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle),
                            source.get(source.size() - 1).getLng(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle));
                    BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                    MarkerOptions markerOption = new MarkerOptions().icon(descriptor)
                            .position(point_end)
                            .title(title)
                            .anchor(0.5f, 0.5f)
                            .draggable(false)
                            .zIndex(MapConstantUtils.ZINDEX_MARKER);
                    Marker marker = googleMap.addMarker(markerOption);
                    pointMarkers.add(marker);
                }
            }
        }

    }
    //endregion

    //region arrow point
    private ArrayList<Marker> arrowMakers = new ArrayList<>();

    protected void drawArrowPoint() {
        clearArrowMaker();
        if (source.size() > 1) {
            ArrayList<Point> graphicPoints = new ArrayList<>();
            for (int i = 0; i < source.size(); i++) {
                LatLngBean location = source.get(i);
                double lat = location.getLat(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
                double lng = location.getLng(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
                if (MapUtils.validLatLng(lat, lng)) {
                    Point point = googleMap.getProjection().toScreenLocation(new LatLng(lat, lng));
                    graphicPoints.add(point);
                }
            }
//            ArrayList<LatLngBean> points = AverageLineUtils.getArrowLine(source);
            ArrayList<LatLngBean> points = new ArrayList<>();
            for (int i = 0; i < graphicPoints.size() - 1; i++) {
                Point point = graphicPoints.get(i);
                Point point_last = graphicPoints.get(i + 1);
                LatLng latlng1 = googleMap.getProjection().fromScreenLocation(point);
                LatLng latlng2 = googleMap.getProjection().fromScreenLocation(point_last);
                double length = (point_last.x - point.x) * (point_last.x - point.x) + (point_last.y - point.y) * (point_last.y - point.y);
                length = Math.sqrt(length);
                if (length > 80f) {
                    double centerX = ((point_last.x + point.x) / 2d);
                    double centerY = ((point_last.y + point.y) / 2d);
                    Point point_center = new Point(new Double(centerX).intValue(), new Double(centerY).intValue());
                    LatLng center = googleMap.getProjection().fromScreenLocation(point_center);
                    points.add(new LatLngBean(center.latitude, center.longitude));
                    points.add(new LatLngBean(latlng2.latitude, latlng2.longitude));//方向索引，不进行显示
                }
            }
            for (int i = 0; i < points.size() - 1; i++) {
                if (i % 2 == 0) {
                    LatLngBean point = points.get(i);
                    LatLngBean point_last = points.get(i + 1);
                    double rotate = TurfMeasurementUtils.bearing(point, point_last) - 90d;
                    LatLng point_start = new LatLng(point.getLat(), point.getLng());
                    BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.line_arrow);
                    MarkerOptions markerOption = new MarkerOptions().icon(descriptor)
                            .position(point_start)
                            .title("")
                            .rotation(Float.parseFloat(String.valueOf(rotate)))
                            .anchor(0.5f, 0.5f)
                            .draggable(false)
                            .zIndex(MapConstantUtils.ZINDEX_ARROW);
                    Marker marker = googleMap.addMarker(markerOption);
                    marker.setVisible(layerVisible);
                    arrowMakers.add(marker);
                }
            }
        }
    }

    private void clearArrowMaker() {
        for (Marker marker : arrowMakers) {
            marker.remove();
        }
        arrowMakers.clear();
    }
    //endregion

    //region play line
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
                                if (googleMap != null) {
                                    drawPlayLine(progressLatLng);
                                    drawPlayPoint(progressLatLng);
                                }
                                preLatLng = progressLatLng;
                            }
                        }
                    };
                    LatLngBean nextLocation = newRoute.get(counter + 1);
                    if (markerIconAnimator != null && markerIconAnimator.isStarted()) {
                        markerIconCurrentLocation = (LatLng) markerIconAnimator.getAnimatedValue();
                        markerIconAnimator.cancel();
                    }
                    LatLng firstLocation = new LatLng(newRoute.get(counter).getLat(), newRoute.get(counter).getLng());
                    markerIconAnimator = ObjectAnimator
                            .ofObject(new LatLngEvaluator(), counter == 0 || markerIconCurrentLocation == null
                                            ? firstLocation
                                            : markerIconCurrentLocation,
                                    new LatLng(nextLocation.getLat(), nextLocation.getLng()))
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
    List<LatLngBean> newRoute = new ArrayList<>();

    protected void setRouteData() {
        List<LatLngBean> points = new ArrayList<>();
        if (source.size() > 1) {
            for (LatLngBean latLng : source) {
                double lat = latLng.getLat(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
                double lng = latLng.getLng(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
                if (MapUtils.validLatLng(lat, lng)) {
                    points.add(new LatLngBean(lat, lng));
                }
            }
            aLength = points.size();
            newRoute = resetRouteSource(points, 1000, TurfConstantsUtils.UNIT_KILOMETERS);
            steps = newRoute.size();
        }
    }

    private List<LatLngBean> resetRouteSource(List<LatLngBean> route, int nstep, String units) {
        List<LatLngBean> newroute = new ArrayList<>();
        double lineDistance = TurfMeasurementUtils.length(route, units);
        double nDistance = lineDistance / nstep;
        for (int i = 0; i < route.size() - 1; i++) {
            LatLngBean from = route.get(i);
            LatLngBean to = route.get(i + 1);
            double lDistance = TurfMeasurementUtils.distance(from, to, units);
            if (i == 0) {
                newroute.add(route.get(0));
            }
            if (lDistance > nDistance) {
                List<LatLngBean> rings = lineMore(from, to, lDistance, nDistance, units);
                newroute.addAll(rings);
            } else {
                newroute.add(route.get(i + 1));
            }
        }
        return newroute;
    }

    private List<LatLngBean> lineMore(LatLngBean from, LatLngBean to, double distance, double splitLength, String units) {
        List<LatLngBean> route = new ArrayList<>();
        route.add(from);
        route.add(to);
        //
        int step = (int) (distance / splitLength);
        double leftLength = distance - step * splitLength;
        List<LatLngBean> rings = new ArrayList<>();
        for (int i = 1; i <= step; i++) {
            double nlength = i * splitLength;
            LatLngBean pnt = TurfMeasurementUtils.along(route, nlength, units);
            rings.add(pnt);
        }
        if (leftLength > 0) {
            rings.add(to);
        }
        return rings;
    }

    //region play point
    private Marker playMarker;
    private LatLng preLatLng;

    protected void drawPlayPoint(LatLng latLng) {
        if (preLatLng != null) {
            float bearing = getBearing(preLatLng, latLng);
            Log.i(TAG, bearing + "");
            if (playMarker != null) {
                playMarker.setPosition(latLng);
                playMarker.setRotation(bearing);
            } else {
                BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.icon_move_position);
                MarkerOptions markerOption = new MarkerOptions().icon(descriptor)
                        .position(latLng)
                        .title("")
                        .anchor(0.5f, 0.5f)
                        .rotation(bearing)
                        .draggable(false)
                        .zIndex(MapConstantUtils.ZINDEX_MARKER);
                playMarker = googleMap.addMarker(markerOption);
            }
        }
    }

    private float getBearing(LatLng from, LatLng to) {
        float bear = (float) TurfMeasurementUtils.bearing(LatLngUtils.mapLatLngParse(from),
                LatLngUtils.mapLatLngParse(to));
        bear -= googleMap.getCameraPosition().bearing;
        return bear;
    }
    //endregion

    //region play line
    private List<LatLng> playLines = new ArrayList<>();
    private Polyline playLine;

    protected int playLineColor = MapImageSettingUtils.getFencePolylineBorderColor(context);

    protected void drawPlayLine(LatLng endPoint) {
        playLines.add(endPoint);
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(MapImageSettingUtils.FENCE_LINE_WIDTH * 2.5f);
        polylineOptions.color(playLineColor);
        polylineOptions.addAll(playLines);
        polylineOptions.zIndex(MapImageSettingUtils.ZINDEX_GEOMETRY);
        playLine = googleMap.addPolyline(polylineOptions);
    }

    protected void clearPlayTracker() {
        playLines.clear();
        newRoute.clear();
        playTracker(false);
        this.counter = 0;
        this.steps = 0;
        this.aLength = 0;
        if (playMarker != null) {
            playMarker.remove();
        }
        if (playLine != null) {
            playLine.remove();
        }
    }
    //endregion
    //endregion

    protected void onDestroyLayer() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
