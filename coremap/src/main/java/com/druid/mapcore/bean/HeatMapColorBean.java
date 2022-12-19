package com.druid.mapcore.bean;

import java.io.Serializable;

public class HeatMapColorBean implements Serializable {
    public int color = 0xffffff;
    public int a = 0xff;
    public float weight_start = -1f;
    public float weight_end = -1f;
}
