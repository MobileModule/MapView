package com.druid.mapcore.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class HeatMapSetBean implements Serializable {
    public HeatMapSetBean(){

    }
    public ArrayList<HeatMapColorBean> colors=new ArrayList<>();
    public int alpha=0;
    public int radius=0;
}
