package com.druid.mapbox;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.druid.mapbox.capture.DefineCaptureLayer;
import com.druid.mapbox.circle.DefineCircleLayer;
import com.druid.mapbox.cluster.MapBoxClusterLayer;
import com.druid.mapbox.fence.DrawFenceList;
import com.druid.mapbox.fence.circle.DrawFenceCircle;
import com.druid.mapbox.fence.polygon.DrawFencePolygonByAutoLocation;
import com.druid.mapbox.fence.polygon.DrawFencePolygonByManualLocation;
import com.druid.mapbox.fence.polygon.DrawFencePolygonHand;
import com.druid.mapbox.fence.polygon.DrawFencePolygonInput;
import com.druid.mapbox.fence.rectagle.DrawFenceRectangle;
import com.druid.mapbox.heatmap.MapBoxHeatMapLayer;
import com.druid.mapbox.line.DefineLineLayer;
import com.druid.mapbox.location.MapBoxLocationEngine;
import com.druid.mapbox.navigate.DefineNavigateLayer;
import com.druid.mapbox.tile.CustomTileSet;
import com.druid.mapbox.utils.LocationParseUtils;
import com.druid.mapbox.utils.MapConstantUtils;
import com.druid.mapbox.utils.MapImageSettingUtils;
import com.druid.mapcore.DruidMapView;
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
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.layer.LayerApi;
import com.druid.mapcore.line.LineDrawLayerApi;
import com.druid.mapcore.navigate.NavigateLayerApi;
import com.druid.mapcore.utils.MapStringUtils;
import com.druid.mapcore.utils.PermissionListener;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.StandardScaleGestureDetector;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;
import com.mapbox.mapboxsdk.plugins.localization.MapLocale;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapBoxView extends LinearLayout implements DruidMapView,
        OnMapReadyCallback, Style.OnStyleLoaded,
        LocationEngineCallback<LocationEngineResult>,
        MapboxMap.OnMapClickListener, MapLocationChangedListener {
    public static final String TAG = MapBoxView.class.getName();

    public MapBoxView(Context context) {
        super(context);
        initView();
    }

    public MapBoxView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTypedArray(context, attrs);
        initView();
    }

    public MapBoxView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs);
        initView();
    }

    void initTypedArray(Context context, AttributeSet attrs) {
        if (attrs != null) {

        }
    }

    private RelativeLayout parentView;
    private MapView mapBoxView = null;
    private MapboxMap mapboxMap = null;
    private Style style = null;
    private List<LayerApi> layerApis = new ArrayList<>();


    void initView() {
        inflate(getContext(), R.layout.mapbox_view, this);
        parentView = findViewById(R.id.parentView);
        mapBoxView = findViewById(R.id.mapView);
        mapBoxView.onCreate(null);
    }

    private ArrayList<ImageRegisterBean> registerImages = new ArrayList<>();

    @Override
    public void registerMapMarkerImages(ArrayList<ImageRegisterBean> images) {
        this.registerImages = images;
    }

    @Override
    public void loadingMap(MapLoadedListener loadedListener) {
        addLoadedListener(loadedListener);
        mapBoxView.getMapAsync(this);
    }

    private boolean enableGesture=true;

    @Override
    public void enableGesture(boolean enable) {
        this.enableGesture=enable;
    }

    @Override
    public LatLngBean getCameraTarget() {
        LatLng latLng = mapboxMap.getCameraPosition().target;
        return new LatLngBean(latLng.getLatitude(), latLng.getLongitude(), latLng.getAltitude());
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.mapboxMap.addOnMapClickListener(this);
        this.mapboxMap.addOnScaleListener(onScaleListener);
        this.mapboxMap.addOnCameraIdleListener(cameraIdleListener);
        resetMarkerMainClickEvent();
        MapConstantUtils.configMapUISetting(mapboxMap);
        MapConstantUtils.enableGesture(mapboxMap,enableGesture);
        loadMapStyle();
    }

    @Override
    public void resetMarkerMainClickEvent() {

    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        for (MapClickListener clickListener : mapClickListeners) {
            clickListener.mapClick(new LatLngBean(point.getLatitude(), point.getLongitude(), point.getAltitude()));
        }
        return true;
    }

    void loadMapStyle() {
        Style.Builder styleBuilder = new Style.Builder()
                .fromUri(MapConstantUtils.Outdoors);
        mapboxMap.setStyle(styleBuilder, this);
    }

    boolean reloadedStyle = false;

    @Override
    public void onStyleLoaded(@NonNull Style style) {
//        CustomTileSet.setCustomTileSet(mapboxMap);
        this.style = style;
        MapImageSettingUtils.setMapSymbolDefaultResource(getContext(), style);
        MapImageSettingUtils.setMapSymbolResource(getContext(), style, registerImages);
        for (LayerApi layerApi : layerApis) {
            layerApi.bindMap(mapBoxView, mapboxMap);
        }
        for (MapLoadedListener loadedListener : mapLoadedListeners) {
            loadedListener.mapReadyLoad(reloadedStyle);
        }
        if (!this.reloadedStyle) {
            this.reloadedStyle = true;
        }
    }

    @Override
    public void setMapLanguage(String lan) {
        if (mapBoxView != null && mapboxMap != null && style != null) {
            LocalizationPlugin localizationPlugin = new LocalizationPlugin(mapBoxView, mapboxMap, style);
//            localizationPlugin.matchMapLanguageWithDeviceDefault();
//            if (lan.equals(MapStringUtils.SIMPLIFIED_CHINESE)) {
//                localizationPlugin.setMapLanguage(new MapLocale(MapLocale.SIMPLIFIED_CHINESE));
//                return;
//            }
//            if (lan.equals(MapStringUtils.KOREAN)) {
//                localizationPlugin.setMapLanguage(new MapLocale(MapLocale.KOREAN));
//                return;
//            }

            try {
                localizationPlugin.setMapLanguage(MapLocale.KOREAN);
            } catch (RuntimeException exception) {
                Log.d("MainActivity", exception.toString());
            }
        }
    }

    private boolean loadMineLocation = false;

    @Override
    public void locationMineEnable(String permissionId, PermissionListener permissionListener,boolean firstLocationMine) {
        loadMineLocation = firstLocationMine;
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(
                buildLocationComponentActivationOptions(style));
        locationComponent.setRenderMode(RenderMode.COMPASS);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.getLocationEngine().getLastLocation(this);
    }

    @Override
    public boolean cameraMineLocation() {
        Location location = mapboxMap.getLocationComponent().getLastKnownLocation();
        if (location != null) {
            LatLng selfLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            animationCameraToLatLng(selfLatLng, 14);
            locationChanged(LocationParseUtils.getLocation(location));
        }
        return true;
    }

    MapBoxLocationEngine locationEngine;

    @Override
    public void locationEngineEnable(long intervalTime) {
        if (locationEngine == null) {
            locationEngine = new MapBoxLocationEngine(getContext(),intervalTime);
            locationEngine.setLocationChangedListener(this);
        }
    }

    private LocationComponentActivationOptions
    buildLocationComponentActivationOptions(Style style) {
        LocationComponentOptions locationComponentOptions =
                LocationComponentOptions.builder(getContext())
                        .pulseEnabled(true)
                        .pulseColor(getResources().getColor(R.color.map_gray_82))
                        .build();

        return LocationComponentActivationOptions
                .builder(getContext(), style)
                .locationComponentOptions(locationComponentOptions)
//                .useSpecializedLocationLayer(true)
                .useDefaultLocationEngine(true)
                .locationEngineRequest(new LocationEngineRequest.Builder(500)
                        .setFastestInterval(500)
                        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                        .build())
                .build();
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        if (!mapBoxView.isDestroyed()) {
            if (loadMineLocation) {
                loadMineLocation = false;
                LatLngBean myLatLng = new LatLngBean(result.getLastLocation().getLatitude(),
                        result.getLastLocation().getLongitude());
                cameraAnimationLocationLatLng(myLatLng);
            }
        }
    }

    @Override
    public void onFailure(@NonNull Exception e) {

    }

    @Override
    public void locationChanged(LocationBean location) {
        for (MapLocationChangedListener listener : mapLocationChangedListeners) {
            listener.locationChanged(location);
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
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(animatedPosition));
    }

    private boolean mapTile=false;//是否是瓦片服务

    @Override
    public void changeMapSourceStyleStreet() {
        if (mapboxMap != null) {
            mapTile=false;
            mapboxMap.setStyle(new Style.Builder().fromUri(MapConstantUtils.Street), this);
//            mapboxMap.setStyle(new Style.Builder().fromUri("asset://tianditu.json"));
        }
    }

    @Override
    public void changeMapSourceStyleSatellite() {
        if (mapboxMap != null) {
            mapTile=true;
            mapboxMap.setStyle(new Style.Builder().fromUri(MapConstantUtils.Satellite), this);
        }
    }

    @Override
    public boolean getStatusMapTile() {
        return mapTile;
    }

    @Override
    public void zoomMap(boolean isOut) {
        double zoom = mapboxMap.getCameraPosition().zoom;
        if (isOut) {
            zoom += 2;
        } else {
            zoom -= 2;
        }
        mapboxMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    //region map event
    List<MapClickListener> mapClickListeners = new ArrayList<>();

    void addMapClickListener(MapClickListener listener) {
        if (!mapClickListeners.contains(listener)) {
            this.mapClickListeners.add(listener);
        }
    }

    LinkedList<MapLoadedListener> mapLoadedListeners = new LinkedList<>();

    void addLoadedListener(MapLoadedListener listener) {
        if (!mapLoadedListeners.contains(listener)) {
            this.mapLoadedListeners.add(listener);
        }
    }

    List<MapOnScaleListener> mapOnScaleListeners = new ArrayList<>();

    void addOnScaleListener(MapOnScaleListener listener) {
        if (!mapOnScaleListeners.contains(listener)) {
            this.mapOnScaleListeners.add(listener);
        }
    }

    MapboxMap.OnScaleListener onScaleListener = new MapboxMap.OnScaleListener() {
        @Override
        public void onScaleBegin(@NonNull StandardScaleGestureDetector detector) {

        }

        @Override
        public void onScale(@NonNull StandardScaleGestureDetector detector) {

        }

        @Override
        public void onScaleEnd(@NonNull StandardScaleGestureDetector detector) {
            for (MapOnScaleListener mapOnScaleListener : mapOnScaleListeners) {
                mapOnScaleListener.onScaleEnd();
            }
        }
    };

    List<MapCameraIdleListener> mapCameraIdleListeners = new ArrayList<>();

    void addOnCameraIdleListener(MapCameraIdleListener listener) {
        if (!mapCameraIdleListeners.contains(listener)) {
            mapCameraIdleListeners.add(listener);
        }
    }

    MapboxMap.OnCameraIdleListener cameraIdleListener = new MapboxMap.OnCameraIdleListener() {
        @Override
        public void onCameraIdle() {
            for (MapCameraIdleListener listener : mapCameraIdleListeners) {
                listener.mapCameraIdle();
            }
        }
    };

    List<MapLocationChangedListener> mapLocationChangedListeners = new ArrayList<>();

    void addLocationChangedListener(MapLocationChangedListener listener) {
        if (!mapLocationChangedListeners.contains(listener)) {
            mapLocationChangedListeners.add(listener);
        }
    }
    //endregion

    //region cluster layer
    MapBoxClusterLayer clusterLayer = null;

    @Override
    public ClusterLayer drawClusterLayer() {
        if (clusterLayer == null) {
            this.clusterLayer = new MapBoxClusterLayer(getContext());
            layerApis.add(clusterLayer);
            loadClusterLayerEvent();
        }
        return clusterLayer;
    }

    void loadClusterLayerEvent() {
        if (clusterLayer != null) {
            addMapClickListener(clusterLayer.getMapClickListener());
            addLoadedListener(clusterLayer.getMapLoadedListener());
            addOnScaleListener(clusterLayer.getMapOnScaleListener());
        }
    }

    MapBoxHeatMapLayer heatMapLayer = null;

    @Override
    public HeatMapLayerApi drawHeatMapLayer() {
        if (heatMapLayer == null) {
            this.heatMapLayer = new MapBoxHeatMapLayer(getContext());
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
            addOnScaleListener(fenceCircleLayer.getMapOnScaleListener());
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
        if(fenceListLayer==null){
            fenceListLayer=new DrawFenceList(getContext());
            layerApis.add(fenceListLayer);
            loadFenceListLayerEvent();
        }
        return fenceListLayer;
    }

    void loadFenceListLayerEvent(){
        if (fenceListLayer != null) {
            addLoadedListener(fenceListLayer.getMapLoadedListener());
            addMapClickListener(fenceListLayer.getMapClickListener());
        }
    }

    //endregion

    //region line layer
    DefineLineLayer lineLayer;

    @Override
    public LineDrawLayerApi drawLineLayer() {
        if(lineLayer==null){
            lineLayer=new DefineLineLayer(getContext());
            layerApis.add(lineLayer);
            loadLineLayerEvent();
        }
        return lineLayer;
    }

    void loadLineLayerEvent(){
        if(lineLayer!=null){
            addLoadedListener(lineLayer.getMapLoadedListener());
        }
    }

    DefineNavigateLayer navigateLayer;

    @Override
    public NavigateLayerApi drawNavigateLayer() {
        if(navigateLayer==null){
            navigateLayer=new DefineNavigateLayer(getContext());
            layerApis.add(navigateLayer);
            loadNavigateLayerEvent();
            locationEngineEnable(1*1000L);
        }
        return navigateLayer;
    }

    void loadNavigateLayerEvent(){
        if(navigateLayer!=null){
            addLoadedListener(navigateLayer.getMapLoadedListener());
            addLocationChangedListener(navigateLayer.getLocationChangedListener());
        }
    }

    DefineCircleLayer circleLayer;

    @Override
    public CircleDrawLayerApi drawCircleLayer() {
        if(circleLayer==null){
            circleLayer=new DefineCircleLayer(getContext());
            layerApis.add(circleLayer);
            loadCircleLayerEvent();
        }
        return circleLayer;
    }

    void loadCircleLayerEvent(){
        if(circleLayer!=null){
            addLoadedListener(circleLayer.getMapLoadedListener());
        }
    }
    //endregion

    //region capture layer
    DefineCaptureLayer captureLayer;

    @Override
    public CaptureDrawLayerApi drawCaptureLayer() {
        if(captureLayer==null){
            captureLayer=new DefineCaptureLayer(getContext());
            layerApis.add(captureLayer);
            loadCaptureLayerEvent();
        }
        return captureLayer;
    }

    void loadCaptureLayerEvent(){
        if(captureLayer!=null){
            addLoadedListener(captureLayer.getMapLoadedListener());
            addMapClickListener(captureLayer.getMapClickListener());
        }
    }
    //endregion

    @Override
    public void onStart() {
        if (mapBoxView != null) {
            mapBoxView.onStart();
        }
    }

    @Override
    public void onResume() {
        if (mapBoxView != null) {
            mapBoxView.onResume();
        }
        if (locationEngine != null) {
            locationEngine.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapBoxView != null) {
            mapBoxView.onPause();
        }
        if (locationEngine != null) {
            locationEngine.onPause();
        }
    }

    @Override
    public void onStop() {
        if (mapBoxView != null) {
            mapBoxView.onStop();
        }
    }

    @Override
    public void onCreate(Bundle outState) {
        if (mapBoxView != null) {
            mapBoxView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mapBoxView != null) {
            mapBoxView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        if (mapBoxView != null) {
            mapBoxView.onDestroy();
        }
        for (LayerApi layerApi : layerApis) {
            layerApi.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        if (mapBoxView != null)
            mapBoxView.onLowMemory();
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
        this.mapOnScaleListeners.clear();
    }
}
