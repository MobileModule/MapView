package com.druid.mapcore.interfaces;

import com.druid.mapcore.bean.ClusterMarkerBean;

public interface MapBulkClickListener {
    void mapBulkPointClick(int index, ClusterMarkerBean marker);
}
