package com.druid.mapcore.fence.layer.rectangle;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.widget.RelativeLayout;

import com.druid.mapcore.fence.layer.FenceDrawLayer;
import com.druid.mapcore.fence.view.rectangle.FenceRectangleCrop;
import com.druid.mapcore.fence.view.rectangle.FenceRectangleCropView;
import com.druid.mapcore.fence.view.rectangle.FenceRectangleMask;
import com.druid.mapcore.fence.view.rectangle.FenceRectangleMaskView;
import com.druid.mapcore.interfaces.FenceRectangleCropLocationListener;
import com.druid.mapcore.utils.MapLog;

public abstract class FenceDrawRectangleLayer extends FenceDrawLayer implements
        FenceRectangleCropLocationListener {
    public static final String TAG = FenceDrawRectangleLayer.class.getName();

    public FenceDrawRectangleLayer(Context context) {
        super(context);
    }

    private FenceRectangleCrop fenceRectangleCrop;
    private FenceRectangleMask fenceRectangleMask;
    protected boolean canDrawRectangleFence = false;

    protected void addFenceRectangleView(RelativeLayout parentView) {
        FenceRectangleCropView fenceRectangleCropView = new FenceRectangleCropView(context);
        RelativeLayout.LayoutParams lpCrop = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        lpCrop.addRule(RelativeLayout.CENTER_IN_PARENT);
        fenceRectangleCropView.setLayoutParams(lpCrop);
        this.fenceRectangleCrop = fenceRectangleCropView;
        this.fenceRectangleCrop.setRectangleCropLocationListener(this);
        fenceRectangleCropView.setVisibility(View.VISIBLE);
        parentView.addView(fenceRectangleCropView);
        //
        FenceRectangleMaskView fenceRectangleMaskView = new FenceRectangleMaskView(context);
        RelativeLayout.LayoutParams lpMask = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        lpMask.addRule(RelativeLayout.CENTER_IN_PARENT);
        fenceRectangleMaskView.setLayoutParams(lpMask);
        fenceRectangleMaskView.setVisibility(View.GONE);
        this.fenceRectangleMask = fenceRectangleMaskView;
        parentView.addView(fenceRectangleMaskView);
    }

    protected Point center = null;
    protected Point up_left = null;
    protected Point up_right = null;
    protected Point bottom_left = null;
    protected Point bottom_right = null;

    @Override
    public void fenceRectangleCropLocationRectChanged(int startX, int startY, int endX, int endY) {
        String formatStr = String.format("startX:%s,startY:%s,endX:%s,endY:%s", startX, startY, endX, endY);
        MapLog.warning(TAG, formatStr);
        this.center = new Point(Math.abs(endX - startX) / 2, Math.abs(endY - startY) / 2);
        this.up_left = new Point(startX, startY);
        this.up_right = new Point(endX, startY);
        this.bottom_left = new Point(startX, endY);
        this.bottom_right = new Point(endX, endY);
    }

    protected void setFenceRectangleCropViewVisible() {
        if (fenceRectangleCrop != null) {
            fenceRectangleCrop.setRectangleCropViewVisible(true);
        }
        if (fenceRectangleMask != null) {
            fenceRectangleMask.setRectangleMaskViewVisible(false);
        }
        this.canDrawRectangleFence = false;
    }

    protected void setFenceRectangleCropViewGone() {
        if (fenceRectangleCrop != null) {
            fenceRectangleCrop.setRectangleCropViewVisible(false);
        }
        if (fenceRectangleMask != null) {
            fenceRectangleMask.setRectangleMaskViewVisible(true);
            fenceRectangleMask.setRecMask(center, up_left, up_right, bottom_left, bottom_right);
        }
        this.canDrawRectangleFence = true;
    }
}
