package com.druid.mapcore.bean;

import java.io.Serializable;

public class ImageRegisterBean implements Serializable {
    public int resId = 0;
    public String imgName = "";

    public ImageRegisterBean(int resId, String imgName) {
        this.resId = resId;
        this.imgName = imgName;
    }
}
