package com.shenkai.bigview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class BigView extends View implements GestureDetector.OnGestureListener, View.OnTouchListener, GestureDetector.OnDoubleTapListener {
    private final ScaleGestureDetector scaleGestureDetector;
    private Rect rect;
    private BitmapFactory.Options options;
    private GestureDetector gestureDetector;
    private Scroller scroller;
    private int imageWidth;
    private int imageHeight;
    private BitmapRegionDecoder decoder;
    private int viewWidth;
    private int viewHeight;
    private float scale;
    private Bitmap bitmap;
    private float originalScale;

    public BigView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // 第1步：设置bigView需要成员变量
        rect = new Rect();
        //内存复用
        options = new BitmapFactory.Options();
        //手势识别
        gestureDetector = new GestureDetector(context, this);
        //滚动类
        scroller = new Scroller(context);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGesture());

        setOnTouchListener(this);
    }

    //第二部:设置图片
    public void setImage(InputStream is) {
        // 获取图片的信息, 不能将整张图片加载进内存
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(is, null, options);
        imageWidth = options.outWidth;
        imageHeight = options.outHeight;

        //开启复用
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        options.inJustDecodeBounds = false;

        //创建一个区域解码器
        try {
            decoder = BitmapRegionDecoder.newInstance(is, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestLayout();
    }

    // 第3步，测量
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();

        //确定加载图片的区域
//        rect.left = 0;
//        rect.top = 0;
//        rect.right = imageWidth;
        //得到图片的宽度，就能根据view的宽度计算缩放因子
//        scale = viewWidth / (float) imageWidth;
//        rect.bottom = (int) (viewHeight / scale);

        // 加了缩放手势之后的逻辑
        rect.left = 0;
        rect.top = 0;
        rect.right = Math.min(imageWidth, viewWidth);
        rect.bottom = Math.min(imageHeight, viewHeight);

        // 再定义一个缩放因子
        originalScale = viewWidth / (float) imageWidth;
        scale = originalScale;
    }

    // 第4步，画出具体的内容
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (decoder == null) return;
        // 复用内存 // 复用bitmap必须跟即将解码的bitmap尺寸一样
        options.inBitmap = bitmap;
        bitmap = decoder.decodeRegion(rect, options);

        Matrix matrix = new Matrix();
        matrix.setScale(viewWidth / (float) rect.width(), viewWidth / (float) rect.width());

        canvas.drawBitmap(bitmap, matrix, null);
    }

    //5.处理手势
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    // 6： 手按下去，处理事件
    @Override
    public boolean onDown(MotionEvent e) {
        //如果移动没有停止，就强行停止
        if (!scroller.isFinished()) {
            scroller.forceFinished(true);
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    // 7: 处理滑动事件
    // e1 就是开始事件，手指按下去，获取坐标
    // e2 当前事件
    // xy xy轴
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // 上下左右移动的时候，mRect需要改变显示区域
        rect.offset((int) distanceX, (int) distanceY);
        if (rect.bottom > imageHeight) {
            rect.bottom = imageHeight;
            rect.top = imageHeight-(int) (viewHeight/scale);
        }
        if (rect.top < 0) {
            rect.top=0;
            rect.bottom = (int) (viewHeight/scale);
        }
        if (rect.right > imageWidth) {
            rect.right = imageWidth;
            rect.left = imageWidth-(int) (viewWidth/scale);
        }
        if (rect.left < 0) {
            rect.left = 0;
            rect.right = (int) (viewWidth/scale);
        }
        invalidate();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    // 处理惯性问题
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        scroller.fling(rect.left, rect.top, (int) -velocityX, (int) -velocityY, 0, imageWidth - (int) (viewWidth / scale),
                0, imageHeight - (int) (viewHeight / scale));
        return false;
    }

    @Override
    public void computeScroll() {
        if (scroller.isFinished()) return;
        if (scroller.computeScrollOffset()) {
            rect.top = scroller.getCurrY();
            rect.bottom = rect.top + (int) (viewHeight / scale);
            invalidate();
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {

        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    // 处理缩放的回调事件
    class ScaleGesture extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            
            return super.onScale(detector);
        }
    }
}
