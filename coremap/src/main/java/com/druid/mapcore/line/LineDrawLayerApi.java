package com.druid.mapcore.line;

import android.graphics.drawable.Drawable;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.interfaces.LineTrackMoveListener;
import com.druid.mapcore.layer.LayerApi;

import java.util.ArrayList;

public interface LineDrawLayerApi<T1, T2> extends LayerApi<T1, T2> {
    void setLineSource(ArrayList<LatLngBean> points);//设置数据源
    LineDrawLayerApi setLineColor(int color,Drawable arrowDrawable);//设置线颜色,设置线箭头图片
    void markStartEndPosition(Drawable startDrawable,Drawable endDrawable);//是否标记起点和终点
    void openArrowLine();//打开箭头绘制

    /**
     * 是否播放轨迹
     * @param play true:play false:pause
     * @return 是执行成功
     */
    boolean playTracker(boolean play);

    void setPlayTrackListener(LineTrackMoveListener listener);
}
