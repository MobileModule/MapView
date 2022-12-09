package com.druid.mapcore.bean;

import java.io.Serializable;

public class MarkerBean implements Serializable {
    public String id = "";
    public String name = "";
    public String title = "";
    public float angle = 0;
    public LatLngBean location = null;

    public MarkerBean(String id, String name, String title,
                      float angle, LatLngBean location) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.angle = angle;
        this.location = location;
    }
}
