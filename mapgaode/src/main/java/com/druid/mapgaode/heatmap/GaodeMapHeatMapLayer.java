package com.druid.mapgaode.heatmap;

import android.content.Context;
import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.Gradient;
import com.amap.api.maps.model.HeatmapTileProvider;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.HeatMapSetBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.heatmap.HeatMapLayer;
import com.druid.mapcore.heatmap.HeatMapLayerApi;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.druid.mapgaode.utils.LatLngUtils;

import java.util.ArrayList;
import java.util.List;

public class GaodeMapHeatMapLayer extends HeatMapLayer implements HeatMapLayerApi<MapView, AMap> {
    public GaodeMapHeatMapLayer(Context context) {
        super(context);
    }

    protected MapView mapView = null;
    protected AMap aMap = null;

    @Override
    public void bindMap(MapView mapview_, AMap map_) {
        this.mapView = mapview_;
        this.aMap = map_;

    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {

    }

    @Override
    public void mapReadyLoad(boolean reloaded) {
        createHeatMapSource();
        updateHeatMapLayer();
    }

    private void createHeatMapSource() {

    }

    @Override
    public void setHeatMapSource(List<LatLngBean> source) {
        this.source = source;
        updateHeatMapLayer();
    }

    private HeatMapSetBean set;

    @Override
    public void setHeatMapSet(HeatMapSetBean set) {
        this.set=set;
    }

    /**
     * Alternative radius for convolution
     */
    private static final int ALT_HEATMAP_RADIUS = 10;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.4;

    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    private HeatmapTileProvider mProvider;
    private TileOverlay heatMapLayer;

    private void updateHeatMapLayer() {
        if (aMap != null) {
            if (heatMapLayer != null) {
                heatMapLayer.clearTileCache();
                heatMapLayer.remove();
            }
            if (source.size() <= 0) {
                return;
            }
            List<LatLng> points = new ArrayList<>();
            for (LatLngBean latLngBean : source) {
                points.add(LatLngUtils.mapLatLngFrom(latLngBean));
            }
            mProvider = new HeatmapTileProvider.Builder().data(points).build();

//            mProvider.setRadius(ALT_HEATMAP_RADIUS);
//            mProvider.setGradient(ALT_HEATMAP_GRADIENT);
//            mProvider.setOpacity(ALT_HEATMAP_OPACITY);

            heatMapLayer = aMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
    }

    @Override
    public boolean setLayerVisible(boolean visible) {
        if (heatMapLayer != null)
            heatMapLayer.setVisible(visible);
        return visible;
    }

    @Override
    public MapLoadedListener getMapLoadedListener() {
        return this;
    }

    @Override
    public MapClickListener getMapClickListener() {
        return null;
    }

    @Override
    public MapOnScaleListener getMapOnScaleListener() {
        return null;
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

    @Override
    public void onDestroy() {
        if (heatMapLayer != null) {
            heatMapLayer.remove();
            heatMapLayer = null;
        }
    }

}
