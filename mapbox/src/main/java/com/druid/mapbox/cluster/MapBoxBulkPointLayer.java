package com.druid.mapbox.cluster;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.druid.mapcore.cluster.BulkPointLayer;
import com.druid.mapcore.bean.ClusterMarkerBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.interfaces.MapBulkClickListener;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.utils.MapUtils;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MapBoxBulkPointLayer extends BulkPointLayer {
    public static final String TAG = MapBoxBulkPointLayer.class.getName();

    public MapBoxBulkPointLayer(Context context) {
        super(context);
    }

    private SymbolManager bulkPointSymbolManager;
    private MapView mapView = null;
    private MapboxMap mapboxMap = null;
    private Style style = null;
    private ArrayList<ClusterMarkerBean> sources = new ArrayList<>();
    private List<Symbol> symbols = new ArrayList<>();

    public MapBoxBulkPointLayer bindMap(MapView mapView, MapboxMap mapboxMap) {
        this.mapView = mapView;
        this.mapboxMap = mapboxMap;
        this.style = mapboxMap.getStyle();
        return this;
    }

    @Override
    public void mapReadyLoad(boolean reloaded) {
        this.style = mapboxMap.getStyle();
        bulkPointSymbolManager = new SymbolManager(mapView, mapboxMap, style);
        bulkPointSymbolManager.setIconAllowOverlap(true);
        bulkPointSymbolManager.setTextOptional(true);
        if (reloaded) {
            reloadBulkPoints();
        }
        bulkPointSymbolManager.addClickListener(new OnSymbolClickListener() {
            @Override
            public boolean onAnnotationClick(Symbol symbol) {
                Log.i(TAG, symbol.getSymbolSortKey() + "");
                return false;
            }
        });
    }

    public MapLoadedListener getMapLoadedListener() {
        return this;
    }

    public void setLayerVisible(boolean visible) {

    }

    private MapBulkClickListener bulkClickListener;

    public MapBoxBulkPointLayer setBulkPointSymbolManager(MapBulkClickListener listener) {
        this.bulkClickListener = listener;
        return this;
    }

    public void loadBulkPoints(ArrayList<ClusterMarkerBean> sources) {
        this.sources = sources;
        reloadBulkPoints();
    }

    private void reloadBulkPoints() {
        if (symbols.size() > 0 && sources.size() > 0) {
            if (mapboxMap != null && (!mapView.isDestroyed())) {
                bulkPointSymbolManager.delete(symbols);
            }
        }
        new LoadSymbolOptionsListTask(this, sources).execute();
    }

    private void onSymbolOptionListLoaded(List<Feature> features) {
        List<SymbolOptions> options = new ArrayList<>();
        for (int i = 0; i < features.size(); i++) {
            Feature feature = features.get(i);
            options.add(new SymbolOptions()
                    .withGeometry((Point) feature.geometry())
                    .withIconImage("icon_battery_mark4")//"fire-station-11")
            );
        }
        symbols = bulkPointSymbolManager.create(options);
    }

    private static class LoadSymbolOptionsListTask extends AsyncTask<Void, Integer, List<Feature>> {

        private WeakReference<MapBoxBulkPointLayer> instance;
        private ArrayList<ClusterMarkerBean> sources;

        private LoadSymbolOptionsListTask(MapBoxBulkPointLayer instance, ArrayList<ClusterMarkerBean> sources) {
            this.instance = new WeakReference<>(instance);
            this.sources = sources;
        }

        @Override
        protected List<Feature> doInBackground(Void... params) {
       /*     List<SymbolOptions> options = new ArrayList<>();
            if (sources != null) {
                for (int i = 0; i < sources.size(); i++) {
                    ClusterMarkerBean source = sources.get(i);
                    if (source.latLng != null) {
                        double lat = source.latLng.lat;
                        double lng = source.latLng.lng;
                        double alt = source.latLng.alt;
                         if (MapUtils.validLatLng(lat, lng)) {
                                SymbolOptions option = new SymbolOptions();
                                option.withGeometry(Point.fromLngLat(lng, lat, alt));
                                option.withIconImage(source.resource_id);
                                options.add(option);
                        }
                    }
                }
            }*/
            List<Feature> features = new ArrayList<>();
            for (int i = 0; i < sources.size(); i++) {
                ClusterMarkerBean source = sources.get(i);
                if (source.latLng != null) {
                    double lat = source.latLng.getLat();
                    double lng = source.latLng.getLng();
                    double alt = source.latLng.getAlt();
                    if (MapUtils.validLatLng(lat, lng)) {
                        Feature feature = Feature.fromGeometry(Point.fromLngLat(lng, lat));
                        features.add(feature);
                    }
                }
            }
            return features;
        }

        @Override
        protected void onPostExecute(List<Feature> features) {
            super.onPostExecute(features);
            MapBoxBulkPointLayer layer = this.instance.get();
            if (layer != null) {
                if (layer.context != null) {
                    layer.onSymbolOptionListLoaded(features);
                }
            }
        }
    }

    @Override
    public void mapClick(LatLngBean latLngBean) {

    }

    @Override
    public void onScaleEnd() {

    }

    @Override
    public void onDestroy() {
        if (bulkPointSymbolManager != null) {
            bulkPointSymbolManager.onDestroy();
        }
    }

}
