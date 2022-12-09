package com.druid.mapcore.bean;

import com.druid.mapcore.utils.LatLngTransformUtils;
import com.druid.mapcore.utils.MapUtils;

import java.io.Serializable;

public class LatLngBean implements Serializable {
    private double lat = 0;
    private double lng = 0;
    private double alt = 0;

    public int point_location = 0;// 0为地球，1为中国，2为香港，3为台湾

    //wgs84 坐标
    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    //动态获取坐标
    public double getLat(boolean mapTile,MapEngineType mapType) {
        if(!MapUtils.validLatLng(lat,lng)){
           return lat;
        }
        if(mapType==MapEngineType.MapGoogle){
            if(mapTile){//国内卫星影像图使用火星坐标系:gcj
                if(LatLngTransformUtils.inChina(point_location,lat,lng)){
                   return LatLngTransformUtils.Latlng84_To_Gcj02(lat,lng)[0];
                }
            }
        }
        if(mapType==MapEngineType.MapBaidu){

        }
        if(mapType==MapEngineType.MapGaode){

        }
        if(mapType==MapEngineType.MapBox){

        }
        return lat;
    }

    public double getLng(boolean mapTile,MapEngineType mapType) {
        if(!MapUtils.validLatLng(lat,lng)){
            return lng;
        }
        if(mapType==MapEngineType.MapGoogle){
            if(mapTile){//国内卫星影像图使用火星坐标系:gcj
                if(LatLngTransformUtils.inChina(point_location,lat,lng)){
                    return LatLngTransformUtils.Latlng84_To_Gcj02(lat,lng)[1];
                }
            }
        }
        if(mapType==MapEngineType.MapBaidu){

        }
        if(mapType==MapEngineType.MapGaode){

        }
        if(mapType==MapEngineType.MapBox){

        }
        return lng;
    }

    //
    public double getAlt() {
        return alt;
    }

    public void setPointLocation(int point_location) {
        this.point_location = point_location;
    }

    public LatLngBean(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public LatLngBean(int point_location, double lat, double lng) {
        this.point_location = point_location;
        this.lat = lat;
        this.lng = lng;
    }

    public LatLngBean(int point_location, double lat, double lng, double alt) {
        this.point_location = point_location;
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
    }

    public LatLngBean(double lat, double lng, double alt) {
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
    }


}
