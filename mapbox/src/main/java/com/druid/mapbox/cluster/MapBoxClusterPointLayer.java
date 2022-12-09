package com.druid.mapbox.cluster;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.e;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import com.druid.mapbox.utils.LatLngUtils;
import com.druid.mapbox.utils.MapConstantUtils;
import com.druid.mapbox.utils.MapImageSettingUtils;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.cluster.ClusterPointLayer;
import com.druid.mapcore.bean.ClusterMarkerBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.interfaces.ClusterStatusChangedListener;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapClusterClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.layer.LayerApi;
import com.druid.mapcore.setting.MapSetting;
import com.druid.mapcore.utils.ClusterMarkerImage;
import com.druid.mapcore.utils.MapLog;
import com.druid.mapcore.utils.MapUtils;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

public class MapBoxClusterPointLayer extends ClusterPointLayer implements LayerApi<MapView, MapboxMap> {
    public static final String TAG = MapBoxClusterPointLayer.class.getName();

    public MapBoxClusterPointLayer(Context context) {
        super(context);
    }

    /**
     * @param isCluster          if default cluster
     * @param usedBulkPointLayer if default bulk points
     */
    public MapBoxClusterPointLayer setDefaultSetting(boolean isCluster, boolean usedBulkPointLayer) {
        this.isCluster = isCluster;
        this.usedBulkPointLayer = usedBulkPointLayer;
        if (usedBulkPointLayer) {
            unClusterBulkMarkLayer = new MapBoxBulkPointLayer(context);
        }
        return this;
    }

    private ClusterStatusChangedListener clusterStatusChangedListener;

    public void setClusterStatusChangedListener(ClusterStatusChangedListener listener) {
        this.clusterStatusChangedListener = listener;
    }

    MapView mapView = null;
    MapboxMap mapboxMap = null;
    boolean usedBulkPointLayer = false;
    Style style = null;
    GeoJsonOptions geoJsonOptions = null;
    GeoJsonSource clusterSource = null;
    SymbolLayer clusterLayer = null;
    SymbolLayer clusterMarkerLayer = null;
    GeoJsonSource unClusterSource = null;
    SymbolLayer unClusterMarkerLayer = null;
    GeoJsonSource clickMarkerSource = null;
    SymbolLayer clickMarkerLayer = null;
    MapBoxBulkPointLayer unClusterBulkMarkLayer = null;

