package com.druid.mapcore.fence.view.marker;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.druid.mapcore.R;
import com.druid.mapcore.bean.FenceCoreBean;

public class FenceTitleMarkView extends LinearLayout {
    public FenceTitleMarkView(Context context) {
        super(context);
        initView();
    }

    public FenceTitleMarkView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FenceTitleMarkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public FenceTitleMarkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    TextView tv;
    void initView() {
        inflate(getContext(), R.layout.layout_map_text, this);
        tv = (TextView) findViewById(R.id.tv_fence_name);
    }

    public FenceTitleMarkView setFenceBean(FenceCoreBean fence){
        tv.setText(fence.name);
        Drawable drawableLeft = null;
        if (fence.msg_type == 2) {
            drawableLeft = getResources().getDrawable(R.drawable.icon_fence_text_out);
        } else {
            drawableLeft = getResources().getDrawable(R.drawable.icon_fence_text_in);
        }
        tv.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
        tv.setCompoundDrawablePadding((int) getResources().getDimension(R.dimen.fence_title_padding));
        return this;
    }

    public Bitmap getBitmapView(){
        tv.setDrawingCacheEnabled(true);
        tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());

        //千万别忘最后一步
        tv.destroyDrawingCache();
        return bitmap;
    }
}
