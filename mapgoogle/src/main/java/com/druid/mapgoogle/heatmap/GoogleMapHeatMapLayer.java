package com.druid.mapgoogle.heatmap;

import android.content.Context;
import android.graphics.Color;

import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.HeatMapColorBean;
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
import com.druid.mapcore.utils.ColorUtils;
import com.druid.mapgoogle.utils.LatLngUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleMapHeatMapLayer extends HeatMapLayer implements HeatMapLayerApi<MapView, GoogleMap> {
    public GoogleMapHeatMapLayer(Context context) {
        super(context);
    }

    protected MapView mapView = null;
    protected GoogleMap googleMap = null;

    @Override
    public void bindMap(MapView mapview_, GoogleMap map_) {
        this.mapView = mapview_;
        this.googleMap = map_;

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
        updateHeatMapLayer();
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
        if(set==null){
            return;
        }
        ArrayList<Integer> colorsArray=new ArrayList<>();
        ArrayList<Float> levelArray=new ArrayList<>();
        for(HeatMapColorBean color:set.colors){
            colorsArray.add(color.color);
            levelArray.add(color.weight_end);
        }
        Collections.reverse(colorsArray);
        Collections.reverse(levelArray);
        colorsArray.add(0, Color.argb(0, 0, 0, 0));
        levelArray.add(0,0f);
        Integer[] colors_=new Integer[colorsArray.size()];
        colorsArray.toArray(colors_);
        Float[] levels_=new Float[levelArray.size()];
        levelArray.toArray(levels_);

        int[] colors=new int[colors_.length];
        for(int i=0;i<colors_.length;i++){
            colors[i]=colors_[i];
        }
        float[] levels=new float[levels_.length];
        for(int i=0;i<levels_.length;i++){
            levels[i]=levels_[i];
        }
        Gradient gradient=new Gradient(colors,levels);

        if (googleMap != null) {
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
            mProvider.setRadius(set.radius);
            mProvider.setGradient(gradient);
            mProvider.setOpacity(set.alpha/100d);
            heatMapLayer = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
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
