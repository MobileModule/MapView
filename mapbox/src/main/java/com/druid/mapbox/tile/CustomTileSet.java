package com.druid.mapbox.tile;

import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

public class CustomTileSet{

    public static void setCustomTileSet(MapboxMap mapbox){
        TileSet tileSet=new TileSet("2.1.0","http://t0.tianditu.gov.cn/img_w/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=w&FORMAT=tiles&TILEMATRIX={z}&TILEROW={x}&TILECOL={y}&tk=6db30aa938e5fd3cac17d186cdba9a08");
        tileSet.setMinZoom(0);
        tileSet.setMaxZoom(14);
        Source source = new VectorSource("custom-tile-source", tileSet);
        mapbox.getStyle().addSource(source);
    }
}
