package com.druid.mapcore.fence.view.rectangle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.druid.mapcore.R;
import com.druid.mapcore.interfaces.FenceRectangleCropLocationListener;

public class FenceRectangleCropView extends View implements FenceRectangleCrop{
    private static final String TAG = "MapCropView";
    private Context context = null;
    private FenceRectangleCropLocationListener locationListener;/*listen to the Rect */
    private onChangeLocationlistener changeLocationlistener;/*listening position changed */
    private int MODE;
    private static final int MODE_OUTSIDE = 0x000000aa;/*170*/
    private static final int MODE_INSIDE = 0x000000bb;/*187*/
    private static final int MODE_POINT = 0X000000cc;/*204*/
    private static final int MODE_ILLEGAL = 0X000000dd;/*221*/
    private static final int minWidth = 100;/*the minimum width of the rectangle*/
    private static final int minHeight = 200;/*the minimum height of the rectangle*/
    private static final int START_X = 200;
    private static final int START_Y = 200;
    private static final float EDGE_WIDTH = 1.8f;
    private static final int ACCURACY = 160;/*touch accuracy*/
    private int pointPosition;/*vertex of a rectangle*/
    private int sX;/*start X location*/
    private int sY;/*start Y location*/
    private int eX;/*end X location*/
    private int eY;/*end Y location*/
    private int pressX;/*X coordinate values while finger press*/
    private int pressY;/*Y coordinate values while finger press*/
    private int memonyX;/*the last time the coordinate values of X*/
    private int memonyY;/*the last time the coordinate values of Y*/
    private int coverWidth = 300;/*width of selection box*/
    private int coverHeight = 400;/*height of selection box*/
    private Paint mPaint;
    private Paint mPaintLine;
    private Bitmap mBitmapCover;
    private Bitmap mBitmapRectBlack;
    private PorterDuffXfermode xfermode;/*paint mode*/

    public FenceRectangleCropView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public FenceRectangleCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public FenceRectangleCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private int fence_border_color = 0;
    private int fence_fill_color = 0;

    @SuppressWarnings("deprecation")
    private void init() {
        fence_border_color = getResources().getColor(R.color.fence_rectangle_border_line);
        fence_fill_color = getResources().getColor(R.color.fence_rectangle_fill_line);

        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int width = manager.getDefaultDisplay().getWidth();
        int height = getScreentHeight();//manager.getDefaultDisplay().getHeight();

//        Resources resources = this.getResources();
//        DisplayMetrics dm = resources.getDisplayMetrics();
//        int width = dm.widthPixels;
//        int height = dm.heightPixels;

        sX = width / 6;
        sY = height / 4;

        coverWidth = sX * 4;
        coverHeight = sY;
//        sY=0;

        mBitmapCover = makeBitmap(width, height, 0x00000000, 0, 0);//0x5A000000, 0, 0);
        mBitmapRectBlack = makeBitmap(coverWidth, coverHeight, fence_fill_color, coverWidth, coverHeight);//0xff000000
        eX = sX + coverWidth;
        eY = sY + coverHeight;
        pressX = 0;
        pressY = 0;
        xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaintLine = new Paint();
        mPaintLine.setColor(fence_border_color);
        mPaintLine.setStrokeWidth(3.0f);
    }

    /*生成bitmap*/
    private Bitmap makeBitmap(int mwidth, int mheight, int resource, int staX, int staY) {
        Bitmap bm = Bitmap.createBitmap(mwidth, mheight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(resource);
        c.drawRect(staX, staY, mwidth, mheight, p);
        return bm;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setFilterBitmap(false);
        int sc = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null,Canvas.ALL_SAVE_FLAG);
//                Canvas. MATRIX_SAVE_FLAG |
//                        Canvas.CLIP_SAVE_FLAG |
//                        Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
//                        Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
//                        Canvas.CLIP_TO_LAYER_SAVE_FLAG);
        canvas.drawBitmap(mBitmapCover, 0, 0, mPaint);
        mPaint.setXfermode(xfermode);
        canvas.drawBitmap(mBitmapRectBlack, sX, sY, mPaint);
        if (locationListener != null) {
            int maxX = Math.max(sX, eX);
            int minX = Math.min(sX, eX);
            int maxY = Math.max(sY, eY);
            int minY = Math.min(sY, eY);
            locationListener.fenceRectangleCropLocationRectChanged(minX, minY, maxX, maxY);
//            locationListener.locationRect(sX, sY, eX, eY);
        }
        mPaint.setXfermode(null);
        canvas.restoreToCount(sc);

        drawRect(canvas);
    }

