package com.druid.mapcore.setting;

import android.text.TextUtils;

public class MapConstant {
    public static final String LAYER_DEM_ID = "hillshade-layer";
    public static final String SOURCE_DEM_ID = "hillshade-source";

    public static final String SOURCE_DEM_URL = "mapbox://mapbox.terrain-rgb";
    public static final String LAYER_BELOW_ID = "waterway-river-canal";

    //聚合
    public static final String SOURCE_CLUSTER_ID = "source-cluster-id";
    public static final String LAYER_CLUSTERED_ID = "clustered-layer-id";
    public static final String LAYER_UNCLUSTERED_ID = "unclustered-layer-id";
    //未聚合
    public static final String SOURCE_MARKER_ID = "source-marker-id";
    public static final String LAYER_MAKER_ID = "unclustered-marker-layer-id";
    //点击
    public static final String SOURCE_MARKER_CLICK_ID = "source-marker-click-id";
    public static final String LAYER_MAKER_CLICK_ID = "unclustered-marker-click-layer-id";

    public static final String FIELD_CLUSTER_COUNT = "point_count";//系统自带
    public static final String FIELD_ID = "field_object_id";
    public static final String FIELD_RADIUS = "field_object_radius";
    public static final String FIELD_INDEX = "field_object_index";
    public static final String FIELD_IMAGE = "field_object_image";
    public static final String FIELD_IMAGE_NORMAL = "field_object_image_normal";
    public static final String FIELD_IMAGE_BIG = "field_object_image_big";
    public static final String FIELD_COLOR = "field_color";
    public static final String FIELD_PLAY_COLOR = "field_play_color";
    public static final String FIELD_ARROW_IMAGE = "field_arrow_image";
    public static final String FIELD_NAVIGATE_END_IMAGE = "field_navigate_end_image";
    public static final String FIELD_BEARING = "field_bearing";
    public static final String FIELD_MOVE_POSITION = "field_move_position";
    //fence polygon draw
    public static final String SOURCE_FENCE_POLYGON_LINE_ID = "source-fence-polygon-line-id";
    public static final String LAYER_FENCE_POLYGON_LINE_ID = "layer-fence-polygon-line-id";

    public static final String SOURCE_FENCE_POLYGON_POINT_ID = "source-fence-polygon-point-id";
    public static final String LAYER_FENCE_POLYGON_POINT_ID = "layer-fence-polygon-point-id";
    public static final String FIELD_FENCE_IMAGE = "field_fence_image";
    public static final String FIELD_FENCE_COLOR = "field_fence_color";

    public static final String SOURCE_FENCE_POLYGON_FILL_ID = "source-fence-polygon-fill-id";
    public static final String LAYER_FENCE_POLYGON_FILL_ID = "layer-fence-polygon-fill-id";
    //fence list
    public static final String SOURCE_FENCE_POLYGON_ID = "source-fence-polygon-id";
    public static final String LAYER_FENCE_POLYGON_ID = "layer-fence-polygon-id";

    public static final String SOURCE_FENCE_CIRCLE_ID = "source-fence-circle-id";
    public static final String LAYER_FENCE_CIRCLE_ID = "layer-fence-circle-id";

    public static final String SOURCE_FENCE_TITLE_ID = "source-fence-title-id";
    public static final String LAYER_FENCE_TITLE_ID = "layer-fence-title-id";
    //line
    public static final String SOURCE_POLYGON_LINE_ID = "source-polygon-line-id";
    public static final String LAYER_POLYGON_LINE_ID = "layer-polygon-line-id";
    public static final String SOURCE_POLYGON_POINT_ID = "source-polygon-point-id";
    public static final String LAYER_POLYGON_POINT_ID = "layer-polygon-point-id";
    public static final String SOURCE_LINE_ARROW_ID = "source-polygon-arrow-id";
    public static final String LAYER_LINE_ARROW_ID = "layer-polygon-arrow-id";
    public static final String SOURCE_PLAY_POINT_ID = "source-play-point-id";
    public static final String LAYER_PLAY_POINT_ID = "layer-play-point-id";
    public static final String SOURCE_PLAY_LINE_ID = "source-play-line-id";
    public static final String LAYER_PLAY_LINE_ID = "layer-play-line-id";
    //capture
    public static final String SOURCE_CAPTURE_LINE_ID = "source-capture-line-id";
    public static final String LAYER_CAPTURE_LINE_ID = "layer-capture-line-id";
    public static final String SOURCE_CAPTURE_POINT_ID = "source-capture-point-id";
    public static final String LAYER_CAPTURE_POINT_ID = "layer-capture-point-id";

    //polygon
    public static final String SOURCE_POLYGON_FILL_ID = "source-polygon-fill-id";
    public static final String LAYER_POLYGON_FILL_ID = "layer-polygon-fill-id";
    public static final String SOURCE_POLYGON_FILL_LINE_ID = "source-polygon-fill-line-id";
    public static final String LAYER_POLYGON_FILL_LINE_ID = "layer-polygon-fill-line-id";

    //navigate
    public static final String SOURCE_NAVIGATE_LINE_ID = "source-navigate-line-id";
    public static final String LAYER_NAVIGATE_LINE_ID = "layer-navigate-line-id";

    //绘制矩形围栏中心点
    public static final String SOURCE_FENCE_CIRCLE_CENTER_ID = "source-fence-circle-center-id";
    public static final String LAYER_FENCE_CIRCLE_CENTER_ID = "layer-fence-circle-center-id";

    //热力图
    public static final String SOURCE_HEAT_MAP_ID = "source-heatmap-id";
    public static final String LAYER_HEAT_MAP_ID = "layer-heatmap-id";

    public static final int ZINDEX_ARROW = 90;
    public static final int ZINDEX_GEOMETRY = 97;
    public static final int ZINDEX_MARKER = 99;
    public static final int ZINDEX_MARKER_LOCATION = 90;
    public static final float MAP_MAX_ZOOM = 20f;

    public static final String CLUSTER_MARKER_NORMAL = "marker-normal";
    public static final String CLUSTER_MARKER_LARGE = "marker-large";

    public static final String getMarkerTitle(boolean largeMarker, int index) {
        String title = CLUSTER_MARKER_NORMAL;
        if (largeMarker) {
            title = CLUSTER_MARKER_LARGE;
        }
        title = title + "-" + index;
        return title;
    }

    public static final int getMarkerIndexFromTitle(String title) {
        int index = -1;
        if (title.contains("-")) {
            String[] titleArray = title.split("-");
            if (titleArray.length > 2) {
                if (title.contains(CLUSTER_MARKER_LARGE) ||
                        title.contains(CLUSTER_MARKER_NORMAL)) {
                    String indexStr = titleArray[2];
                    try {
                        index = Integer.parseInt(indexStr);
                    } catch (Exception ex) {
                        ex.getMessage();
                    }
                }
            }
        }
        return index;
    }

    public static final boolean getMarkerISLargeFormTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            if (title.contains(CLUSTER_MARKER_LARGE)) {
                return true;
            }
        }
        return false;
    }

    public static final boolean getMarkerISNormalFromTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            if (title.contains(CLUSTER_MARKER_NORMAL)) {
                return true;
            }
        }
        return false;
    }

}
