package com.druid.mapgoogle.cluster;

import android.content.Context;
import android.text.TextUtils;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.ClusterMarkerBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.MapEngineType;
import com.druid.mapcore.cluster.ClusterPointLayer;
import com.druid.mapcore.interfaces.ClusterStatusChangedListener;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapClusterClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapcore.layer.LayerApi;
import com.druid.mapcore.setting.MapConstant;
import com.druid.mapcore.setting.MapSetting;
import com.druid.mapcore.utils.MapLog;
import com.druid.mapcore.utils.MapUtils;
import com.druid.mapcore.utils.ResourceUtils;
import com.druid.mapgoogle.bean.GoogleClusterMarkerBean;
import com.druid.mapgoogle.bean.GoogleClusterMarkerItem;
import com.druid.mapgoogle.utils.LatLngUtils;
import com.druid.mapgoogle.utils.MapConstantUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.MapClusterConstant;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

public class MapGoogleClusterPointLayer extends ClusterPointLayer<Marker> implements LayerApi<MapView, GoogleMap>,
        GoogleClusterManager.GoogleClusterMarkerClickListener {
    public static final String TAG = MapGoogleClusterPointLayer.class.getName();

    public MapGoogleClusterPointLayer(Context context) {
        super(context);
    }

    public MapGoogleClusterPointLayer setDefaultSetting(boolean isCluster, boolean usedBulkPointLayer) {
        this.isCluster = isCluster;
        return this;
    }

    private ClusterStatusChangedListener clusterStatusChangedListener;

    public void setClusterStatusChangedListener(ClusterStatusChangedListener listener) {
        this.clusterStatusChangedListener = listener;
    }

    MapView mapView = null;
    GoogleMap googleMap = null;
    protected boolean isCluster = true;

    @Override
    public void bindMap(MapView mapView_, GoogleMap map) {
        this.mapView = mapView_;
        this.googleMap = map;
    }

    private DruidMapView druidMapView;

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {
        this.druidMapView = druidMapView;
    }

    private boolean reloadMap = false;

    @Override
    public void mapReadyLoad(boolean reloaded) {
        this.reloadMap = reloaded;
        //  createClusterMarkerLayer();
        //再初始化数据源
        if (reloaded) {
            reloadCluster();
        }
    }

    private ArrayList<ClusterMarkerBean> sources = new ArrayList<>();
    private boolean defaultIcon = true;
    private boolean flatZoom = true;

    public void loadCluster(ArrayList<ClusterMarkerBean> sources, boolean defaultIcon, boolean flatZoom, boolean needOffset) {
        this.sources = sources;
        this.defaultIcon = defaultIcon;//是否使用默认图标
        this.flatZoom = flatZoom;
        MapClusterConstant.needOffset = needOffset;
        reloadCluster();
    }

    private LatLngBounds clusterLayerLatLngBounds = null;

    void reloadCluster() {
        keepClusterStyle();
        if (flatZoom) {
            if (!reloadMap) {
                cameraToClusterMarkerBounds();
            }
        }
    }

    protected boolean changeMakerCluster() {
        if (isCluster) {
            isCluster = false;
            addUnClusterMarkerLayer();
        } else {
            isCluster = true;
            addClusterMarkerLayer();
        }
        if (clusterStatusChangedListener != null) {
            clusterStatusChangedListener.clusterStatusChanged(isCluster);
        }
        return isCluster;
    }

    private void keepClusterStyle() {
        if (isCluster) {
            addClusterMarkerLayer();
        } else {
            addUnClusterMarkerLayer();
        }
    }

    private ArrayList<Marker> uncluster_markers = new ArrayList<Marker>();

    private void addUnClusterMarkerLayer() {
        mapClear();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        //google cluster had reset map event
        if (druidMapView != null) {
            druidMapView.resetMarkerMainClickEvent();
        }
        for (int i = 0; i < sources.size(); i++) {
            ClusterMarkerBean source = sources.get(i);
            if (source.latLng != null) {
                double lat = source.latLng.getLat(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
                double lng = source.latLng.getLng(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
                if (MapUtils.validLatLng(lat, lng)) {
                    boolean largeMarker = false;
                    int resId = source.res_id;
                    if (nowMarkerClickIndex != -1) {
                        if (nowMarkerClickIndex == i) {
                            largeMarker = true;
                            resId = ResourceUtils.getMarkerLargeResourceId(source.resource_id_name, context);
                        }
                    }
                    builder.include(new LatLng(lat, lng));
                    BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(resId);
                    MarkerOptions markerOption = new MarkerOptions().icon(descriptor)
                            .position(LatLngUtils.mapLatLngFrom(source.latLng))
                            .title(MapConstantUtils.getMarkerTitle(largeMarker, i))
                            .draggable(false)
                            .zIndex(MapConstantUtils.ZINDEX_MARKER);
                    if (!defaultIcon) {
                        markerOption.anchor(0.5f, 0.5f);
                    }
                    Marker marker = googleMap.addMarker(markerOption);
                    uncluster_markers.add(marker);
                }
            }
        }
        if (uncluster_markers.size() > 1)
            clusterLayerLatLngBounds = builder.build();
//        zoomFirstCamera(false);
    }

    private GoogleClusterManager<GoogleClusterMarkerBean> googleClusterManager;

    private void createClusterMarkerLayer() {
        if (googleClusterManager == null) {
            googleClusterManager = new GoogleClusterManager<GoogleClusterMarkerBean>(context, googleMap);
            googleClusterManager.setClusterMarkerClickListener(this::mapMarkerClick);
            MapConstantUtils.configClusterStyle(context, googleClusterManager, googleMap);
            googleClusterManager.setOnClusterClickListener(googleClusterClickListener);
            googleClusterManager.setOnClusterItemClickListener(googleClusterItemClickListener);
        }
    }

    ClusterManager.OnClusterClickListener googleClusterClickListener =
            new ClusterManager.OnClusterClickListener<GoogleClusterMarkerBean>() {
                @Override
                public boolean onClusterClick(Cluster<GoogleClusterMarkerBean> cluster) {
                    LatLng latLng = cluster.getPosition();
                    if (cluster.getSize() > 0) {
                        if (googleMap.getCameraPosition().zoom < googleMap.getMaxZoomLevel()) {
                            float zoom = googleMap.getCameraPosition().zoom + 2f;
                            if (googleMap.getCameraPosition().zoom < googleMap.getMaxZoomLevel()) {
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
                            }
                        } else {
                            ArrayList<ClusterMarkerBean> selectedMarkers = new ArrayList<>();
                            for (GoogleClusterMarkerBean googleClusterMarker : cluster.getItems()) {
                                Object object = googleClusterMarker.getMarker().object;
                                if (object instanceof ClusterMarkerBean) {
                                    ClusterMarkerBean clusterMarker = (ClusterMarkerBean) object;
                                    selectedMarkers.add(clusterMarker);
                                }
                            }
                            if (clusterClickListener != null) {
                                clusterClickListener.mapMarkerClusterClick(selectedMarkers);
                            }
                        }
                    }
                    return true;
                }
            };

    ClusterManager.OnClusterItemClickListener googleClusterItemClickListener =
            new ClusterManager.OnClusterItemClickListener<GoogleClusterMarkerBean>() {
                @Override
                public boolean onClusterItemClick(GoogleClusterMarkerBean item) {
                    int index = item.getIndex();
                    if (index < sources.size()) {
                        ClusterMarkerBean clusterMarker = sources.get(index);
                        clickClusterMarker = item;
                        nowMarkerClickIndex = item.getIndex();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(item.getPosition()), clusterMarkerToLargeCallback);
                    }
                    return true;
                }
            };

    GoogleMap.CancelableCallback clusterMarkerToLargeCallback = new GoogleMap.CancelableCallback() {
        @Override
        public void onCancel() {

        }

        @Override
        public void onFinish() {
            boolean toLarge = clusterMarkerLarge();
            if (toLarge) {

            }
        }
    };

    //region uncluster
    private int pre_marker_click_index = -1;

    private void addClusterMarkerLayer() {
        createClusterMarkerLayer();//重置延持
        mapClear();
        googleMap.setOnMarkerClickListener(googleClusterManager);
        // googleMap.setOnInfoWindowClickListener(mClusterManager); //事件会被重置
        List<GoogleClusterMarkerBean> googleMarkerItems = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < sources.size(); i++) {
            ClusterMarkerBean source = sources.get(i);
            if (source.latLng != null) {
                double lat = source.latLng.getLat(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
                double lng = source.latLng.getLng(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
                if (MapUtils.validLatLng(lat, lng)) {
                    builder.include(new LatLng(lat, lng));
                    GoogleClusterMarkerItem clusterMarker = new GoogleClusterMarkerItem();
                    int resId = source.res_id;
                    if (nowMarkerClickIndex != -1) {
                        if (nowMarkerClickIndex == i) {
                            resId = ResourceUtils.getMarkerLargeResourceId(source.resource_id_name, context);
                        }
                    }
                    clusterMarker.icon = resId;
                    clusterMarker.mTitle = MapConstantUtils.CLUSTER_MARKER_NORMAL;
                    clusterMarker.latLng = LatLngUtils.mapLatLngFrom(source.latLng);
                    clusterMarker.object = source.object;
                    googleMarkerItems.add(new GoogleClusterMarkerBean(i, clusterMarker));
                }
            }
        }
        if (googleMarkerItems.size() > 1) {
            clusterLayerLatLngBounds = builder.build();
        }
        googleClusterManager.addItems(googleMarkerItems);
        googleClusterManager.cluster();
    }

    GoogleMap.CancelableCallback markerToLargeCallback = new GoogleMap.CancelableCallback() {
        @Override
        public void onCancel() {

        }

        @Override
        public void onFinish() {
            unClusterMarkerLarge();
        }
    };

    private void unClusterMarkerLarge() {
        boolean clickSelf = false;
        if (nowMarkerClickIndex != -1) {
            Marker marker = uncluster_markers.get(nowMarkerClickIndex);
            int resId = ResourceUtils.getMarkerLargeResourceId(sources.get(nowMarkerClickIndex).resource_id_name, context);
            String title = marker.getTitle();
            //已经最大化-移除
            if (MapConstantUtils.getMarkerISLargeFormTitle(title)) {
                resId = sources.get(nowMarkerClickIndex).res_id;
                clickSelf = true;
                title = MapConstantUtils.getMarkerTitle(false, nowMarkerClickIndex);
            } else {
                //复位
                preMarkerNormal();
                title = MapConstantUtils.getMarkerTitle(true, nowMarkerClickIndex);
                this.pre_marker_click_index = nowMarkerClickIndex;
            }

            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(resId);
            marker.setIcon(descriptor);
            marker.setTitle(title);
            if (clickSelf) {
                clusterMarkerClickClear(false);
                if (clusterClickListener != null) {
                    clusterClickListener.mapMarkerSingleRemove(nowMarkerClickIndex);
                }
            } else {
                if (clusterClickListener != null) {
                    clusterClickListener.mapMarkerSingleClick(nowMarkerClickIndex, sources.get(nowMarkerClickIndex));
                }
            }
        }
    }

    private void preMarkerNormal() {
        if (pre_marker_click_index != -1) {
            Marker preMarker = uncluster_markers.get(pre_marker_click_index);
            int resId = sources.get(pre_marker_click_index).res_id;
            String title = MapConstantUtils.getMarkerTitle(false, pre_marker_click_index);
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(resId);
            preMarker.setIcon(descriptor);
            preMarker.setTitle(title);
        }
    }

    public void unClusterMarkerNormal() {
        if (nowMarkerClickIndex != -1) {
            if (clusterClickListener != null) {
                clusterClickListener.mapMarkerSingleRemove(nowMarkerClickIndex);
            }
        }
        if (nowMarkerClickIndex != -1) {
            Marker marker = uncluster_markers.get(nowMarkerClickIndex);
            int resId = sources.get(pre_marker_click_index).res_id;
            String title = MapConstantUtils.getMarkerTitle(false, nowMarkerClickIndex);
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(resId);
            marker.setIcon(descriptor);
            marker.setTitle(title);
        }
        clusterMarkerClickClear(false);
    }
    //endregion

    private int nowMarkerClickIndex = -1;

    //region cluster
    private GoogleClusterMarkerBean clickClusterMarker = null;
    private GoogleClusterMarkerBean pre_cluster_marker_large = null;
    private GoogleClusterMarkerBean pre_cluster_marker_normal = null;

    private boolean clusterMarkerLarge() {
        ClusterMarkerBean clusterMarkerClick = null;
        if (clickClusterMarker == null) {
            return false;
        } else {
            clusterMarkerClick = sources.get(nowMarkerClickIndex);
        }
        if (clusterMarkerClick == null) {
            return false;
        }

        boolean clickSelf = false;
        GoogleClusterMarkerItem marker = new GoogleClusterMarkerItem();
        int resId = ResourceUtils.getMarkerLargeResourceId(clusterMarkerClick.resource_id_name, context);
        marker.mTitle = MapConstantUtils.CLUSTER_MARKER_LARGE;
        //已经最大化-移除
        if (clickClusterMarker.getTitle().equals(MapConstantUtils.CLUSTER_MARKER_LARGE)) {
            resId = clusterMarkerClick.res_id;
            clickSelf = true;
            marker.mTitle = MapConstantUtils.CLUSTER_MARKER_NORMAL;
        }
        marker.icon = resId;
        marker.latLng = clickClusterMarker.getMarker().latLng;
        marker.object = clickClusterMarker.getMarker().object;
        GoogleClusterMarkerBean clusterMarker = new GoogleClusterMarkerBean(nowMarkerClickIndex, marker);
        if (clickSelf) {
            googleClusterManager.removeItem(clickClusterMarker);
            googleClusterManager.addItem(clusterMarker);
            if (clusterClickListener != null) {
                clusterClickListener.mapMarkerSingleRemove(nowMarkerClickIndex);
            }
            clusterMarkerClickClear(false);
            return false;
        } else {
            //复位
            if (this.pre_cluster_marker_large != null) {
                googleClusterManager.removeItem(pre_cluster_marker_large);
                if (this.pre_cluster_marker_normal != null) {
                    googleClusterManager.addItem(pre_cluster_marker_normal);
                }
            }
            this.pre_cluster_marker_large = clusterMarker;
            this.pre_cluster_marker_normal = clickClusterMarker;
            googleClusterManager.removeItem(pre_cluster_marker_normal);
            googleClusterManager.addItem(pre_cluster_marker_large);
            //
            if (clusterClickListener != null) {
                clusterClickListener.mapMarkerSingleClick(nowMarkerClickIndex, sources.get(nowMarkerClickIndex));
            }
            return true;
        }
    }

    private void clusterMarkerNormal() {
        if (nowMarkerClickIndex != -1) {
            if (clusterClickListener != null) {
                clusterClickListener.mapMarkerSingleRemove(nowMarkerClickIndex);
            }
        }
        if (pre_cluster_marker_normal != null || pre_cluster_marker_large != null) {
            if (nowMarkerClickIndex != -1) {
                //todo 遍历复位
                googleClusterManager.removeItem(pre_cluster_marker_large);
                googleClusterManager.addItem(pre_cluster_marker_normal);
                googleClusterManager.cluster();
            }
        }
        clusterMarkerClickClear(false);
    }

    private void clusterMarkerClickClear(boolean restoreClickIndex) {
        if (!restoreClickIndex) {
            if (pre_cluster_marker_normal != null && pre_cluster_marker_large != null) {
                pre_cluster_marker_normal = null;
                pre_cluster_marker_large = null;
            }
            clickClusterMarker = null;
        }
        if (!restoreClickIndex) {
            nowMarkerClickIndex = -1;
            pre_marker_click_index = -1;
        }
    }
    //endregion

    public void mapClear() {
        for (Marker marker : uncluster_markers) {
            marker.remove();
        }
        this.uncluster_markers.clear();
        clusterMarkerClickClear(true);
        if (googleClusterManager != null) {
            googleClusterManager.clearItems();
            googleClusterManager.cluster();
        }
        //googleMap.clear();
    }

    protected void cameraToClusterMarkerBounds() {
        if (clusterLayerLatLngBounds != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(clusterLayerLatLngBounds, MapSetting.MAP_BOUNDS_PADDING));
        } else {
            if (sources.size() == 1) {
                double lat = sources.get(0).latLng.getLat(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
                double lng = sources.get(0).latLng.getLng(druidMapView.getStatusMapTile(), MapEngineType.MapGoogle);
                if (MapUtils.validLatLng(lat, lng)) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 9f));
                }
            }
        }
    }

    @Override
    public void mapClick(LatLngBean latLngBean) {
        resetMarkerNormal();
    }

    public void resetMarkerNormal() {
        if (isCluster) {
            clusterMarkerNormal();
        } else {
            unClusterMarkerNormal();
        }
    }

    public boolean searchMarkerCluster(String marker_id) {
        if (isCluster) {
            changeMakerCluster();
        }
        if (nowMarkerClickIndex != -1) {
            resetMarkerNormal();
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
//                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(LatLngUtils.mapLatLngFrom(marker.latLng))
//                            , markerToLargeCallback);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLngUtils.mapLatLngFrom(marker.latLng)));
                    unClusterMarkerLarge();
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public void onScaleEnd() {

    }

    MapClusterClickListener clusterClickListener = null;

    public void setClusterClickListener(MapClusterClickListener listener) {
        this.clusterClickListener = listener;
    }

    @Override
    public MapLoadedListener getMapLoadedListener() {
        return this;
    }

    @Override
    public MapClickListener getMapClickListener() {
        return this;
    }

    @Override
    public MapOnScaleListener getMapOnScaleListener() {
        return null;
    }

    @Override
    public MapCameraIdleListener getMapCameraIdleListener() {
        return this;
    }

    @Override
    public MapMarkerClickListener getMapMarkerClickListener() {
        return this;
    }

    @Override
    public MapInfoWindowClickListener getMapInfoWindowClickListener() {
        return this;
    }

    @Override
    public void mapInfoWindowClick(Marker marker) {
        if (isCluster) {
            if (googleClusterManager != null) {
                googleClusterManager.onInfoWindowClick(marker);
            }
        }
    }

    @Override
    public boolean mapMarkerClick(Marker marker) {
        if (!isCluster) {
            String title = marker.getTitle();
            if (!TextUtils.isEmpty(title)) {
                try {
                    int index = MapConstantUtils.getMarkerIndexFromTitle(title);
                    if (index != -1) {
                        nowMarkerClickIndex = index;
                        ClusterMarkerBean clusterMarker = sources.get(index);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), markerToLargeCallback);
                    }
                } catch (Exception ex) {

                }
            }
        }
        return false;
    }

    @Override
    public void mapCameraIdle() {
        if (isCluster) {
            if (googleClusterManager != null) {
                googleClusterManager.onCameraIdle();
            }
        }
    }

    @Override
    public void cameraToLayer() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public boolean onGoogleMarkerClick(Marker marker) {
        MapLog.warning(TAG, "Cluster-onGoogleMarkerClick");
        return false;
    }

    public boolean setLayerVisible(boolean visible) {
        if (isCluster) {
            clusterMarkerNormal();//点击事件复位
            if (visible) {
                addClusterMarkerLayer();
            } else {
                mapClear();
            }
        } else {
            unClusterMarkerNormal();//点击事件复位
            if (visible) {
                addUnClusterMarkerLayer();
            } else {
                mapClear();
            }
        }
        return visible;
    }
}
