package com.druid.mapcore;

import android.os.Bundle;

import com.druid.mapcore.bean.HeatMapSetBean;
import com.druid.mapcore.bean.ImageRegisterBean;
import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.capture.CaptureDrawLayerApi;
import com.druid.mapcore.circle.CircleDrawLayerApi;
import com.druid.mapcore.cluster.ClusterLayer;
import com.druid.mapcore.fence.api.FenceListLayerApi;
import com.druid.mapcore.fence.api.circle.FenceDrawCircleLayerApi;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByAutoLocationLayer;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByHandLayer;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByInputLayer;
import com.druid.mapcore.fence.api.polygon.FenceDrawPolygonByManualLocationLayer;
import com.druid.mapcore.fence.api.rectangle.FenceDrawRectangleLayerApi;
import com.druid.mapcore.heatmap.HeatMapLayerApi;
import com.druid.mapcore.interfaces.MapLoadedListener;
import com.druid.mapcore.line.LineDrawLayerApi;
import com.druid.mapcore.navigate.NavigateLayerApi;
import com.druid.mapcore.utils.PermissionListener;

import java.util.ArrayList;

public interface DruidMapView {
    void registerMapMarkerImages(ArrayList<ImageRegisterBean> images);

    void loadingMap(MapLoadedListener loadedListener);//加载地图

    void enableGesture(boolean enable);//是否支持手势

    LatLngBean getCameraTarget();//获取中心点

    void setMapLanguage(String lan);//切换语言

    void locationMineEnable(String permissionId, PermissionListener permissionListener,boolean firstLocationMine);//定位到我的否可用

    boolean cameraMineLocation();//定位到我的位置

    void locationEngineEnable(long intervalTime);//定位是否可用

    void cameraAnimationLocationLatLng(LatLngBean latLng);//定位到某个位置

    void changeMapSourceStyleStreet();//图源切换为街景图

    void changeMapSourceStyleSatellite();//图源切换为卫星图

    boolean getStatusMapTile();//是否是瓦片服务

    void zoomMap(boolean isOut);//地图缩放

    void resetMarkerMainClickEvent();//操作marker点击事件

    HeatMapLayerApi drawHeatMapLayer(HeatMapSetBean set);

    ClusterLayer drawClusterLayer();//开启-聚合图层

    FenceDrawPolygonByHandLayer drawFencePolygonByHandLayer();//开启-手绘围栏图层

    FenceDrawPolygonByAutoLocationLayer drawFencePolygonByAutoLocationLayer();//开启-跑马模式绘制围栏图层

    FenceDrawPolygonByManualLocationLayer drawFencePolygonByManualLocationLayer();//开启-跑马测绘模式绘制围栏图层

    FenceDrawPolygonByInputLayer drawFencePolygonByInputLayer();//开启-精确绘制围栏模式图层

    FenceDrawCircleLayerApi drawFenceCircleLayer();//开启-圆形围栏绘制图层

    FenceDrawRectangleLayerApi drawFenceRectangleLayer();//开启-矩形围栏绘制图层

    FenceListLayerApi drawFenceListLayer();//围栏渲染

    LineDrawLayerApi drawLineLayer();//开启-绘制线图层

    NavigateLayerApi drawNavigateLayer();//开启-导航图层

    CircleDrawLayerApi drawCircleLayer();//开启-绘制圆图层

    CaptureDrawLayerApi drawCaptureLayer();//监控图层

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    void onCreate(Bundle outState);

    void onSaveInstanceState(Bundle outState);

    void onDestroy();

    void onLowMemory();
}
