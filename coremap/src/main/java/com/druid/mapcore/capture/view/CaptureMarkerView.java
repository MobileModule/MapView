package com.druid.mapcore.capture.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.druid.mapcore.R;
import com.druid.mapcore.bean.MarkerBean;

public class CaptureMarkerView extends LinearLayout {
    public CaptureMarkerView(Context context) {
        super(context);
        initView();
    }

    public CaptureMarkerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CaptureMarkerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public CaptureMarkerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    View view_parent;
    TextView tv_capture_index;

    void initView() {
        inflate(getContext(), R.layout.capture_layout, this);
        view_parent = findViewById(R.id.view_parent);
        tv_capture_index = (TextView) findViewById(R.id.tv_capture_index);
    }

    public CaptureMarkerView setMarker(MarkerBean marker) {
        tv_capture_index.setText(marker.title);
        return this;
    }

    public Bitmap getBitmapView() {
        view_parent.setDrawingCacheEnabled(true);
        view_parent.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view_parent.layout(0, 0, view_parent.getMeasuredWidth(), view_parent.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view_parent.getDrawingCache());

        //千万别忘最后一步
        view_parent.destroyDrawingCache();
        return bitmap;
    }
}