    //style loaded execute
    @Override
    public void bindMap(MapView mapView, MapboxMap mapboxMap) {
        if (mapboxMap.getStyle().isFullyLoaded()) {
            this.mapView = mapView;
            this.mapboxMap = mapboxMap;
            this.style = mapboxMap.getStyle();
            if (usedBulkPointLayer) {
                if (unClusterBulkMarkLayer != null) {
                    unClusterBulkMarkLayer.bindMap(mapView, mapboxMap);
                }
            }
        } else {
            MapLog.error(TAG, "Mapbox style is unLoaded,cannot bindMap for ClusterLayer");
        }
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {

    }

    MapClusterClickListener clusterClickListener = null;

    public void setClusterClickListener(MapClusterClickListener listener) {
        this.clusterClickListener = listener;
    }

    public MapClickListener getMapClickListener() {
        return this;
    }

    public MapLoadedListener getMapLoadedListener() {
        return this;
    }

    public MapOnScaleListener getMapOnScaleListener() {
        return this;
    }

    @Override
    public MapCameraIdleListener getMapCameraIdleListener() {
        return null;
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
    public void cameraToLayer() {

    }

    private ArrayList<ClusterMarkerBean> sources = new ArrayList<>();

    private void loadClusterSource(Style style) {
        int clusterZoomLevel = (int) mapboxMap.getMaxZoomLevel();
        geoJsonOptions = new GeoJsonOptions()
                .withClusterMaxZoom(clusterZoomLevel)
                .withCluster(true)
                .withClusterRadius(50);
        clusterSource = new GeoJsonSource(MapConstantUtils.SOURCE_CLUSTER_ID,
                geoJsonOptions);
        style.addSource(clusterSource);
        style.addLayer(createClusterMarkerLayer());
        style.addLayer(createClusterLayer());
        registerMarkerImage();
    }

    private SymbolLayer createClusterMarkerLayer() {
        Expression imgNormal = Expression.toString(get(MapConstantUtils.FIELD_IMAGE_NORMAL));
        clusterMarkerLayer = new SymbolLayer(MapConstantUtils.LAYER_UNCLUSTERED_ID,
                MapConstantUtils.SOURCE_CLUSTER_ID)
                .withProperties(iconImage(imgNormal),
                        iconOffset(new Float[]{0f, MapSetting.getClusterMarkerNormalOffsetY(context, isDefaultMarker())}));
        return clusterMarkerLayer;
    }

    private SymbolLayer createClusterLayer() {
        clusterLayer = new SymbolLayer(MapConstantUtils.LAYER_CLUSTERED_ID,
                MapConstantUtils.SOURCE_CLUSTER_ID);
        clusterLayer.setProperties(iconImage(MapSetting.CLUSTER_MARKER),
                circleRadius(18f),
                textField(Expression.toString(get(MapConstantUtils.FIELD_CLUSTER_COUNT))),
                textSize(12f),
                textColor(Color.WHITE),
                textIgnorePlacement(true),
                textAllowOverlap(true)
        );
        clusterLayer.setFilter(all(has(MapConstantUtils.FIELD_CLUSTER_COUNT)));
        return clusterLayer;
    }

    private void loadUnClusterSource(Style style) {
        unClusterSource = new GeoJsonSource(MapConstantUtils.SOURCE_MARKER_ID);
        style.addSource(unClusterSource);
        style.addLayer(createUnClusterLayer());
    }

    private SymbolLayer createUnClusterLayer() {
        unClusterMarkerLayer = new SymbolLayer(MapConstantUtils.LAYER_MAKER_ID,
                MapConstantUtils.SOURCE_MARKER_ID);
        unClusterMarkerLayer.withProperties(
                iconImage(get(MapConstantUtils.FIELD_IMAGE_NORMAL)),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[]{0f, MapSetting.getClusterMarkerNormalOffsetY(context, isDefaultMarker())})
        );
        unClusterMarkerLayer.withProperties(visibility(Property.NONE));
        return unClusterMarkerLayer;
    }

    private void loadClickSource(Style style) {
        clickMarkerSource = new GeoJsonSource(MapConstantUtils.SOURCE_MARKER_CLICK_ID);
        style.addSource(clickMarkerSource);
        style.addLayer(createClickMarkerLayer());
    }

    private SymbolLayer createClickMarkerLayer() {
        clickMarkerLayer = new SymbolLayer(MapConstantUtils.LAYER_MAKER_CLICK_ID,
                MapConstantUtils.SOURCE_MARKER_CLICK_ID);
        clickMarkerLayer.withProperties(
                iconImage(get(MapConstantUtils.FIELD_IMAGE_BIG)),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[]{0f, MapSetting.getClusterMarkerLargeOffsetY(context, isDefaultMarker())})
        );
        clickMarkerLayer.withProperties(visibility(Property.NONE));
        return clickMarkerLayer;
    }

    private LatLngBounds clusterLayerLatLngBounds = null;
    private FeatureCollection featureCollection = null;
    private boolean flatZoom = true;

    public void loadCluster(ArrayList<ClusterMarkerBean> sources, boolean flatZoom) {
        this.sources = sources;
        this.flatZoom = flatZoom;
        reloadCluster();
        if (usedBulkPointLayer) {
            if (unClusterBulkMarkLayer != null) {
                unClusterBulkMarkLayer.loadBulkPoints(sources);
            }
        }
    }

