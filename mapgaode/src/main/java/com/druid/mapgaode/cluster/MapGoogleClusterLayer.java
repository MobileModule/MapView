package com.druid.mapgaode.cluster;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Marker;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.ClusterMarkerBean;
import com.druid.mapcore.cluster.ClusterItemLayer;
import com.druid.mapcore.cluster.ClusterLayer;
import com.druid.mapcore.interfaces.ClusterStatusChangedListener;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapClusterClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;

import java.util.ArrayList;

public class MapGoogleClusterLayer extends ClusterItemLayer<Marker> implements ClusterLayer<MapView, AMap> {
    private MapGoogleClusterPointLayer clusterLayer;

    public MapGoogleClusterLayer(Context context) {
        super(context);
        clusterLayer = new MapGoogleClusterPointLayer(context);
    }

    @Override
    public void mapReadyLoad(boolean reloaded) {
        if (clusterLayer != null) {
            clusterLayer.mapReadyLoad(reloaded);
        }
    }

    @Override
    public void bindMap(MapView mapview, AMap map) {
        clusterLayer.bindMap(mapview, map);
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {
        clusterLayer.attachDruidMap(druidMapView);
    }

    @Override
    public MapLoadedListener getMapLoadedListener() {
        return this::mapReadyLoad;
    }

    @Override
    public MapClickListener getMapClickListener() {
        return clusterLayer.getMapClickListener();
    }

    @Override
    public MapOnScaleListener getMapOnScaleListener() {
        return clusterLayer.getMapOnScaleListener();
    }

    @Override
    public MapCameraIdleListener getMapCameraIdleListener() {
        return clusterLayer.getMapCameraIdleListener();
    }

    @Override
    public MapMarkerClickListener getMapMarkerClickListener() {
        return clusterLayer.getMapMarkerClickListener();
    }

    @Override
    public MapInfoWindowClickListener getMapInfoWindowClickListener() {
        return clusterLayer.getMapInfoWindowClickListener();
    }

    @Override
    public void cameraToLayer() {
        clusterLayer.cameraToClusterMarkerBounds();
    }


    @Override
    public void enableClusterMarker(MapClusterClickListener clusterClickListener, boolean isCluster, boolean usedBulkPointLayer) {
        clusterLayer.setDefaultSetting(isCluster, usedBulkPointLayer);
        setClusterClickListener(clusterClickListener);
    }

    @Override
    public void setClusterSource(ArrayList<ClusterMarkerBean> sources, boolean flatZoom,
                                 boolean defaultIcon,boolean needOffset) {
        if (clusterLayer != null) {
            clusterLayer.loadCluster(sources,defaultIcon,flatZoom,needOffset);
        }
    }

    @Override
    public boolean changeMarkerCluster() {
        if (clusterLayer != null) {
            return clusterLayer.changeMakerCluster();
        }
        return false;
    }

    @Override
    public boolean setLayerVisible(boolean visible) {
        if (clusterLayer != null) {
            return clusterLayer.setLayerVisible(visible);
        }
        return visible;
    }

    @Override
    public boolean searchMarkerCluster(String marker_id) {
        if (clusterLayer != null) {
            return clusterLayer.searchMarkerCluster(marker_id);
        }
        return false;
    }

    @Override
    public void resetMarkerLarge() {
        if (clusterLayer != null) {
            clusterLayer.resetMarkerNormal();
        }
    }

    @Override
    public void setClusterStatusChangedListener(ClusterStatusChangedListener listener) {
        if (clusterLayer != null) {
            clusterLayer.setClusterStatusChangedListener(listener);
        }
    }

    @Override
    public void setMapPositionDrawable(Drawable startNormalDrawable, Drawable startBigDrawable, Drawable centerNormalDrawable, Drawable centerBigDrawable, Drawable endNormalDrawable, Drawable endBigDrawable) {

    }

    @Override
    public void onDestroy() {
        if (clusterLayer != null) {
            clusterLayer.onDestroy();
        }
    }

    void setClusterClickListener(MapClusterClickListener clusterClickListener) {
        if (clusterLayer != null) {
            clusterLayer.setClusterClickListener(clusterClickListener);
        }
    }

    @Override
    public void mapInfoWindowClick(Marker marker) {
//        clusterLayer.mapInfoWindowClick(marker);
    }

    @Override
    public boolean mapMarkerClick(Marker marker) {
//        clusterLayer.mapMarkerClick(marker);
        return false;
    }
}
