package com.druid.mapcore.setting;

import android.content.Context;

import com.druid.mapcore.R;
import com.druid.mapcore.utils.DimenUtils;

public class MapSetting {
    public static final float FENCE_MARKER_HEIGHT = 66f;//px

    public static final float FENCE_LINE_WIDTH = 3f;

    public static final float FENCE_GOOGLE_LINE_WIDTH = 7f;

    public static float getFenceMarkerOffsetY(Context context, boolean isDefaultMark) {
        if (isDefaultMark) {
            return -DimenUtils.px2dip(context, FENCE_MARKER_HEIGHT) / 2f;
        } else {
            return 0;
        }
    }

    public static final float CLUSTER_MARKER_LARGE_HEIGHT = 104f;//px

    public static float getClusterMarkerLargeOffsetY(Context context, boolean isDefaultMark) {
        if (isDefaultMark) {
            return -DimenUtils.px2dip(context, CLUSTER_MARKER_LARGE_HEIGHT) / 2f;
        } else {
            return 0;
        }
    }

    public static final float CLUSTER_MARKER_NORMAL_HEIGHT = 60f;//px

    public static float getClusterMarkerNormalOffsetY(Context context, boolean isDefaultMark) {
        if (isDefaultMark) {
            return -DimenUtils.px2dip(context, CLUSTER_MARKER_NORMAL_HEIGHT) / 2f;
        } else {
            return 0;
        }
    }

    public static final float CLUSTER_MARKER_HEIGHT = 87f;//px

    public static float getClusterMarkerOffsetY(Context context) {
        return -DimenUtils.px2dip(context, CLUSTER_MARKER_HEIGHT) / 2f;
    }

    public static final int MAP_BOUNDS_PADDING = 100;

    public static int getFencePolylineBorderColor(Context context) {
        return context.getResources().getColor(R.color.fence_polyline);
    }

    public static int getFencePolygonBorderColor(Context context) {
        return context.getResources().getColor(R.color.fence_polygon_border_line);
    }

    public static int getFencePolygonFillColor(Context context) {
        return context.getResources().getColor(R.color.fence_polygon_fill_line);
    }

    public static int getLocationMarkerBorderColor(Context context) {
        return context.getResources().getColor(R.color.location_border_line);
    }

    public static int getLocationMarkerFillColor(Context context) {
        return context.getResources().getColor(R.color.location_fill_line);
    }

    public static final String CLUSTER_MARKER = "CLUSTER_MARKER";
    public static int cluster_marker_resource_id = R.drawable.cluster_marker_default;

    public static void setClusterMarkerResource(int resource) {
        cluster_marker_resource_id = resource;
    }

    //
    public static final int ZINDEX_GEOMETRY = 97;
    public static final int ZINDEX_GEOMETRY_MARKER = 98;
}
