package com.druid.mapcore.fence.layer.circle;

import android.content.Context;
import android.widget.RelativeLayout;

import com.druid.mapcore.fence.layer.FenceDrawLayer;
import com.druid.mapcore.fence.view.circle.CircleView;
import com.druid.mapcore.fence.view.circle.FenceCircleView;
import com.druid.mapcore.interfaces.FenceCircleAreaChangedListener;
import com.druid.mapcore.interfaces.MapOnScaleListener;

public abstract class FenceDrawCircleLayer extends FenceDrawLayer implements MapOnScaleListener {
    public FenceDrawCircleLayer(Context context) {
        super(context);
    }

    protected CircleView circleView;

    protected void addFenceCircleView(RelativeLayout parentView) {
        FenceCircleView fenceCircleView = new FenceCircleView(context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        fenceCircleView.setLayoutParams(lp);
        this.circleView = fenceCircleView;
        parentView.addView(fenceCircleView);
    }

}
