package com.druid.mapcore.fence.view.circle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.druid.mapcore.R;
import com.druid.mapcore.utils.MapSettingConstantUtils;

public class FenceCircleView extends View implements CircleView{
    private Context context;
    private Canvas canvas;
    private float width;
    private float height;
    private float radius;

    public FenceCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public FenceCircleView(Context context) {
        super(context);
        this.context = context;
    }

    public FenceCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.canvas = canvas;
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        width = canvas.getWidth();
        height = canvas.getHeight();
        radius = width / 4;

        //屏幕绘制
        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        Paint allPaint = new Paint();
        allPaint.setAntiAlias(true);
        allPaint.setColor(context.getResources().getColor(R.color.fence_circle_mask));
        RectF rectF = new RectF(0, 0, width, height);
        canvas.drawRect(rectF, allPaint);

        //绘制圆形
        allPaint.setColor(context.getResources().getColor(R.color.fence_circle_fill_line));
        allPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawCircle(width / 2, height / 2, radius, allPaint);
        allPaint.setXfermode(null);
        canvas.restoreToCount(sc);

        //绘制内圆
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(context.getResources().getColor(R.color.fence_circle_border_line));
        paint.setStrokeWidth(MapSettingConstantUtils.getRectangleWidthMask(getContext()));
        canvas.drawCircle(width / 2, height / 2, radius, paint);
    }

}