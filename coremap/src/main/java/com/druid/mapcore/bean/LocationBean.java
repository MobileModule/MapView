package com.druid.mapcore.bean;

import java.io.Serializable;

public class LocationBean implements Serializable {
    public LatLngBean position = null;
    public double accuracy = 0;

    public LocationBean(LatLngBean position) {
        this.position = position;
    }
}
