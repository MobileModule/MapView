package com.druid.mapcore.fence.view.marker;

import com.druid.mapcore.bean.LatLngBean;

public interface FenceCenterMark {
    void setCenterMarkerViewVisible(boolean visible);
    void setMarkInfoWindowVisible(boolean visible);
    void setCenterMarkerPosition(LatLngBean latLng);
    void setOnCenterMarkerEditListener(FenceCenterMarkView.ClickCenterMarkerEditListener listener);
    void setClickEnabled(boolean enabled);
}
