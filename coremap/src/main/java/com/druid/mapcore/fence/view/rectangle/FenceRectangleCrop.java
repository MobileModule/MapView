package com.druid.mapcore.fence.view.rectangle;

import com.druid.mapcore.interfaces.FenceRectangleCropLocationListener;

public interface FenceRectangleCrop {
    void setRectangleCropViewVisible(boolean visible);

    void setRectangleCropLocationListener(FenceRectangleCropLocationListener locationListener);
}
