package com.druid.mapgaode.fence.polygon;

import android.content.Context;
import android.text.TextUtils;

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
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.druid.mapbaidu.R;
import com.druid.mapcore.bean.FencePolygonPointsBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.fence.layer.polygon.FenceDrawPolygonLayer;
import com.druid.mapcore.interfaces.FencePointMarkerClickListener;
import com.druid.mapcore.utils.MapSettingConstantUtils;
import com.druid.mapcore.utils.MapUtils;
import com.druid.mapgaode.utils.LatLngUtils;
import com.druid.mapgaode.utils.MapConstantUtils;
import com.druid.mapgaode.utils.MapImageSettingUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class DrawFencePolygon extends FenceDrawPolygonLayer<Marker> {
    protected int minSize = 3;
    protected FencePolygonPointsBean fencePoints = new FencePolygonPointsBean();
    protected MapView mapView = null;
    protected AMap aMap = null;

    public DrawFencePolygon(Context context) {
        super(context);
    }

    public void createFenceSource() {

    }

    private LatLngBounds fenceLayerLatLngBounds = null;
    private List<Marker> pointMarkers = new ArrayList<>();
    private List<Polyline> pointLines = new ArrayList<>();
    private Polygon fencePolygon = null;

    private void updateDrawFencePoint() {
        for (Marker marker : pointMarkers) {
            marker.remove();
        }
        pointMarkers.clear();
        Iterator<Map.Entry<Integer, LatLngBean>> it = fencePoints.getFencePointMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, LatLngBean> entry = it.next();
            LatLngBean point = entry.getValue();
            int index = entry.getKey();
            double lat = point.getLat();
            double lng = point.getLng();
            if (MapUtils.validLatLng(lat, lng)) {
                BitmapDescriptor descriptor = null;
                if (index == 0) {
                    descriptor = BitmapDescriptorFactory.fromResource(R.drawable.fence_point_start);
                } else {
                    descriptor = BitmapDescriptorFactory.fromResource(R.drawable.fence_point_end);
                }
                MarkerOptions markerOption = new MarkerOptions().icon(descriptor)
                        .position(LatLngUtils.mapLatLngFrom(point))
                        .zIndex(MapConstantUtils.ZINDEX_MARKER)
                        .draggable(false);
                Marker marker = aMap.addMarker(markerOption);
                marker.setTitle(MapConstantUtils.getMarkerTitle(false, index));
                pointMarkers.add(marker);
            }
        }
    }

    private void updateDrawFenceLine(boolean drawEndLine) {
        for (Polyline polyline : pointLines) {
            polyline.remove();
        }
        pointLines.clear();
        List<LatLng> mapPoints = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        List<LatLngBean> points = fencePoints.getFencePointList(editorFencePointIndex, tempClickPoint);
        for (int index = 0; index < points.size(); index++) {
            LatLngBean point = points.get(index);
            double lat = point.getLat();
            double lng = point.getLng();
            if (MapUtils.validLatLng(lat, lng)) {
                LatLng point_use = LatLngUtils.mapLatLngFrom(point);
                builder.include(point_use);
                mapPoints.add(point_use);
                if (index != 0) {
                    LatLngBean prePoint = points.get(index - 1);
                    LatLng point_pre = LatLngUtils.mapLatLngFrom(prePoint);
                    List<LatLng> linePoints = new ArrayList<>();
                    linePoints.add(point_pre);
                    linePoints.add(point_use);
                    //
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.width(MapImageSettingUtils.FENCE_GOOGLE_LINE_WIDTH);
                    polylineOptions.color(MapImageSettingUtils.getFencePolylineBorderColor(context));
                    polylineOptions.addAll(linePoints);
                    polylineOptions.zIndex(MapConstantUtils.ZINDEX_GEOMETRY);
                    Polyline polyline = aMap.addPolyline(polylineOptions);
                    pointLines.add(polyline);
                }
            }
        }
        if (mapPoints.size() > 1) {
            fenceLayerLatLngBounds = builder.build();
        }
        //绘制结束线
        if (drawEndLine) {
            List<LatLng> linePoints = new ArrayList<>();
            linePoints.add(mapPoints.get(mapPoints.size() - 1));
            linePoints.add(mapPoints.get(0));
            //
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.width(MapImageSettingUtils.FENCE_GOOGLE_LINE_WIDTH);
            polylineOptions.color(MapImageSettingUtils.getFencePolylineBorderColor(context));
            polylineOptions.addAll(linePoints);
            polylineOptions.zIndex(MapConstantUtils.ZINDEX_GEOMETRY);
            Polyline polyline = aMap.addPolyline(polylineOptions);
            pointLines.add(polyline);
        }
    }

    public void updateDrawFence(boolean drawEndLine) {
        updateDrawFenceLine(drawEndLine);
        updateDrawFencePoint();
    }

    public boolean completeDrawFence(boolean needMoveCamera) {
        if (fencePolygon != null) {
            fencePolygon.remove();
            fencePolygon = null;
        }
        if (this.editorFencePointIndex != -1) {
            if (fencePoints.size() < 2) {
                return false;
            }
        } else {
            if (fencePoints.size() < 3) {
                return false;
            }
        }
        updateDrawFence(true);
        //
        List<LatLng> mapPoints = new ArrayList<>();
        List<LatLngBean> points = fencePoints.getFencePointList(editorFencePointIndex, tempClickPoint);
        for (int i = 0; i < points.size(); i++) {
            LatLngBean point = points.get(i);
            double lat = point.getLat();
            double lng = point.getLng();
            if (MapUtils.validLatLng(lat, lng)) {
                LatLng point_use = LatLngUtils.mapLatLngFrom(point);
                mapPoints.add(point_use);
            }
        }
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(mapPoints);
        polygonOptions.strokeWidth(MapImageSettingUtils.FENCE_GOOGLE_LINE_WIDTH)
                .strokeColor(MapImageSettingUtils.getFencePolygonBorderColor(context))
                .fillColor(MapImageSettingUtils.getFencePolygonFillColor(context))
                .zIndex(MapConstantUtils.ZINDEX_GEOMETRY);
        fencePolygon = aMap.addPolygon(polygonOptions);
        //
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
        for (Marker marker : pointMarkers) {
            marker.remove();
        }
        pointMarkers.clear();
        for (Polyline polyline : pointLines) {
            polyline.remove();
        }
        pointLines.clear();
        if (fencePolygon != null) {
            fencePolygon.remove();
            fencePolygon = null;
        }
        this.editorFencePointIndex = -1;
        this.tempClickPoint = null;
    }

    protected void queryMarkerClick(Marker marker) {
        String title = marker.getTitle();
        if (!TextUtils.isEmpty(title)) {
            if (MapConstantUtils.getMarkerISNormalFromTitle(title)) {
                int click_index = MapConstantUtils.getMarkerIndexFromTitle(title);
                if (click_index != -1) {
                    if (editorFencePointIndex != -1 && tempClickPoint != null) {
                        fencePoints.addFencePoint(editorFencePointIndex, tempClickPoint);
                    }
                    editorFencePoint(click_index);
                }
            }
        }
    }

    private int editorFencePointIndex = -1;
    private LatLngBean tempClickPoint;

    private void editorFencePoint(int index) {
        LatLngBean removeLatLng = fencePoints.removeFencePoint(index);
        if (removeLatLng != null) {
            this.editorFencePointIndex = index;
            tempClickPoint = removeLatLng;
            updateDrawFencePoint();
            cameraMoveToFencePoint(LatLngUtils.mapLatLngFrom(tempClickPoint));
        }
    }

    public void cameraFlatMoveToPoint(LatLngBean latLngBean) {
        LatLng latLng = new LatLng(latLngBean.getLat(), latLngBean.getLng());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(aMap.getCameraPosition().zoom)
                .build();
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void cameraMoveToFencePoint(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(aMap.getCameraPosition().zoom)
                .build();
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 200,
                new AMap.CancelableCallback() {
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

    protected boolean completeEditorFencePoint() {
        if (editorFencePointIndex != -1) {
            fencePoints.addFencePoint(editorFencePointIndex, LatLngUtils.mapLatLngParse(aMap.getCameraPosition().target));
            this.editorFencePointIndex = -1;
            this.tempClickPoint = null;
            completeDrawFence(true);
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
                completeDrawFence(true);
                return true;
            }
        }
        return false;
    }

    protected void cameraToFenceBounds() {
        if (fenceLayerLatLngBounds != null) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(fenceLayerLatLngBounds, MapSettingConstantUtils.MAP_BOUNDS_PADDING));
        }
    }

    private FencePointMarkerClickListener fencePointMarkerClickListener;

    public void setFencePointMarkerClickListener(FencePointMarkerClickListener listener) {
        this.fencePointMarkerClickListener = listener;
    }
}
