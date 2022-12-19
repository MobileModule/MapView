package com.druid.mapgaode;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.druid.mapbaidu.R;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.HeatMapSetBean;
import com.druid.mapcore.bean.ImageRegisterBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.LocationBean;
import com.druid.mapcore.capture.CaptureDrawLayerApi;
import com.druid.mapcore.circle.CircleDrawLayerApi;
import com.druid.mapcore.cluster.ClusterLayer;
import com.druid.mapcore.fence.api.FenceListLayerApi;
import com.druid.mapcore.fence.api.circle.FenceDrawCircleLayerApi;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByAutoLocationLayer;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByHandLayer;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByInputLayer;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByManualLocationLayer;
import com.druid.mapcore.fence.api.rectangle.FenceDrawRectangleLayerApi;
import com.druid.mapcore.heatmap.HeatMapLayerApi;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.PolygonClickListener;
import com.druid.mapcore.layer.LayerApi;
import com.druid.mapcore.line.LineDrawLayerApi;
import com.druid.mapcore.navigate.NavigateLayerApi;
import com.druid.mapcore.utils.PermissionListener;
import com.druid.mapgaode.circle.DefineCircleLayer;
import com.druid.mapgaode.fence.DrawFenceList;
import com.druid.mapgaode.fence.circle.DrawFenceCircle;
import com.druid.mapgaode.fence.polygon.DrawFencePolygonByAutoLocation;
import com.druid.mapgaode.fence.polygon.DrawFencePolygonByManualLocation;
import com.druid.mapgaode.fence.polygon.DrawFencePolygonHand;
import com.druid.mapgaode.fence.polygon.DrawFencePolygonInput;
import com.druid.mapgaode.fence.rectangle.DrawFenceRectangle;
import com.druid.mapgaode.line.DefineLineLayer;
import com.druid.mapgaode.utils.LatLngUtils;
import com.druid.mapgaode.utils.MapConstantUtils;
import com.druid.mapgaode.cluster.MapGoogleClusterLayer;
import com.druid.mapgaode.heatmap.GaodeMapHeatMapLayer;
import com.druid.mapgaode.location.LocationMarkerLayer;
import com.druid.mapgaode.location.MapGoogleLocationEngine;
import com.druid.mapgaode.navigate.DefineNavigateLayer;

import java.util.ArrayList;
import java.util.List;

