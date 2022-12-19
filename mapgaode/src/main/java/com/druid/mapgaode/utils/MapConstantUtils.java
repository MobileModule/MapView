package com.druid.mapgaode.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.amap.api.maps.AMap;
import com.amap.api.maps.UiSettings;
import com.druid.mapcore.setting.MapConstant;
import com.druid.mapgaode.cluster.GoogleClusterRenderView;

public class MapConstantUtils extends MapConstant {

    public static void configMapUISetting(AMap amap) {
        UiSettings uiSettings = amap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setCompassEnabled(true);
        uiSettings.setIndoorSwitchEnabled(false);//图层选择
        uiSettings.setRotateGesturesEnabled(false);// 禁用旋转
        uiSettings.setTiltGesturesEnabled(false);//禁用倾斜手势
    }

    public static void enableGesture(AMap amap, boolean enable) {
        UiSettings uiSettings = amap.getUiSettings();
        if (enable) {
            uiSettings.setScrollGesturesEnabled(true);
            uiSettings.setZoomGesturesEnabled(true);
        } else {
            uiSettings.setScrollGesturesEnabled(false);
            uiSettings.setZoomGesturesEnabled(false);
            uiSettings.setAllGesturesEnabled(false);
        }
    }

    public static void configClusterStyle(Context context, ClusterManager mClusterManager,
                                          AMap amap) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int widthDp = (int) (metrics.widthPixels / metrics.density);
        int heightDp = (int) (metrics.heightPixels / metrics.density);
        mClusterManager.setAnimation(false);
        mClusterManager.setAlgorithm(new NonHierarchicalViewBasedAlgorithm<>(widthDp, heightDp));
        mClusterManager.setRenderer(new GoogleClusterRenderView(context, amap, mClusterManager));
    }
}
