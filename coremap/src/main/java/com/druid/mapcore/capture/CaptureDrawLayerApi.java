package com.druid.mapcore.capture;

import android.graphics.drawable.Drawable;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.bean.MarkerBean;
import com.druid.mapcore.interfaces.CaptureClickListener;
import com.druid.mapcore.layer.LayerApi;

import java.util.ArrayList;

public interface CaptureDrawLayerApi <T1, T2> extends LayerApi<T1, T2> {
    void setLineSource(ArrayList<LatLngBean> points);//设置数据源
    void setMarkerSource(ArrayList<MarkerBean> markers);
    void setCaptureClickListener(CaptureClickListener listener);
    CaptureDrawLayerApi setLineColor(int color, Drawable captureDrawable);//设置线颜色,设置摄像头颜色
}
