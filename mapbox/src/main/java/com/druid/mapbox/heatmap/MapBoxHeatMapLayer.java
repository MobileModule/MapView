package com.druid.mapbox.heatmap;

import static com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapIntensity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import android.content.Context;

import com.druid.mapbox.utils.MapConstantUtils;
import com.druid.mapcore.DruidMapView;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.heatmap.HeatMapLayer;
import com.druid.mapcore.heatmap.HeatMapLayerApi;
import com.druid.mapcore.interfaces.MapCameraIdleListener;
import com.druid.mapcore.interfaces.MapClickListener;
import com.druid.mapcore.interfaces.MapInfoWindowClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.interfaces.MapMarkerClickListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

public class MapBoxHeatMapLayer extends HeatMapLayer implements HeatMapLayerApi<MapView, MapboxMap> {
    public MapBoxHeatMapLayer(Context context) {
        super(context);
    }

    protected MapView mapView = null;
    protected MapboxMap mapboxMap = null;
    protected Style style = null;

    @Override
    public void bindMap(MapView mapview_, MapboxMap map_) {
        this.mapView = mapview_;
        this.mapboxMap = map_;
        this.style = mapboxMap.getStyle();
    }

    @Override
    public void attachDruidMap(DruidMapView druidMapView) {

    }

    @Override
    public void mapReadyLoad(boolean reloaded) {
        this.style = mapboxMap.getStyle();
        createHeatMapSource(style);
        setLayerVisible(visibleLayer);
        updateHeatMapLayer();
    }

    private GeoJsonSource heatMapSource = null;
    protected HeatmapLayer heatMapLayer = null;

    private void createHeatMapSource(Style style) {
        heatMapSource = new GeoJsonSource(MapConstantUtils.SOURCE_HEAT_MAP_ID);
        style.addSource(heatMapSource);
        style.addLayer(createHeatMapLayer());
    }

    protected HeatmapLayer createHeatMapLayer() {
        heatMapLayer = new HeatmapLayer(MapConstantUtils.LAYER_HEAT_MAP_ID,
                MapConstantUtils.SOURCE_HEAT_MAP_ID);
        heatMapLayer.setProperties(

                // Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
                // Begin color ramp at 0-stop with a 0-transparancy color
                // to create a blur-like effect.
                heatmapColor(
                        interpolate(
                                linear(), heatmapDensity(),
                                literal(0), rgba(255, 255, 255, 0),
                                literal(0.1), rgb(13, 252, 0),
                                literal(0.4), rgb(255, 211, 0),
                                literal(0.6), rgb(234, 91, 15),
                                literal(0.9), rgb(255, 0, 0)
                        )
                ),

                // Increase the heatmap weight based on frequency and property magnitude
//                heatmapWeight(
//
//                ),

                // Increase the heatmap color weight weight by zoom level
                // heatmap-intensity is a multiplier on top of heatmap-weight
                heatmapIntensity(
                        interpolate(
                                linear(), zoom(),
                                stop(0, 1),
                                stop(9, 3)
                        )
                )//,

                // Adjust the heatmap radius by zoom level
//                heatmapRadius(
//                ),

                // Transition from heatmap to circle layer by zoom level
//                heatmapOpacity(
//                )
        );
        return heatMapLayer;
    }

    @Override
    public void setHeatMapSource(List<LatLngBean> source) {
        this.source = source;
        updateHeatMapLayer();
    }

    private boolean visibleLayer=true;
    @Override
    public boolean setLayerVisible(boolean visible) {
        this.visibleLayer=visible;
        if (visible) {
            heatMapLayer.withProperties(visibility(Property.VISIBLE));
        } else {
            heatMapLayer.withProperties(visibility(Property.NONE));
        }
        return visible;
    }

    private void updateHeatMapLayer() {
        if (mapboxMap != null && mapboxMap.getStyle().isFullyLoaded()) {
            List<Feature> features = new ArrayList<>();
            for (LatLngBean latLngBean : source) {
                JsonObject properties = new JsonObject();
                Feature feature = Feature.fromGeometry(Point.fromLngLat(latLngBean.getLng(), latLngBean.getLat()), properties);
                features.add(feature);
            }
            FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);
            heatMapSource.setGeoJson(featureCollection);
        }
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

    }

}