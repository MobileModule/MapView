package com.druid.mapcore.navigate;

import android.graphics.drawable.Drawable;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.interfaces.MapLocationChangedListener;
import com.druid.mapcore.layer.LayerApi;

public interface NavigateLayerApi<T1, T2> extends LayerApi<T1, T2> {
    void setLineSource(LatLngBean point);//设置数据源

    NavigateLayerApi setLineColor(int color, Drawable endMarker);

    void setLocationFollow(boolean follow);//位置是否跟随地图

    void setLocationChangedListener(MapLocationChangedListener listener);
}
