package com.druid.mapcore.interfaces;


import com.druid.mapcore.bean.ClusterMarkerBean;

import java.util.ArrayList;

public interface MapClusterClickListener {
    void mapMarkerClusterClick(ArrayList<ClusterMarkerBean> markers);

    void mapMarkerSingleClick(int index, ClusterMarkerBean marker);

    void mapMarkerSingleRemove(int index);
}
