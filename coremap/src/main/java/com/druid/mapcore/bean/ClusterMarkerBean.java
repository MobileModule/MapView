package com.druid.mapcore.bean;

import com.druid.mapcore.R;

import java.io.Serializable;

public class ClusterMarkerBean implements Serializable {
    public LatLngBean latLng;
    public Object object;
    public int index = 0;
    public String id = "";
    public String name = "";
    public String resource_id_name = "icon_battery_mark4";
    public int res_id = R.drawable.icon_battery_mark4;
}