    void reloadCluster() {
        ArrayList<Feature> features = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < sources.size(); i++) {
            ClusterMarkerBean source = sources.get(i);
            if (source.latLng != null) {
                double lat = source.latLng.getLat();
                double lng = source.latLng.getLng();
                if (MapUtils.validLatLng(lat, lng)) {
                    String img_name = source.resource_id_name;
                    JsonObject properties = new JsonObject();
                    properties.addProperty(MapConstantUtils.FIELD_ID, source.id);
                    properties.addProperty(MapConstantUtils.FIELD_INDEX, i);
                    properties.addProperty(MapConstantUtils.FIELD_IMAGE_NORMAL, img_name);
                    Feature feature = Feature.fromGeometry(
                            Point.fromLngLat(lng, lat), properties);
                    features.add(feature);
                    builder.include(new LatLng(lat, lng));
                }
            }
        }
        if (features.size() > 1) {
            clusterLayerLatLngBounds = builder.build();
        }
        featureCollection = FeatureCollection.fromFeatures(features);
        //
        clusterSource.setGeoJson(featureCollection);
        if (!usedBulkPointLayer) {
            unClusterSource.setGeoJson(featureCollection);
        }
        keepClusterStyle();
        if (features.size() > 0) {
            if (flatZoom) {
                if (!reloadMap) {
                    cameraToClusterMarkerBounds();
                }
            }
        }
    }

    protected boolean isCluster = true;

    protected boolean changeMakerCluster() {
        if (isCluster) {
            isCluster = false;
            clusterLayer.withProperties(visibility(Property.NONE));
            clusterMarkerLayer.withProperties(visibility(Property.NONE));
            if (!usedBulkPointLayer) {
                unClusterMarkerLayer.withProperties(visibility(Property.VISIBLE));
            }
        } else {
            isCluster = true;
            if (!usedBulkPointLayer) {
                unClusterMarkerLayer.withProperties(visibility(Property.NONE));
            }
            clusterLayer.withProperties(visibility(Property.VISIBLE));
            clusterMarkerLayer.withProperties(visibility(Property.VISIBLE));
        }
        if (clusterStatusChangedListener != null) {
            clusterStatusChangedListener.clusterStatusChanged(isCluster);
        }
        return isCluster;
    }

    private void keepClusterStyle() {
        if (isCluster) {
            if (usedBulkPointLayer) {
                unClusterBulkMarkLayer.setLayerVisible(false);
            } else {
                unClusterMarkerLayer.withProperties(visibility(Property.NONE));
            }
            clusterLayer.withProperties(visibility(Property.VISIBLE));
            clusterMarkerLayer.withProperties(visibility(Property.VISIBLE));
        } else {
            clusterLayer.withProperties(visibility(Property.NONE));
            clusterMarkerLayer.withProperties(visibility(Property.NONE));
            if (usedBulkPointLayer) {
                unClusterBulkMarkLayer.setLayerVisible(true);
            } else {
                unClusterMarkerLayer.withProperties(visibility(Property.VISIBLE));
            }
        }
    }

    protected void cameraToClusterMarkerBounds() {
        if (clusterLayerLatLngBounds != null) {
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(clusterLayerLatLngBounds, MapSetting.MAP_BOUNDS_PADDING));
        } else {
            if (sources.size() == 1) {
                if(MapUtils.validLatLng(sources.get(0).latLng.getLat(),
                        sources.get(0).latLng.getLng())) {
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(sources.get(0).latLng.getLat(),
                            sources.get(0).latLng.getLng()), 9f));
                }
            }
        }
    }

    private boolean reloadMap = false;

    @Override
    public void mapReadyLoad(boolean reloaded) {
        this.style = mapboxMap.getStyle();
        this.reloadMap = reloaded;
        //判断是否使用海量点图层
        if (usedBulkPointLayer) {
            if (unClusterBulkMarkLayer != null) {
                unClusterBulkMarkLayer.mapReadyLoad(reloaded);
            }
        }
        //先初始化样式
        loadClusterSource(style);
        if (!usedBulkPointLayer) {
            loadUnClusterSource(style);
        }
        loadClickSource(style);
        //再初始化数据源
        if (reloaded) {
            reloadCluster();
        }
        setLayerVisible(visibleLayer);
    }

    private int nowMarkerClickIndex = -1;

    void reloadMarkerClick(boolean addEnable) {
        if (addEnable) {
            if (sources.size() > 0) {
                if (nowMarkerClickIndex != -1) {
                    ClusterMarkerBean source = sources.get(nowMarkerClickIndex);
                    String img_name = source.resource_id_name + ClusterMarkerImage.LARGE;
                    JsonObject properties = new JsonObject();
                    properties.addProperty(MapConstantUtils.FIELD_ID, source.id);
                    properties.addProperty(MapConstantUtils.FIELD_INDEX, nowMarkerClickIndex);
                    properties.addProperty(MapConstantUtils.FIELD_IMAGE_BIG, img_name);
                    Feature feature = Feature.fromGeometry(
                            Point.fromLngLat(source.latLng.getLng(), source.latLng.getLat()), properties);
                    FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
                    clickMarkerSource.setGeoJson(featureCollection);
                    clickMarkerLayer.withProperties(visibility(Property.VISIBLE));
                } else {
                    clickMarkerLayer.withProperties(visibility(Property.NONE));
                }
            }
        } else {
            clickLayerNormal();
        }
    }

    public void clickLayerNormal() {
        if (nowMarkerClickIndex != -1) {
            if (clusterClickListener != null) {
                clusterClickListener.mapMarkerSingleRemove(nowMarkerClickIndex);
            }
            nowMarkerClickIndex = -1;
        }
        clickMarkerLayer.withProperties(visibility(Property.NONE));
    }

    @Override
    public void mapClick(LatLngBean latLngBean) {
        if (nowMarkerClickIndex != -1) {
            reloadMarkerClick(false);
            return;
        }
        mapClickToLarge(latLngBean);
    }

    private void mapClickToLarge(LatLngBean latLngBean) {
        LatLng latLng = new LatLng(latLngBean.getLat(), latLngBean.getLng());
        PointF point = mapboxMap.getProjection().toScreenLocation(latLng);
        List<Feature> features = mapboxMap.queryRenderedFeatures(point,
                MapConstantUtils.LAYER_CLUSTERED_ID,
                MapConstantUtils.LAYER_UNCLUSTERED_ID,
                MapConstantUtils.LAYER_MAKER_ID);
        if (!features.isEmpty()) {
            Feature feature = features.get(0);
            if (feature.hasProperty(MapConstantUtils.FIELD_CLUSTER_COUNT)) {
                android.graphics.Point screenPoint =
                        new android.graphics.Point((int) point.x, (int) point.y);
                onClusterClick(features.get(0), latLng, screenPoint);
            } else {
                if (feature.hasProperty(MapConstantUtils.FIELD_INDEX)) {
                    nowMarkerClickIndex =
                            feature.getNumberProperty(MapConstantUtils.FIELD_INDEX).intValue();
                    Point point_geo = (Point) feature.geometry();
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), markerToLargeCallback);
                }
            }
        }
    }

    public boolean searchMarkerCluster(String marker_id) {
        if (isCluster) {
            changeMakerCluster();
        }
        if (nowMarkerClickIndex != -1) {
            reloadMarkerClick(false);
        }
        int index = -1;
        for (int i = 0; i < sources.size(); i++) {
            if (marker_id.equals(sources.get(i).id)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return false;
        } else {
            ClusterMarkerBean marker = sources.get(index);
            if (marker.latLng != null) {
                if (marker.latLng.getLat() != 200 && marker.latLng.getLng() != 200) {
                    nowMarkerClickIndex = index;
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(LatLngUtils.mapLatLngFrom(marker.latLng))
                            , markerToLargeCallback);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    MapboxMap.CancelableCallback markerToLargeCallback = new MapboxMap.CancelableCallback() {
        @Override
        public void onCancel() {

        }

        @Override
        public void onFinish() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    reloadMarkerClick(true);
                    ClusterMarkerBean marker = sources.get(nowMarkerClickIndex);
                    if (clusterClickListener != null) {
                        clusterClickListener.mapMarkerSingleClick(nowMarkerClickIndex, marker);
                    }
                }
            }, 10);
        }
    };

    private void onClusterClick(Feature clusterFeature, LatLng latLng,
                                android.graphics.Point clickPoint) {
        if (mapboxMap.getCameraPosition().zoom < mapboxMap.getMaxZoomLevel()) {
            double zoom = mapboxMap.getCameraPosition().zoom + 2f;
            if (mapboxMap.getCameraPosition().zoom < mapboxMap.getMaxZoomLevel()) {
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
            }
        } else {
            int count = clusterFeature.
                    getNumberProperty(MapConstantUtils.FIELD_CLUSTER_COUNT).intValue();
            FeatureCollection featureCollection = clusterSource.getClusterLeaves(clusterFeature,
                    count, 0);
            ArrayList<ClusterMarkerBean> selectedMarkers = new ArrayList<>();
            List<Feature> features = featureCollection.features();
            for (int i = 0; i < features.size(); i++) {
                Feature feature = features.get(i);
                Point point = (Point) feature.geometry();
                if (feature.hasProperty(MapConstantUtils.FIELD_INDEX)) {
                    int index =
                            feature.getNumberProperty(MapConstantUtils.FIELD_INDEX).intValue();
                    selectedMarkers.add(sources.get(index));
                }
            }
            if (clusterClickListener != null) {
                clusterClickListener.mapMarkerClusterClick(selectedMarkers);
            }
        }
    }

    @Override
    public void onScaleEnd() {
        reloadMarkerClick(false);
    }

    @Override
    public void onDestroy() {
        if (usedBulkPointLayer) {
            if (unClusterBulkMarkLayer != null) {
                unClusterBulkMarkLayer.onDestroy();
            }
        }
    }

    @Override
    public void mapInfoWindowClick(Object marker) {

    }

    @Override
    public boolean mapMarkerClick(Object marker) {
        return false;
    }

    @Override
    public void mapCameraIdle() {

    }

    protected boolean visibleLayer = true;

    public boolean setLayerVisible(boolean visible) {
        this.visibleLayer = visible;
        clickLayerNormal();//点击事件复位
        if (isCluster) {
            if (visible) {
                clusterLayer.withProperties(visibility(Property.VISIBLE));
                clusterMarkerLayer.withProperties(visibility(Property.VISIBLE));
                unClusterMarkerLayer.withProperties(visibility(Property.NONE));
            } else {
                clusterLayer.withProperties(visibility(Property.NONE));
                clusterMarkerLayer.withProperties(visibility(Property.NONE));
                unClusterMarkerLayer.withProperties(visibility(Property.NONE));
            }
        } else {
            if (visible) {
                clusterLayer.withProperties(visibility(Property.NONE));
                clusterMarkerLayer.withProperties(visibility(Property.NONE));
                unClusterMarkerLayer.withProperties(visibility(Property.VISIBLE));
            } else {
                clusterLayer.withProperties(visibility(Property.NONE));
                clusterMarkerLayer.withProperties(visibility(Property.NONE));
                unClusterMarkerLayer.withProperties(visibility(Property.NONE));
            }
        }
        return visible;
    }

    Drawable startNormalDrawable;
    Drawable startBigDrawable;
    Drawable centerNormalDrawable;
    Drawable centerBigDrawable;
    Drawable endNormalDrawable;
    Drawable endBigDrawable;

    protected void setMapPositionDrawable(Drawable startNormalDrawable, Drawable startBigDrawable,
                                          Drawable centerNormalDrawable, Drawable centerBigDrawable,
                                          Drawable endNormalDrawable, Drawable endBigDrawable) {
        this.startNormalDrawable = startNormalDrawable;
        this.startBigDrawable = startBigDrawable;
        this.centerNormalDrawable = centerNormalDrawable;
        this.centerBigDrawable = centerBigDrawable;
        this.endNormalDrawable = endNormalDrawable;
        this.endBigDrawable = endBigDrawable;
    }

    protected void registerMarkerImage() {
        if (!isDefaultMarker()) {
            mapboxMap.getStyle().addImage(ClusterMarkerImage.CUSTOM.CLUSTER_MAKER_START, startNormalDrawable);
            mapboxMap.getStyle().addImage(ClusterMarkerImage.CUSTOM.CLUSTER_MAKER_START_BIG, startBigDrawable);
            mapboxMap.getStyle().addImage(ClusterMarkerImage.CUSTOM.CLUSTER_MAKER_CENTER, centerNormalDrawable);
            mapboxMap.getStyle().addImage(ClusterMarkerImage.CUSTOM.CLUSTER_MAKER_CENTER_BIG, centerBigDrawable);
            mapboxMap.getStyle().addImage(ClusterMarkerImage.CUSTOM.CLUSTER_MAKER_END, endNormalDrawable);
            mapboxMap.getStyle().addImage(ClusterMarkerImage.CUSTOM.CLUSTER_MAKER_END_BIG, endBigDrawable);
        }
    }

    private boolean isDefaultMarker() {
        if (startNormalDrawable != null &&
                startBigDrawable != null &&
                centerNormalDrawable != null &&
                centerBigDrawable != null &&
                endNormalDrawable != null &&
                endBigDrawable != null) {
            return false;
        }
        return true;
    }
}
