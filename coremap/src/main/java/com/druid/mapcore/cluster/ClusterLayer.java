package com.druid.mapcore.cluster;

import android.graphics.drawable.Drawable;

import com.druid.mapcore.bean.ClusterMarkerBean;
import com.druid.mapcore.interfaces.ClusterStatusChangedListener;
import com.druid.mapcore.interfaces.MapClusterClickListener;
import com.druid.mapcore.layer.LayerApi;

import java.util.ArrayList;

public interface ClusterLayer<T1, T2> extends LayerApi<T1, T2> {

    void enableClusterMarker(MapClusterClickListener clusterClickListener,
                             boolean cluster, boolean usedBulkPointLayer);//聚合是否可用

    void setClusterSource(ArrayList<ClusterMarkerBean> sources,boolean flatZoom,
                          boolean defaultIcon,boolean needOffset);//聚合点设置源数据,

    boolean changeMarkerCluster();//切换聚合点显示

    boolean searchMarkerCluster(String marker_id);//搜索聚合点，并且放大

    void resetMarkerLarge();//复位marker变大

    void setClusterStatusChangedListener(ClusterStatusChangedListener listener);

    void setMapPositionDrawable(Drawable startNormalDrawable, Drawable startBigDrawable,
                                Drawable centerNormalDrawable, Drawable centerBigDrawable,
                                Drawable endNormalDrawable, Drawable endBigDrawable);//聚合图标点设置
}
