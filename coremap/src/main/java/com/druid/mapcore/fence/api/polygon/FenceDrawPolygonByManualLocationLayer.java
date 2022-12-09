package com.druid.mapcore.fence.api.polygon;

import com.druid.mapcore.interfaces.MapLocationChangedListener;

public interface FenceDrawPolygonByManualLocationLayer<T1, T2> extends FencePolygonLayerApi<T1, T2> {
    MapLocationChangedListener getLocationChangedListener();
    boolean addLocationPoint();
}
