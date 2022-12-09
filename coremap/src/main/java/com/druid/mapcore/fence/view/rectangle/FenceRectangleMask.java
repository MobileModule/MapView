package com.druid.mapcore.fence.view.rectangle;

import android.graphics.Point;

public interface FenceRectangleMask {
    void setRectangleMaskViewVisible(boolean visible);

    void setRecMask(Point center, Point up_left, Point up_right, Point bottom_left, Point bottom_right);
}
