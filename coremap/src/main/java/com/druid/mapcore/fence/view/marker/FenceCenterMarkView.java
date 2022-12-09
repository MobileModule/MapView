package com.druid.mapcore.fence.view.marker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.druid.mapcore.R;
import com.druid.mapcore.bean.LatLngBean;

public class FenceCenterMarkView extends LinearLayout implements FenceCenterMark,
        View.OnClickListener {
    public static final String TAG = FenceCenterMarkView.class.getName();

    public interface ClickCenterMarkerEditListener {
        void clickCenterMarkerEdit(LatLngBean latLng);
    }

    public FenceCenterMarkView(Context context) {
        super(context);
        initView();
    }

    public FenceCenterMarkView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTypedArray(context, attrs);
        initView();
    }

    public FenceCenterMarkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs);
        initView();
    }

    void initTypedArray(Context context, AttributeSet attrs) {
        if (attrs != null) {

        }
    }

    private View view_marker;
    private View view_marker_infowindow;
    private TextView tv_lng, tv_lat;
    private View ll_editor;
    private View view_bottom_placeholder;

    void initView() {
        inflate(getContext(), R.layout.marker_center_view, this);
        view_marker = findViewById(R.id.view_marker);
        view_marker_infowindow = findViewById(R.id.view_marker_infowindow);
        view_marker_infowindow.setVisibility(GONE);
        tv_lng = findViewById(R.id.tv_lng);
        tv_lat = findViewById(R.id.tv_lat);
        ll_editor = findViewById(R.id.ll_editor);
        ll_editor.setOnClickListener(this);
        findViewById(R.id.img_marker).setOnClickListener(this);
        view_bottom_placeholder = findViewById(R.id.view_bottom_placeholder);
        view_bottom_placeholder.setVisibility(GONE);
        setPlaceHolderHeight();
    }

    @Override
    public void setCenterMarkerViewVisible(boolean visible) {
        if (visible) {
            view_marker.setVisibility(VISIBLE);
        } else {
            view_marker.setVisibility(GONE);
        }
    }

    @Override
    public void setMarkInfoWindowVisible(boolean visible) {
        if (visible) {
            if (view_marker_infowindow.getVisibility() != VISIBLE ||
                    view_bottom_placeholder.getVisibility() != VISIBLE) {
                view_marker_infowindow.setVisibility(View.VISIBLE);
                view_bottom_placeholder.setVisibility(VISIBLE);
            }
        } else {
            if (view_marker_infowindow.getVisibility() != GONE ||
                    view_bottom_placeholder.getVisibility() != GONE) {
                view_marker_infowindow.setVisibility(GONE);
                view_bottom_placeholder.setVisibility(GONE);
            }
        }
    }

    private LatLngBean centerPosition;

    @Override
    public void setCenterMarkerPosition(LatLngBean latLng) {
        this.centerPosition = latLng;
        tv_lat.setText(latLng.getLat() + "");
        tv_lng.setText(latLng.getLng() + "");
    }

    private ClickCenterMarkerEditListener centerMarkerEditListener;

    @Override
    public void setOnCenterMarkerEditListener(ClickCenterMarkerEditListener listener) {
        this.centerMarkerEditListener = listener;
    }

    private boolean clickEnabled = true;

    @Override
    public void setClickEnabled(boolean enabled) {
        this.clickEnabled = enabled;
        if (!clickEnabled) {
            view_marker_infowindow.setVisibility(GONE);
            view_bottom_placeholder.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (clickEnabled) {
            if (v.getId() == R.id.img_marker) {
                if (view_marker_infowindow.getVisibility() == VISIBLE) {
                    view_marker_infowindow.setVisibility(GONE);
                    view_bottom_placeholder.setVisibility(GONE);
                } else {
                    view_marker_infowindow.setVisibility(View.VISIBLE);
                    view_bottom_placeholder.setVisibility(VISIBLE);
                }
            }
            if (v.getId() == R.id.ll_editor) {
                setMarkInfoWindowVisible(false);
                if (centerMarkerEditListener != null) {
                    centerMarkerEditListener.clickCenterMarkerEdit(centerPosition);
                }
            }
        }
    }

    private int height = 0;

    private void setPlaceHolderHeight() {
        int widthAll = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int heightAll = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view_marker_infowindow.measure(widthAll, heightAll);
        height = view_marker_infowindow.getMeasuredHeight();
        LinearLayout.LayoutParams layoutParams = (LayoutParams) view_bottom_placeholder.getLayoutParams();
        layoutParams.height = height;
        view_bottom_placeholder.setLayoutParams(layoutParams);
    }

}
