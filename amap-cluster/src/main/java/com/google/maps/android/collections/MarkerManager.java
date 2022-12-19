/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.collections;

import android.view.View;


import com.amap.api.maps.AMap;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.google.maps.android.MapClusterConstant;

/**
 * Keeps track of collections of markers on the map. Delegates all Marker-related events to each
 * collection's individually managed listeners.
 * <p/>
 * All marker operations (adds and removes) should occur via its collection class. That is, don't
 * add a marker via a collection, then remove it via Marker.remove()
 */
public class MarkerManager extends MapObjectManager<Marker, MarkerManager.Collection> implements
        AMap.OnInfoWindowClickListener,
        AMap.OnMarkerClickListener,
        AMap.OnMarkerDragListener,
        AMap.InfoWindowAdapter,
        AMap.OnInfoWindowLongClickListener {

    public MarkerManager(AMap map) {
        super(map);
    }

    @Override
    void setListenersOnUiThread() {
        if (mMap != null) {
            mMap.setOnInfoWindowClickListener(this);
            mMap.setOnInfoWindowLongClickListener(this);
            mMap.setOnMarkerClickListener(this);
            mMap.setOnMarkerDragListener(this);
            mMap.setInfoWindowAdapter(this);
        }
    }

    public Collection newCollection() {
        return new Collection();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mInfoWindowAdapter != null) {
            return collection.mInfoWindowAdapter.getInfoWindow(marker);
        }
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mInfoWindowAdapter != null) {
            return collection.mInfoWindowAdapter.getInfoContents(marker);
        }
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mInfoWindowClickListener != null) {
            collection.mInfoWindowClickListener.onInfoWindowClick(marker);
        }
    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mInfoWindowLongClickListener != null) {
            collection.mInfoWindowLongClickListener.onInfoWindowLongClick(marker);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mMarkerClickListener != null) {
            return collection.mMarkerClickListener.onMarkerClick(marker);
        }
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDragStart(marker);
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDrag(marker);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Collection collection = mAllObjects.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDragEnd(marker);
        }
    }

    @Override
    protected void removeObjectFromMap(Marker object) {
        object.remove();
    }

    public class Collection extends MapObjectManager.Collection {
        private AMap.OnInfoWindowClickListener mInfoWindowClickListener;
        private AMap.OnInfoWindowLongClickListener mInfoWindowLongClickListener;
        private AMap.OnMarkerClickListener mMarkerClickListener;
        private AMap.OnMarkerDragListener mMarkerDragListener;
        private AMap.InfoWindowAdapter mInfoWindowAdapter;

        public Collection() {
        }

        //todo 修改
        public Marker addMarker(MarkerOptions opts) {
            opts.zIndex(100);
            if(MapClusterConstant.needOffset) {
                opts.anchor(0.5f, 0.5f);
            }
            Marker marker = mMap.addMarker(opts);
            super.add(marker);
            return marker;
        }

        public void addAll(java.util.Collection<MarkerOptions> opts) {
            for (MarkerOptions opt : opts) {
                addMarker(opt);
            }
        }

        public void addAll(java.util.Collection<MarkerOptions> opts, boolean defaultVisible) {
            for (MarkerOptions opt : opts) {
                addMarker(opt).setVisible(defaultVisible);
            }
        }

        public void showAll() {
            for (Marker marker : getMarkers()) {
                marker.setVisible(true);
            }
        }

        public void hideAll() {
            for (Marker marker : getMarkers()) {
                marker.setVisible(false);
            }
        }

        public boolean remove(Marker marker) {
            return super.remove(marker);
        }

        public java.util.Collection<Marker> getMarkers() {
            return getObjects();
        }

        public void setOnInfoWindowClickListener(GoogleMap.OnInfoWindowClickListener infoWindowClickListener) {
            mInfoWindowClickListener = infoWindowClickListener;
        }

        public void setOnInfoWindowLongClickListener(GoogleMap.OnInfoWindowLongClickListener infoWindowLongClickListener) {
            mInfoWindowLongClickListener = infoWindowLongClickListener;
        }

        public void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener markerClickListener) {
            mMarkerClickListener = markerClickListener;
        }

        public void setOnMarkerDragListener(GoogleMap.OnMarkerDragListener markerDragListener) {
            mMarkerDragListener = markerDragListener;
        }

        public void setInfoWindowAdapter(GoogleMap.InfoWindowAdapter infoWindowAdapter) {
            mInfoWindowAdapter = infoWindowAdapter;
        }
    }
}
