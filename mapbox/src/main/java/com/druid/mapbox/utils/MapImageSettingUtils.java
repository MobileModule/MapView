package com.druid.mapbox.utils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

import com.druid.mapbox.R;
import com.druid.mapcore.bean.ImageRegisterBean;
import com.druid.mapcore.setting.MapSetting;
import com.druid.mapcore.utils.ClusterMarkerImage;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;

public class MapImageSettingUtils extends MapSetting {

    public static void setMapSymbolDefaultResource(Context context, Style style) {
        style.addImage(CLUSTER_MARKER,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(cluster_marker_resource_id)).getBitmap());
        //
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_DIE,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark_die)).getBitmap());
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_0,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark0)).getBitmap());
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_1,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark1)).getBitmap());
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_2,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark2)).getBitmap());
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_3,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark3)).getBitmap());
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_4,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark4)).getBitmap());
        //
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_DIE_BIG,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark_die_large)).getBitmap());
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_0_BIG,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark0_large)).getBitmap());
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_1_BIG,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark1_large)).getBitmap());
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_2_BIG,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark2_large)).getBitmap());
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_3_BIG,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark3_large)).getBitmap());
        style.addImage(ClusterMarkerImage.DEFAULT.CLUSTER_BATTERY_MAKER_4_BIG,
                ((BitmapDrawable) context.getResources()
                        .getDrawable(R.drawable.icon_battery_mark4_large)).getBitmap());
        //
        style.addImage("icon_fence_point_start", ((BitmapDrawable) context.getResources()
                .getDrawable(R.drawable.fence_point_start)).getBitmap());
        style.addImage("icon_fence_point_end", ((BitmapDrawable) context.getResources()
                .getDrawable(R.drawable.fence_point_end)).getBitmap());
    }

    public static void setMapSymbolResource(Context context, Style style, ArrayList<ImageRegisterBean> images) {
        for (ImageRegisterBean image : images) {
            style.addImage(image.imgName,
                    ((BitmapDrawable) context.getResources().getDrawable(image.resId)).getBitmap());
        }
    }
}
