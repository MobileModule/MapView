package com.druid.mapgoogle.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.druid.mapcore.setting.MapConstant;
import com.druid.mapgoogle.cluster.GoogleClusterRenderView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm;

public class MapConstantUtils extends MapConstant {

    public static void configMapUISetting(GoogleMap googleMap) {
        googleMap.setMaxZoomPreference(MapConstantUtils.MAP_MAX_ZOOM);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setCompassEnabled(true);
        uiSettings.setIndoorLevelPickerEnabled(false);//图层选择
        uiSettings.setMapToolbarEnabled(false);//外部工具
        uiSettings.setRotateGesturesEnabled(false);// 禁用旋转
        uiSettings.setTiltGesturesEnabled(false);//禁用倾斜手势
    }

    public static void enableGesture(GoogleMap googleMap, boolean enable) {
        UiSettings uiSettings = googleMap.getUiSettings();
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
                                          GoogleMap googleMap) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int widthDp = (int) (metrics.widthPixels / metrics.density);
        int heightDp = (int) (metrics.heightPixels / metrics.density);
        mClusterManager.setAnimation(false);
        mClusterManager.setAlgorithm(new NonHierarchicalViewBasedAlgorithm<>(widthDp, heightDp));
        mClusterManager.setRenderer(new GoogleClusterRenderView(context, googleMap, mClusterManager));
    }
}