    private void drawRect(Canvas canvas) {
        //绘制大矩形
        float top_left_X = (float) sX - EDGE_WIDTH;
        float top_right_X = (float) eX + EDGE_WIDTH;
        float top_Y = (float) sY - EDGE_WIDTH;

        float bottom_left_X = (float) sX - EDGE_WIDTH;
        float bottom_right_X = (float) eX + EDGE_WIDTH;
        float bottom_Y = (float) eY + EDGE_WIDTH;
        //top
        canvas.drawLine(top_left_X, top_Y, top_right_X, top_Y, mPaintLine);
        //bottom
        canvas.drawLine(bottom_left_X, bottom_Y, bottom_right_X, bottom_Y, mPaintLine);
        //left
        canvas.drawLine(top_left_X, top_Y, bottom_left_X, bottom_Y, mPaintLine);
        //right
        canvas.drawLine(top_right_X, top_Y, bottom_right_X, bottom_Y, mPaintLine);

        //fill绘制填充半透明
        Paint mPaintLineFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintLineFill.setColor(fence_fill_color);
        mPaintLineFill.setStyle(Paint.Style.FILL);

        Path bigFillPath = new Path();
        bigFillPath.moveTo(top_left_X, top_Y);
        bigFillPath.lineTo(top_right_X, top_Y);
        bigFillPath.lineTo(bottom_right_X, bottom_Y);
        bigFillPath.lineTo(bottom_left_X, bottom_Y);
        bigFillPath.close();
        canvas.drawPath(bigFillPath, mPaintLineFill);

        //绘制
        Paint mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFill.setColor(Color.WHITE);
        mPaintFill.setStyle(Paint.Style.FILL);

        Paint mPaintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintStroke.setColor(fence_border_color);
        mPaintStroke.setStrokeWidth(5f);
        mPaintStroke.setStyle(Paint.Style.STROKE);

        //up
        float upCenterX = Math.abs(top_right_X - top_left_X) / 2 + top_left_X;
        float upCenterY = top_Y;

        //down
        float downCenterX = upCenterX;
        float downCenterY = bottom_Y;

        //left
        float leftCenterX = top_left_X;
        float leftCenterY = Math.abs(bottom_Y - top_Y) / 2 + top_Y;

        //right
        float rightCenterX = top_right_X;
        float rightCenterY = Math.abs(bottom_Y - top_Y) / 2 + top_Y;

        final float xOffset = 10.f;
        final float yOffset = 30.0f;

        /**
         * up-left小矩形
         */
        Path mPath = new Path();
        mPath.moveTo(top_left_X - xOffset, top_Y - xOffset);
        mPath.lineTo(top_left_X + xOffset, top_Y - xOffset);
        mPath.lineTo(top_left_X + xOffset, top_Y + xOffset);
        mPath.lineTo(top_left_X - xOffset, top_Y + xOffset);
        mPath.close();
        canvas.drawPath(mPath, mPaintFill);
        canvas.drawPath(mPath, mPaintStroke);

        //down-left小矩形
        mPath = new Path();
        mPath.moveTo(bottom_left_X - xOffset, bottom_Y - xOffset);
        mPath.lineTo(bottom_left_X + xOffset, bottom_Y - xOffset);
        mPath.lineTo(bottom_left_X + xOffset, bottom_Y + xOffset);
        mPath.lineTo(bottom_left_X - xOffset, bottom_Y + xOffset);
        mPath.close();
        canvas.drawPath(mPath, mPaintFill);
        canvas.drawPath(mPath, mPaintStroke);

        //up_right小矩形
        mPath = new Path();
        mPath.moveTo(top_right_X - xOffset, top_Y - xOffset);
        mPath.lineTo(top_right_X + xOffset, top_Y - xOffset);
        mPath.lineTo(top_right_X + xOffset, top_Y + xOffset);
        mPath.lineTo(top_right_X - xOffset, top_Y + xOffset);
        mPath.close();
        canvas.drawPath(mPath, mPaintFill);
        canvas.drawPath(mPath, mPaintStroke);

        //bottom_right小矩形
        mPath = new Path();
        mPath.moveTo(bottom_right_X - xOffset, bottom_Y - xOffset);
        mPath.lineTo(bottom_right_X + xOffset, bottom_Y - xOffset);
        mPath.lineTo(bottom_right_X + xOffset, bottom_Y + xOffset);
        mPath.lineTo(bottom_right_X - xOffset, bottom_Y + xOffset);
        mPath.close();
        canvas.drawPath(mPath, mPaintFill);
        canvas.drawPath(mPath, mPaintStroke);

//        /**
//         * up小矩形
//         */
//        Path mPath = new Path();
//        mPath.moveTo(upCenterX - xOffset, upCenterY - yOffset);
//        mPath.lineTo(upCenterX + xOffset, upCenterY - yOffset);
//        mPath.lineTo(upCenterX + xOffset, upCenterY + yOffset);
//        mPath.lineTo(upCenterX - xOffset, upCenterY + yOffset);
//        mPath.close();
//        canvas.drawPath(mPath, mPaintFill);
//        canvas.drawPath(mPath, mPaintStroke);
//
//        //down小矩形
//        mPath = new Path();
//        mPath.moveTo(downCenterX - xOffset, downCenterY - yOffset);
//        mPath.lineTo(downCenterX + xOffset, downCenterY - yOffset);
//        mPath.lineTo(downCenterX + xOffset, downCenterY + yOffset);
//        mPath.lineTo(downCenterX - xOffset, downCenterY + yOffset);
//        mPath.close();
//        canvas.drawPath(mPath, mPaintFill);
//        canvas.drawPath(mPath, mPaintStroke);
//
//        //left小矩形
//        mPath = new Path();
//        mPath.moveTo(leftCenterX - yOffset, leftCenterY - xOffset);
//        mPath.lineTo(leftCenterX + yOffset, leftCenterY - xOffset);
//        mPath.lineTo(leftCenterX + yOffset, leftCenterY + xOffset);
//        mPath.lineTo(leftCenterX - yOffset, leftCenterY + xOffset);
//        mPath.close();
//        canvas.drawPath(mPath, mPaintFill);
//        canvas.drawPath(mPath, mPaintStroke);
//
//        //right小矩形
//        mPath = new Path();
//        mPath.moveTo(rightCenterX - yOffset, rightCenterY - xOffset);
//        mPath.lineTo(rightCenterX + yOffset, rightCenterY - xOffset);
//        mPath.lineTo(rightCenterX + yOffset, rightCenterY + xOffset);
//        mPath.lineTo(rightCenterX - yOffset, rightCenterY + xOffset);
//        mPath.close();
//        canvas.drawPath(mPath, mPaintFill);
//        canvas.drawPath(mPath, mPaintStroke);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (changeLocationlistener != null) {
                    changeLocationlistener.locationChange("change self");
                } else {
                    changeLocationlistener = null;
                }
                memonyX = (int) event.getX();
                memonyY = (int) event.getY();
                checkMode(memonyX, memonyY);
                break;
            case MotionEvent.ACTION_MOVE: {
                switch (MODE) {
                    case MODE_ILLEGAL:
                        pressX = (int) event.getX();
                        pressY = (int) event.getY();
                        recoverFromIllegal(pressX, pressY);
                        postInvalidate();
                        break;
                    case MODE_OUTSIDE:
                        //do nothing;
                        break;
                    case MODE_INSIDE:
                        pressX = (int) event.getX();
                        pressY = (int) event.getY();
//                        moveByTouch(pressX, pressY);
                        postInvalidate();
                        break;
                    default:
                        /*MODE_POINT*/
                        pressX = (int) event.getX();
                        pressY = (int) event.getY();
                        mPaintLine.setColor(Color.WHITE);
                        moveByPoint(pressX, pressY);
                        postInvalidate();
                        break;
                }
            }
            break;
            case MotionEvent.ACTION_UP:
                mPaintLine.setColor(fence_border_color);
                postInvalidate();
                break;
            default:
                break;
        }
        return true;
    }

    /*从非法状态恢复，这里处理的是达到最小值后能拉伸放大*/
    private void recoverFromIllegal(int rx, int ry) {
        if ((rx > sX && ry > sY) && (rx < eX && ry < eY)) {
            MODE = MODE_ILLEGAL;
        } else {
            MODE = MODE_POINT;
        }
    }

    private void checkMode(int cx, int cy) {
        if (cx > sX && cx < eX && cy > sY && cy < eY) {
            MODE = MODE_INSIDE;
        } else if (nearbyPoint(cx, cy) < 4) {
            MODE = MODE_POINT;
        } else {
            MODE = MODE_OUTSIDE;
        }
    }

    /*判断点(inX,inY)是否靠近矩形的4个顶点*/
    private int nearbyPoint(int inX, int inY) {
//        int topCenterX = Math.abs(eX - sX) / 2 + sX;
//        int topCenterY = sY;
//
//        int bottomCenterX = Math.abs(eX - sX) / 2 + sX;
//        int bottomCenterY = eY;
//
//        int leftCenterX = sX;
//        int leftCenterY = Math.abs(eY - sY) / 2 + sY;
//
//        int rightCenterX = eX;
//        int righCenterY = Math.abs(eY - sY) / 2 + sY;
//
//        //top
//        if ((Math.abs(topCenterX - inX) <= ACCURACY && (Math.abs(inY - topCenterY) <= ACCURACY))) {
//            pointPosition = 0;
//            return 0;
//        }
//        //right
//        if ((Math.abs(rightCenterX - inX) <= ACCURACY && (Math.abs(inY - righCenterY) <= ACCURACY))) {
//            pointPosition = 1;
//            return 1;
//        }
//        //left
//        if ((Math.abs(leftCenterX - inX) <= ACCURACY && (Math.abs(inY - leftCenterY) <= ACCURACY))) {
//            pointPosition = 2;
//            return 2;
//        }
//        //down
//        if ((Math.abs(bottomCenterX - inX) <= ACCURACY && (Math.abs(inY - bottomCenterY) <= ACCURACY))) {
//            pointPosition = 3;
//            return 3;
//        }

        if ((Math.abs(sX - inX) <= ACCURACY && (Math.abs(inY - sY) <= ACCURACY))) {/*left-up angle*/
            pointPosition = 0;
            return 0;
        }
        if ((Math.abs(eX - inX) <= ACCURACY && (Math.abs(inY - sY) <= ACCURACY))) {/*right-up  angle*/
            pointPosition = 1;
            return 1;
        }
        if ((Math.abs(sX - inX) <= ACCURACY && (Math.abs(inY - eY) <= ACCURACY))) {/*left-down angle*/
            pointPosition = 2;
            return 2;
        }
        if ((Math.abs(eX - inX) <= ACCURACY && (Math.abs(inY - eY) <= ACCURACY))) {/*right-down angle*/
            pointPosition = 3;
            return 3;
        }

        pointPosition = 100;
        return 100;
    }

    /*刷新矩形的坐标*/
    private void refreshLocation(int isx, int isy, int iex, int iey) {
        this.sX = isx;
        this.sY = isy;
        this.eX = iex;
        this.eY = iey;
    }

    /*矩形随手指移动*/
    private void moveByTouch(int mx, int my) {/*move center point*/
        int dX = mx - memonyX;
        int dY = my - memonyY;
        sX += dX;
        sY += dY;
        eX = sX + coverWidth;
        eY = sY + coverHeight;
        memonyX = mx;
        memonyY = my;
    }

    /*检测矩形是否达到最小值*/
    private boolean checkLegalRect(int cHeight, int cWidth) {
        return (cHeight > minHeight && cWidth > minWidth);
    }

    /*点击顶点附近时的缩放处理*/
    @SuppressWarnings("SuspiciousNameCombination")
    private void moveByPoint(int bx, int by) {
        switch (pointPosition) {
            case 0:/*left-up*/
                coverWidth = Math.abs(eX - bx);
                coverHeight = Math.abs(eY - by);
                //noinspection SuspiciousNameCombination
                if (!checkLegalRect(coverWidth, coverHeight)) {
                    MODE = MODE_ILLEGAL;
                } else {
                    mBitmapRectBlack = null;
                    mBitmapRectBlack = makeBitmap(coverWidth, coverHeight, fence_fill_color, coverWidth, coverHeight);
                    refreshLocation(bx, by, eX, eY);
                }
                break;
            case 1:/*right-up*/
                coverWidth = Math.abs(bx - sX);
                coverHeight = Math.abs(eY - by);
                if (!checkLegalRect(coverWidth, coverHeight)) {
                    MODE = MODE_ILLEGAL;
                } else {
                    mBitmapRectBlack = null;
                    mBitmapRectBlack = makeBitmap(coverWidth, coverHeight, fence_fill_color, coverWidth, coverHeight);
                    refreshLocation(sX, by, bx, eY);
                }
                break;
            case 2:/*left-down*/
                coverWidth = Math.abs(eX - bx);
                coverHeight = Math.abs(by - sY);
                if (!checkLegalRect(coverWidth, coverHeight)) {
                    MODE = MODE_ILLEGAL;
                } else {
                    mBitmapRectBlack = null;
                    mBitmapRectBlack = makeBitmap(coverWidth, coverHeight, fence_fill_color, coverWidth, coverHeight);
                    refreshLocation(bx, sY, eX, by);
                }
                break;
            case 3:/*right-down*/
                coverWidth = Math.abs(bx - sX);
                coverHeight = Math.abs(by - sY);
                if (!checkLegalRect(coverWidth, coverHeight)) {
                    MODE = MODE_ILLEGAL;
                } else {
                    mBitmapRectBlack = null;
                    mBitmapRectBlack = makeBitmap(coverWidth, coverHeight, fence_fill_color, coverWidth, coverHeight);
                    refreshLocation(sX, sY, bx, by);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void setRectangleCropLocationListener(FenceRectangleCropLocationListener locationListener) {
        this.locationListener = locationListener;
    }

    @Override
    public void setRectangleCropViewVisible(boolean visible) {
        if(visible){
            setVisibility(View.VISIBLE);
        }else {
            setVisibility(View.GONE);
        }
    }


    public interface onChangeLocationlistener {
        @SuppressWarnings("SameParameterValue")
         void locationChange(String msg);
    }

    public int getScreentHeight() {
        int heightPixels;
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display d = manager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
        // since SDK_INT = 1;
        heightPixels = metrics.heightPixels;
        // includes window decorations (statusbar bar/navigation bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
            try {
                heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
            } catch (Exception ignored) {
            }
            // includes window decorations (statusbar bar/navigation bar)
        else if (Build.VERSION.SDK_INT >= 17)
            try {
                android.graphics.Point realSize = new android.graphics.Point();
                Display.class.getMethod("getRealSize", android.graphics.Point.class).invoke(d, realSize);
                heightPixels = realSize.y;
            } catch (Exception ignored) {
            }
        return heightPixels;
    }
}