public class MapGaodeView extends LinearLayout implements DruidMapView,
        AMap.OnMapClickListener, MapLocationChangedListener,
        AMap.OnInfoWindowClickListener, AMap.OnMarkerClickListener{
    public static final String TAG = MapGaodeView.class.getName();

    public MapGaodeView(Context context) {
        super(context);
        initView();
    }

    public MapGaodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTypedArray(context, attrs);
        initView();
    }

    public MapGaodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs);
        initView();
    }

    void initTypedArray(Context context, AttributeSet attrs) {
        if (attrs != null) {

        }
    }

    private RelativeLayout parentView;
    private MapView mapView = null;
    private AMap aMap = null;
    private List<LayerApi> layerApis = new ArrayList<>();

    void initView() {
        inflate(getContext(), R.layout.mapgaode_view, this);
        parentView = findViewById(R.id.parentView);
        mapView = findViewById(R.id.mapView);
    }

    @Override
    public void registerMapMarkerImages(ArrayList<ImageRegisterBean> images) {
        //todo
    }

    @Override
    public void loadingMap(MapLoadedListener loadedListener) {
        addLoadedListener(loadedListener);
        onMapReady(mapView.getMap());
    }

    private boolean enableGesture = true;

    @Override
    public void enableGesture(boolean enable) {
        this.enableGesture = enable;
    }

    public void onMapReady(@NonNull AMap aMap) {
        this.aMap = aMap;
        this.aMap.setOnMapClickListener(this);
        this.aMap.setOnCameraChangeListener(cameraIdleListener);
        resetMarkerMainClickEvent();
        MapConstantUtils.configMapUISetting(this.aMap);
        MapConstantUtils.enableGesture(this.aMap, enableGesture);
        onMapLoaded();
    }

    @Override
    public void resetMarkerMainClickEvent() {
        this.aMap.setOnMarkerClickListener(this::onMarkerClick);
        this.aMap.setOnInfoWindowClickListener(this);
    }

    boolean reloadedStyle = false;

    public void onMapLoaded() {
        for (LayerApi layerApi : layerApis) {
            layerApi.bindMap(mapView, aMap);
            layerApi.attachDruidMap(this);
        }
        for (MapLoadedListener loadedListener : mapLoadedListeners) {
            loadedListener.mapReadyLoad(reloadedStyle);
        }
        if (!this.reloadedStyle) {
            this.reloadedStyle = true;
        }
        if (locationEngine != null) {
            locationEngine.onResume();
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        for (MapClickListener clickListener : mapClickListeners) {
            clickListener.mapClick(new LatLngBean(0, latLng.latitude, latLng.longitude));
        }
    }

    @Override
    public LatLngBean getCameraTarget() {
        LatLng latLng = aMap.getCameraPosition().target;
        return new LatLngBean(0, latLng.latitude, latLng.longitude);
    }

    @Override
    public void setMapLanguage(String lan) {

    }

    private LocationMarkerLayer locationMarkerLayer;
    private boolean loadMineLocation = false;

    @Override
    public void locationMineEnable(String permissionId, PermissionListener permissionListener, boolean firstLocationMine) {
        if (locationMarkerLayer == null) {
            loadMineLocation = firstLocationMine;
            locationEngineEnable(0);
            locationMarkerLayer = new LocationMarkerLayer(getContext());
            //map loaded execute
            locationMarkerLayer.bindMap(mapView, aMap);
            addLocationChangedListener(locationMarkerLayer.getMapLocationChangedListener());
        }
    }

    @Override
    public boolean cameraMineLocation() {
        if (locationMarkerLayer != null) {
            if (locationMarkerLayer.getLocationPosition() != null) {
                animationCameraToLatLng(LatLngUtils.mapLatLngFrom(locationMarkerLayer.getLocationPosition()), 14f);
            }
        }
        return true;
    }

    private MapGoogleLocationEngine locationEngine;

    @Override
    public void locationEngineEnable(long intervalTime) {
        if (locationEngine == null) {
            locationEngine = new MapGoogleLocationEngine(getContext(), intervalTime);
            locationEngine.setLocationChangedListener(this);
        }
    }

    @Override
    public void locationChanged(LocationBean location) {
        for (MapLocationChangedListener listener : mapLocationChangedListeners) {
            listener.locationChanged(location);
        }
        if (loadMineLocation) {
            loadMineLocation = false;
            cameraAnimationLocationLatLng(new LatLngBean(0, location.position.getLat(), location.position.getLng()));
        }
    }

    @Override
    public void cameraAnimationLocationLatLng(LatLngBean latLngBean) {
        LatLng latLng = new LatLng(latLngBean.getLat(), latLngBean.getLng());
        animationCameraToLatLng(latLng, 12);
    }

    void animationCameraToLatLng(LatLng latLng, float zoom) {
        CameraPosition animatedPosition = new CameraPosition.Builder()
                .target(latLng)
                .tilt(0)
                .zoom(zoom)
                .bearing(0)
                .build();
        aMap.animateCamera(CameraUpdateFactory.newCameraPosition(animatedPosition));
    }

    private boolean mapTile = false;//是否是瓦片服务

    @Override
    public void changeMapSourceStyleStreet() {
        if (aMap != null) {
            this.mapTile = false;
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        }
    }

    @Override
    public void changeMapSourceStyleSatellite() {
        if (aMap != null) {
            this.mapTile = true;
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        }
    }

    @Override
    public boolean getStatusMapTile() {
        return mapTile;
    }

    @Override
    public void zoomMap(boolean isOut) {
        float zoom = aMap.getCameraPosition().zoom;
        if (isOut) {
            zoom += 2;
        } else {
            zoom -= 2;
        }
        aMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    //region layer
    GaodeMapHeatMapLayer heatMapLayer = null;

    @Override
    public HeatMapLayerApi drawHeatMapLayer(HeatMapSetBean set) {
        if (heatMapLayer == null) {
            this.heatMapLayer = new GaodeMapHeatMapLayer(getContext());
            this.heatMapLayer.setHeatMapSet(set);
            layerApis.add(heatMapLayer);
            loadHeatMapLayerEvent();
        }
        return heatMapLayer;
    }

    void loadHeatMapLayerEvent() {
        if (heatMapLayer != null) {
            addLoadedListener(heatMapLayer.getMapLoadedListener());
        }
    }

    MapGoogleClusterLayer clusterLayer;

    @Override
    public ClusterLayer drawClusterLayer() {
        if (clusterLayer == null) {
            clusterLayer = new MapGoogleClusterLayer(getContext());
            layerApis.add(clusterLayer);
            loadClusterLayerEvent();
        }
        return clusterLayer;
    }

    void loadClusterLayerEvent() {
        if (clusterLayer != null) {
            addMapClickListener(clusterLayer.getMapClickListener());
            addLoadedListener(clusterLayer.getMapLoadedListener());
            addOnCameraIdleListener(clusterLayer.getMapCameraIdleListener());
            addOnMarkerClickListeners(clusterLayer.getMapMarkerClickListener());
            addOnInfoWindowClickListener(clusterLayer.getMapInfoWindowClickListener());
        }
    }
    //endregion

    //region fence layer
    DrawFencePolygonHand fencePolygonHandLayer;

    @Override
    public FenceDrawPolygonByHandLayer drawFencePolygonByHandLayer() {
        if (fencePolygonHandLayer == null) {
            fencePolygonHandLayer = new DrawFencePolygonHand(getContext(), parentView);
            layerApis.add(fencePolygonHandLayer);
            loadFenceHandLayerEvent();
        }
        return fencePolygonHandLayer;
    }

    void loadFenceHandLayerEvent() {
        if (fencePolygonHandLayer != null) {
            addLoadedListener(fencePolygonHandLayer.getMapLoadedListener());
            addMapClickListener(fencePolygonHandLayer.getMapClickListener());
            addOnCameraIdleListener(fencePolygonHandLayer.getMapCameraIdleListener());
            addOnMarkerClickListeners(fencePolygonHandLayer.getMapMarkerClickListener());
        }
    }

    DrawFencePolygonByAutoLocation fencePolygonByAutoLocationLayer;

    @Override
    public FenceDrawPolygonByAutoLocationLayer drawFencePolygonByAutoLocationLayer() {
        if (fencePolygonByAutoLocationLayer == null) {
            fencePolygonByAutoLocationLayer = new DrawFencePolygonByAutoLocation(getContext());
            layerApis.add(fencePolygonByAutoLocationLayer);
            loadFenceAutoLocationLayerEvent();
            locationEngineEnable(0);
        }
        return fencePolygonByAutoLocationLayer;
    }

    void loadFenceAutoLocationLayerEvent() {
        if (fencePolygonByAutoLocationLayer != null) {
            addLoadedListener(fencePolygonByAutoLocationLayer.getMapLoadedListener());
            addMapClickListener(fencePolygonByAutoLocationLayer.getMapClickListener());
            addOnCameraIdleListener(fencePolygonByAutoLocationLayer.getMapCameraIdleListener());
            addLocationChangedListener(fencePolygonByAutoLocationLayer.getLocationChangedListener());
            addOnMarkerClickListeners(fencePolygonByAutoLocationLayer.getMapMarkerClickListener());
        }
    }

    DrawFencePolygonByManualLocation fencePolygonByManualLocationLayer;

    @Override
    public FenceDrawPolygonByManualLocationLayer drawFencePolygonByManualLocationLayer() {
        if (fencePolygonByManualLocationLayer == null) {
            fencePolygonByManualLocationLayer = new DrawFencePolygonByManualLocation(getContext());
            layerApis.add(fencePolygonByManualLocationLayer);
            loadFenceManualLocationLayerEvent();
            locationEngineEnable(0);

        }
        return fencePolygonByManualLocationLayer;
    }

    void loadFenceManualLocationLayerEvent() {
        if (fencePolygonByManualLocationLayer != null) {
            addLoadedListener(fencePolygonByManualLocationLayer.getMapLoadedListener());
            addMapClickListener(fencePolygonByManualLocationLayer.getMapClickListener());
            addOnCameraIdleListener(fencePolygonByManualLocationLayer.getMapCameraIdleListener());
            addLocationChangedListener(fencePolygonByManualLocationLayer.getLocationChangedListener());
            addOnMarkerClickListeners(fencePolygonByManualLocationLayer.getMapMarkerClickListener());
        }
    }

    DrawFencePolygonInput fencePolygonInputLayer;

    @Override
    public FenceDrawPolygonByInputLayer drawFencePolygonByInputLayer() {
        if (fencePolygonInputLayer == null) {
            fencePolygonInputLayer = new DrawFencePolygonInput(getContext());
            layerApis.add(fencePolygonInputLayer);
            loadFenceInputLayerEvent();
        }
        return fencePolygonInputLayer;
    }

    void loadFenceInputLayerEvent() {
        if (fencePolygonInputLayer != null) {
            addLoadedListener(fencePolygonInputLayer.getMapLoadedListener());
            addMapClickListener(fencePolygonInputLayer.getMapClickListener());
            addOnCameraIdleListener(fencePolygonInputLayer.getMapCameraIdleListener());
            addOnMarkerClickListeners(fencePolygonInputLayer.getMapMarkerClickListener());
        }
    }

    DrawFenceCircle fenceCircleLayer;

    @Override
    public FenceDrawCircleLayerApi drawFenceCircleLayer() {
        if (fenceCircleLayer == null) {
            fenceCircleLayer = new DrawFenceCircle(getContext(), parentView);
            layerApis.add(fenceCircleLayer);
            loadFenceCircleLayerEvent();
        }
        return fenceCircleLayer;
    }

    void loadFenceCircleLayerEvent() {
        if (fenceCircleLayer != null) {
            addLoadedListener(fenceCircleLayer.getMapLoadedListener());
            addMapClickListener(fenceCircleLayer.getMapClickListener());
            addOnCameraIdleListener(fenceCircleLayer.getMapCameraIdleListener());
        }
    }

    DrawFenceRectangle fenceRectangleLayer;

    @Override
    public FenceDrawRectangleLayerApi drawFenceRectangleLayer() {
        if (fenceRectangleLayer == null) {
            fenceRectangleLayer = new DrawFenceRectangle(getContext(), parentView);
            layerApis.add(fenceRectangleLayer);
            loadFenceRectangleLayerEvent();
        }
        return fenceRectangleLayer;
    }

    void loadFenceRectangleLayerEvent() {
        if (fenceRectangleLayer != null) {
            addLoadedListener(fenceRectangleLayer.getMapLoadedListener());
            addMapClickListener(fenceRectangleLayer.getMapClickListener());
            addOnCameraIdleListener(fenceRectangleLayer.getMapCameraIdleListener());
        }
    }

    DrawFenceList fenceListLayer;

    @Override
    public FenceListLayerApi drawFenceListLayer() {
        if (fenceListLayer == null) {
            fenceListLayer = new DrawFenceList(getContext());
            layerApis.add(fenceListLayer);
            loadFenceListLayerEvent();
        }
        return fenceListLayer;
    }

    void loadFenceListLayerEvent() {
        if (fenceListLayer != null) {
            addLoadedListener(fenceListLayer.getMapLoadedListener());
            addMapClickListener(fenceListLayer.getMapClickListener());
            addOnMapPolygonClickListeners(fenceListLayer.getPolygonClickListener());
        }
    }

    @Override
    public CaptureDrawLayerApi drawCaptureLayer() {
        return null;
    }

    //endregion

    //region line
    DefineLineLayer lineLayer;

    @Override
    public LineDrawLayerApi drawLineLayer() {
        if (lineLayer == null) {
            lineLayer = new DefineLineLayer(getContext());
            layerApis.add(lineLayer);
            loadLineLayerEvent();
        }
        return lineLayer;
    }

    void loadLineLayerEvent() {
        if (lineLayer != null) {
            addLoadedListener(lineLayer.getMapLoadedListener());
            addOnCameraIdleListener(lineLayer.getMapCameraIdleListener());
        }
    }

    DefineCircleLayer circleLayer;

    @Override
    public CircleDrawLayerApi drawCircleLayer() {
        if (circleLayer == null) {
            circleLayer = new DefineCircleLayer(getContext());
            layerApis.add(circleLayer);
            loadCircleLayerEvent();
        }
        return circleLayer;
    }

    void loadCircleLayerEvent() {
        if (circleLayer != null) {
            addLoadedListener(circleLayer.getMapLoadedListener());
        }
    }

    DefineNavigateLayer navigateLayer;

    @Override
    public NavigateLayerApi drawNavigateLayer() {
        if (navigateLayer == null) {
            navigateLayer = new DefineNavigateLayer(getContext());
            layerApis.add(navigateLayer);
            loadNavigateLayerEvent();
            locationEngineEnable(1 * 1000L);
        }
        return navigateLayer;
    }

    void loadNavigateLayerEvent() {
        if (navigateLayer != null) {
            addLoadedListener(navigateLayer.getMapLoadedListener());
            addLocationChangedListener(navigateLayer.getLocationChangedListener());
        }
    }
    //endregion

    //region map event
    List<MapClickListener> mapClickListeners = new ArrayList<>();

    void addMapClickListener(MapClickListener listener) {
        if (listener == null) {
            return;
        }
        if (!mapClickListeners.contains(listener)) {
            this.mapClickListeners.add(listener);
        }
    }

    List<MapLoadedListener> mapLoadedListeners = new ArrayList<>();

    void addLoadedListener(MapLoadedListener listener) {
        if (listener == null) {
            return;
        }
        if (!mapLoadedListeners.contains(listener)) {
            this.mapLoadedListeners.add(listener);
        }
    }

    List<MapCameraIdleListener> mapCameraIdleListeners = new ArrayList<>();

    void addOnCameraIdleListener(MapCameraIdleListener listener) {
        if (listener == null) {
            return;
        }
        if (!mapCameraIdleListeners.contains(listener)) {
            mapCameraIdleListeners.add(listener);
        }
    }

    AMap.OnCameraChangeListener cameraIdleListener = new AMap.OnCameraChangeListener() {
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {

        }

        @Override
        public void onCameraChangeFinish(CameraPosition cameraPosition) {
            for (MapCameraIdleListener listener : mapCameraIdleListeners) {
                listener.mapCameraIdle();
            }
        }
    };

    List<MapLocationChangedListener> mapLocationChangedListeners = new ArrayList<>();

    void addLocationChangedListener(MapLocationChangedListener listener) {
        if (listener == null) {
            return;
        }
        if (!mapLocationChangedListeners.contains(listener)) {
            mapLocationChangedListeners.add(listener);
        }
    }

    List<MapMarkerClickListener> mapMarkerClickListeners = new ArrayList<>();

    void addOnMarkerClickListeners(MapMarkerClickListener listener) {
        if (listener == null) {
            return;
        }
        if (!mapMarkerClickListeners.contains(listener)) {
            mapMarkerClickListeners.add(listener);
        }
    }

    List<PolygonClickListener> mapPolygonClickListeners = new ArrayList<>();

    void addOnMapPolygonClickListeners(PolygonClickListener listener) {
        if (listener == null) {
            return;
        }
        if (!mapPolygonClickListeners.contains(listener)) {
            mapPolygonClickListeners.add(listener);
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        for (MapMarkerClickListener listener : mapMarkerClickListeners) {
            listener.mapMarkerClick(marker);
        }
        return true;
    }

    List<MapInfoWindowClickListener> mapInfoWindowClickListeners = new ArrayList<>();

    void addOnInfoWindowClickListener(MapInfoWindowClickListener listener) {
        if (listener == null) {
            return;
        }
        if (!mapInfoWindowClickListeners.contains(listener)) {
            mapInfoWindowClickListeners.add(listener);
        }
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        for (MapInfoWindowClickListener listener : mapInfoWindowClickListeners) {
            listener.mapInfoWindowClick(marker);
        }
    }

    //endregion

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {
        if (mapView != null) {
            mapView.onResume();
        }
        if (locationEngine != null) {
            locationEngine.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        if (locationEngine != null) {
            locationEngine.onPause();
        }
    }

    @Override
    public void onStop() {
        if (mapView != null) {
//            mapView.onStop();
        }
    }

    @Override
    public void onCreate(Bundle outState) {
        mapView.onCreate(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        for (LayerApi layer : layerApis) {
            layer.onDestroy();
        }
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mapClickListeners.clear();
        this.mapLoadedListeners.clear();
        this.mapCameraIdleListeners.clear();
        this.mapMarkerClickListeners.clear();
        this.mapInfoWindowClickListeners.clear();
    }
}
