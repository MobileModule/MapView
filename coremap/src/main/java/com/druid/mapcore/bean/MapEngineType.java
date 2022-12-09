package com.druid.mapcore.bean;

public enum MapEngineType {
    MapBox(0),
    MapGoogle(1),
    MapBaidu(2),
    MapGaode(3);
    private final int value;

    MapEngineType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
