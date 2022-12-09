package com.druid.mapcore.interfaces;

import com.druid.mapcore.bean.LatLngBean;

public interface FenceCircleAreaChangedListener {
    void fenceCircleAreaChanged(double radius, LatLngBean centerLatLng);
}
