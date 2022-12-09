package com.druid.mapcore.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class FenceCoreBean implements Serializable {
    public String id="";
    public String name="";
    public int msg_type=1;//1 进入，2 离开
    public int distance;
    public String type;
    public ArrayList<double[]> points = new ArrayList<>();
}
