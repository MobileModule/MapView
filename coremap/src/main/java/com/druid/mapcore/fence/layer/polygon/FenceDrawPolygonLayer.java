package com.druid.mapcore.fence.layer.polygon;

import android.content.Context;
import android.widget.RelativeLayout;

import com.druid.mapcore.bean.LatLngBean;
import com.druid.mapcore.fence.layer.FenceDrawLayer;
import com.druid.mapcore.fence.view.marker.FenceCenterMark;
import com.druid.mapcore.fence.view.marker.FenceCenterMarkView;
import com.druid.mapcore.utils.MapUtils;

public abstract class FenceDrawPolygonLayer<T> extends FenceDrawLayer<T> {
    protected static final int LocationValidLength = 5;//点位点有效距离（米）
    protected boolean drawComplete = false;

    public FenceDrawPolygonLayer(Context context) {
        super(context);
    }

    protected FenceCenterMark centerMarkView;

    protected void addFenceCenterMarkerView(RelativeLayout parentView) {
        if (centerMarkView == null) {
            FenceCenterMarkView markView = new FenceCenterMarkView(context);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            markView.setLayoutParams(lp);
            this.centerMarkView = markView;
            this.centerMarkView.setCenterMarkerViewVisible(false);
            parentView.addView(markView);
        }
    }

    protected LatLngBean preLocation;

    public boolean checkValidLocationPoint(LatLngBean curLatLng) {
        if (preLocation == null) {
            this.preLocation = curLatLng;
            return true;
        }
        if (MapUtils.computeDistanceBetween(curLatLng, preLocation) > LocationValidLength) {
            this.preLocation = curLatLng;
            return true;
        }
        return false;
    }

    protected LatLngBean curLocation;

    public LatLngBean getManualLocation() {
        if (curLocation != null) {
           if(checkValidLocationPoint(curLocation)){
               return curLocation;
           }
        }
        return null;
    }

}
