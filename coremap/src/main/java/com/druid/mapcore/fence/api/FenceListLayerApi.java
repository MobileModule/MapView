package com.druid.mapcore.fence.api;

import com.druid.mapcore.bean.FenceCoreBean;
import com.druid.mapcore.interfaces.FenceClickListener;
import com.druid.mapcore.layer.LayerApi;

import java.util.ArrayList;

public interface FenceListLayerApi<T1, T2> extends LayerApi<T1, T2> {
    void setFenceSource(ArrayList<FenceCoreBean> fenceSource,boolean supportClick);

    void clearFenceSelected();

    void setFenceClickListener(FenceClickListener listener);

    void setFenceSelected(String fence_id);

    String getSelectedFenceId();
}
