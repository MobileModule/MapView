package com.druid.mapcore.interfaces;

import com.druid.mapcore.bean.LocationBean;

public interface MapLocationChangedListener {
    void locationChanged(LocationBean location);
}
