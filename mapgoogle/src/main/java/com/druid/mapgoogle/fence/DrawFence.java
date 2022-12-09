package com.druid.mapgoogle.fence;

import android.content.Context;
import android.text.TextUtils;

import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.bean.FenceType;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.fence.layer.FenceListLayer;
import com.druid.mapcore.fence.view.marker.FenceTitleMarkView;
import com.druid.mapcore.interfaces.FenceClickListener;
import com.druid.mapcore.setting.MapSetting;
import com.druid.mapcore.utils.MapUtils;
import com.druid.mapcore.utils.turf.TurfConstantsUtils;
import com.druid.mapcore.utils.turf.TurfTransformationUtils;
import com.druid.mapgoogle.utils.MapImageSettingUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class DrawFence extends FenceListLayer {
    protected MapView mapView = null;
    protected GoogleMap googleMap = null;

    public DrawFence(Context context) {
        super(context);
    }

    protected ArrayList<LatLng> centers = new ArrayList<>();
    private HashMap<Circle, FenceCoreBean> circles = new HashMap<>();
    private HashMap<Polygon, FenceCoreBean> polygons = new HashMap<>();
    private ArrayList<Marker> textMarkers = new ArrayList<>();

    protected void drawFence() {
        removePolygon();
        centers.clear();
        circles.clear();
        polygons.clear();
        textMarkers.clear();
        drawFencePolygonLayer();
    }

    private void removePolygon(){
        for (HashMap.Entry<Polygon, FenceCoreBean> polygonsEntry : polygons.entrySet()) {
            Polygon polygon = polygonsEntry.getKey();
            polygon.remove();
        }
        for (HashMap.Entry<Circle, FenceCoreBean> circlesEntry : circles.entrySet()) {
            Circle circle = circlesEntry.getKey();
            circle.remove();
        }
        for(Marker marker:textMarkers){
            marker.remove();
        }
    }

    private void drawFencePolygonLayer() {
        for (int i = 0; i < source.size(); i++) {
            FenceCoreBean fence = source.get(i);
            if (fence.type.equals(FenceType.Round)) {
                addDrawCircle(fence);
            } else {
                addDrawPolygon(fence);
            }
        }
    }

    private void addDrawCircle(FenceCoreBean fence) {
        int radius = fence.distance;
        LatLng center = new LatLng(fence.points.get(0)[0], fence.points.get(0)[1]);
        centers.add(center);
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.radius(radius);
        circleOptions.center(center);
        circleOptions.zIndex(MapSetting.ZINDEX_GEOMETRY);
        circleOptions.fillColor(MapImageSettingUtils.getFencePolygonFillColor(context));
        circleOptions.strokeWidth(MapImageSettingUtils.FENCE_GOOGLE_LINE_WIDTH);
        circleOptions.strokeColor(MapImageSettingUtils.getFencePolygonBorderColor(context));
        Circle circle = googleMap.addCircle(circleOptions);
        circle.setTag(fence);
        circle.setClickable(true);
        circles.put(circle, fence);
        drawFenceTitleManager(center, fence);
    }

    private void addDrawPolygon(FenceCoreBean fence) {
        ArrayList<LatLngBean> latLngSources = new ArrayList<>();
        ArrayList<LatLng> latLngs = new ArrayList<>();
        ArrayList<double[]> points = fence.points;
        if (points.size() > 0) {
            PolygonOptions polygonOptions = new PolygonOptions();
            for (int i = 0; i < points.size(); i++) {
                double[] doubles = points.get(i);
                LatLngBean latLngSource = new LatLngBean(0,doubles[0], doubles[1]);
                latLngSources.add(latLngSource);
                LatLng latLng = new LatLng(doubles[0], doubles[1]);
                latLngs.add(latLng);
            }
            polygonOptions.addAll(latLngs);
            polygonOptions.zIndex(MapSetting.ZINDEX_GEOMETRY);
            polygonOptions.fillColor(MapImageSettingUtils.getFencePolygonFillColor(context));
            polygonOptions.strokeWidth(MapImageSettingUtils.FENCE_GOOGLE_LINE_WIDTH);
            polygonOptions.strokeColor(MapImageSettingUtils.getFencePolygonBorderColor(context));

            Polygon polygon = googleMap.addPolygon(polygonOptions);
            polygon.setTag(fence);
            polygon.setClickable(true);
            polygons.put(polygon, fence);
            LatLngBean centerSource = MapUtils.getCenterOfGravityPoint(latLngSources);
            LatLng center = new LatLng(centerSource.getLat(), centerSource.getLng());
            centers.add(center);
            drawFenceTitleManager(center, fence);
        }
    }

    private void drawFenceTitleManager(LatLng center, FenceCoreBean fence) {
        FenceTitleMarkView titleView = new FenceTitleMarkView(context).setFenceBean(fence);
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(titleView.getBitmapView());
        String title = "name-" + fence.name;
        MarkerOptions markerOption = new MarkerOptions().icon(descriptor)
                .position(center)
                .title(title)
                .draggable(false)
                .zIndex(MapSetting.ZINDEX_GEOMETRY_MARKER);
        Marker marker = googleMap.addMarker(markerOption);
        textMarkers.add(marker);
    }

    //
    private String click_id = "";
    private Circle preCircle = null;
    private Polygon prePolygon = null;

    protected void queryFenceClick(Object object) {
        if (object instanceof FenceCoreBean) {
            FenceCoreBean fenceSelected = (FenceCoreBean) object;
            if (fenceSelected != null) {
                ArrayList<FenceCoreBean> selectFences = fenceClick(fenceSelected.id);
            }
        }
    }

    private ArrayList<FenceCoreBean> fenceClick(String fence_id) {
        ArrayList<FenceCoreBean> selectFences = new ArrayList<>();
        try {
            //遍历多边形
            for (HashMap.Entry<Polygon, FenceCoreBean> polygonsEntry : polygons.entrySet()) {
                Polygon polygon = polygonsEntry.getKey();
                FenceCoreBean fence = polygonsEntry.getValue();
                if (fence_id.equals(fence.id)) {
                    selectFences.add(fence);
                    clearFenceSelected();
                    this.prePolygon = polygon;
                    this.prePolygon.setFillColor(MapImageSettingUtils.getFencePolygonBorderColor(context));
                }
            }
            //遍历圆形
            for (HashMap.Entry<Circle, FenceCoreBean> circlesEntry : circles.entrySet()) {
                Circle circle = circlesEntry.getKey();
                FenceCoreBean fence = circlesEntry.getValue();
                if (fence_id.equals(fence.id)) {
                    selectFences.add(fence);
                    clearFenceSelected();
                    this.preCircle = circle;
                    this.preCircle.setFillColor(MapImageSettingUtils.getFencePolygonBorderColor(context));
                }
            }
            //
            if (selectFences.size() > 0) {
                if (selectFences.size() == 1) {
                    FenceCoreBean fence = selectFences.get(0);
                    if (!TextUtils.isEmpty(fence.id)) {
                        this.click_id = fence.id;
                        if (fenceClickListener != null) {
                            fenceClickListener.fenceOnClick(click_id);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return selectFences;
    }

    protected void clearFenceSelected() {
        this.click_id = "";
        if (preCircle != null) {
            preCircle.setFillColor(MapImageSettingUtils.getFencePolygonFillColor(context));
        }
        if (prePolygon != null) {
            prePolygon.setFillColor(MapImageSettingUtils.getFencePolygonFillColor(context));
        }
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
            fenceClick(click_id);
            zoomCenterFence(source.get(index));
        }
    }

    private void zoomCenterFence(FenceCoreBean fence) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds latLngBounds = null;
        if (fence.type.equals(FenceType.Round)) {
            LatLngBean center = new LatLngBean(0,fence.points.get(0)[0], fence.points.get(0)[1]);
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
        googleMap.animateCamera(cameraUpdate);
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
                    googleMap.animateCamera(mapStatusUpdate);
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
