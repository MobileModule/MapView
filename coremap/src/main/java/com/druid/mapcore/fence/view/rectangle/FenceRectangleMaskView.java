package com.druid.mapcore.fence.view.rectangle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.druid.mapcore.R;

public class FenceRectangleMaskView extends View implements FenceRectangleMask{
    private Context context = null;
    private Canvas canvas = null;
    public Point center = null;
    public Point up_left = null;
    public Point up_right = null;
    public Point bottom_left = null;
    public Point bottom_right = null;

    public FenceRectangleMaskView(Context context) {
        super(context);
        this.context = context;
    }

    public FenceRectangleMaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public FenceRectangleMaskView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    public void setRecMask(Point center, Point up_left, Point up_right, Point bottom_left, Point bottom_right) {
        this.center = center;
        this.up_left = up_left;
        this.up_right = up_right;
        this.bottom_left = bottom_left;
        this.bottom_right = bottom_right;
        drawRect();
    }

    private void drawRect() {
        if (canvas != null) {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        if (center != null) {
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

            //屏幕绘制
            int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            Paint allPaint = new Paint();
            allPaint.setAntiAlias(true);
            allPaint.setColor(context.getResources().getColor(R.color.fence_rectangle_mask));
            RectF rectF = new RectF(0, 0, width, height);
            canvas.drawRect(rectF, allPaint);

            //矩形
            allPaint.setColor(context.getResources().getColor(R.color.fence_rectangle_fill_line));
            allPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawRect(up_left.x, up_left.y, bottom_right.x, bottom_right.y, allPaint);
            allPaint.setXfermode(null);
            canvas.restoreToCount(sc);

            //
            Paint rectPaintStorke = new Paint();
            rectPaintStorke.setAntiAlias(true);
            rectPaintStorke.setStrokeWidth(5f);
            rectPaintStorke.setStyle(Paint.Style.STROKE);
            rectPaintStorke.setColor(context.getResources().getColor(R.color.fence_rectangle_border_line));
            canvas.drawRect(up_left.x, up_left.y, bottom_right.x, bottom_right.y, rectPaintStorke);
        }
    }

    @Override
    public void setRectangleMaskViewVisible(boolean visible) {
        if(visible){
            setVisibility(View.VISIBLE);
        }else {
            setVisibility(View.GONE);
        }
    }
}
