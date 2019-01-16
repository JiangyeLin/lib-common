package com.trc.android.common.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * @author HuangMing on 2019/1/15.
 */

public class CustomToastUtil {

    private Activity mActivity;
    private ToastText mToast;
    private ViewGroup mView;
    private String text;
    private long times;
    private static CustomToastUtil mToastInstance;
    private FrameLayout.LayoutParams mViewGroupParams;
    private Context mContext;
    /**
     * 固定参数
     */
    public static final long LENGTH_LONG = 3000;
    public static final long LENGTH_SHORT = 1000;
    /**
     * 静态可设置参数
     */
    public static int bgColor;
    public static int textColor;
    public static int height = 40;

    /**
     * 设置高度
     *
     * @param height 高度
     */
    public static void setHeight(int height) {
        CustomToastUtil.height = height;
    }

    /**
     * 背景色
     *
     * @param bgColor 背景色
     */
    public static void setBgColor(int bgColor) {
        CustomToastUtil.bgColor = bgColor;
    }

    /**
     * 文字颜色
     *
     * @param textColor 文字颜色
     */
    public static void setTextColor(int textColor) {
        CustomToastUtil.textColor = textColor;
    }

    /**
     * 初始化
     *
     * @param bgColor   背景颜色
     * @param textColor 文字颜色
     * @param height    高度
     */
    public static void init(int bgColor, int textColor, int height) {
        CustomToastUtil.bgColor = bgColor;
        CustomToastUtil.textColor = textColor;
        CustomToastUtil.height = height;
    }

    /**
     * 构造函数，上下文为activity
     *
     * @param mActivity 上下文为activity
     * @param text      文本内容
     * @param times     显示时间
     */
    private CustomToastUtil(Activity mActivity, String text, long times) {
        this.mActivity = mActivity;
        this.text = text;
        this.times = times;
    }

    /**
     * 构造函数，上下文为View
     *
     * @param mView 上下文为View
     * @param text  文本内容
     * @param times 显示时间
     */
    private CustomToastUtil(ViewGroup mView, String text, long times) {
        this.mView = mView;
        this.text = text;
        this.times = times;
    }

    /**
     * 调用方法，上下文为activity
     *
     * @param mActivity 上下文为activity
     * @param text      文本内容
     * @param times     显示时间
     * @return CustomToastUtil
     */
    public static CustomToastUtil makeText(Activity mActivity, String text, long times) {
        if (mToastInstance == null) {
            mToastInstance = new CustomToastUtil(mActivity, text, times);
        }
        return mToastInstance;
    }

    /**
     * 调用方法，上下文为view
     *
     * @param mView 上下文为view
     * @param text  文本内容
     * @param times 显示时间
     * @return CustomToastUtil
     */
    public static CustomToastUtil makeText(ViewGroup mView, String text, long times) {
        if (mToastInstance == null) {
            mToastInstance = new CustomToastUtil(mView, text, times);
        }
        return mToastInstance;
    }

    /**
     * 展示
     */
    public void show() {
        if (mActivity != null) {
            initLayoutParams(mActivity);
            if (mToast != null && mToast.isShow()) {
                return;
            } else {
                mToast = initText(mActivity);
                initToast(mToast);
                mActivity.addContentView(mToast, mViewGroupParams);
            }
            mToast.setText(text);
            mToast.showToast(times);
        } else if (mView != null) {
            initLayoutParams(mView.getContext());
            if (mToast != null && mToast.isShow()) {
                return;
            } else {
                mToast = initText(mView.getContext());
                initToast(mToast);
                mView.addView(mToast, mViewGroupParams);
            }
            mToast.setText(text);
            mToast.showToast(times);
        }
    }

    /**
     * 初始化显示文本控件
     *
     * @param context 上下文为
     * @return ToastText 对象
     */
    private ToastText initText(Context context) {
        ToastText textView = new ToastText(context);
        textView.setTextSize(14);
        textView.setPadding(dip2px(20), dip2px(10), dip2px(20), dip2px(10));
        return textView;
    }

    /**
     * 初始化页面布局
     *
     * @param context 上下文为
     */
    private void initLayoutParams(Context context) {
        mContext = context;
        mViewGroupParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, dip2px(height));
        mViewGroupParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        mViewGroupParams.bottomMargin = dip2px(100);
    }

    /**
     * 设置各个参数
     *
     * @param mToast ToastText对象
     */
    private void initToast(ToastText mToast) {
        if (textColor != 0) {
            mToast.setTextColor(textColor);
        }
        if (bgColor != 0) {
            mToast.setPaintColor(bgColor);
        }
        mToast.setHeight(dip2px(height));
    }

    private boolean isShowToast() {
        return mToast != null && mToast.isShow();
    }

    /**
     * 是否在展示
     *
     * @return true/false 显示/不显示
     */
    public static boolean isShow() {
        if (mToastInstance == null) {
            return false;
        } else {
            boolean isShow = mToastInstance.isShowToast();
            mToastInstance = null;
            return isShow;
        }
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue dp值
     * @return px
     */
    private int dip2px(float dipValue) {
        float density = mContext.getResources().getDisplayMetrics().density;
        return (int) (dipValue * density + 0.5f);
    }

    @SuppressLint("AppCompatCustomView")
    class ToastText extends TextView {
        private static final int ANIMATION_TIME = 500;
        private boolean isShow;
        private Paint mPaint;
        private RectF rectF;
        private int mPaintColor = Color.parseColor("#e0e0e0");

        public boolean isShow() {
            return isShow;
        }

        public ToastText(Context context) {
            this(context, null);
        }

        public ToastText(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ToastText(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        public void setPaintColor(int color) {
            mPaintColor = color;
            mPaint.setColor(mPaintColor);
        }

        /**
         * 初始化画笔
         */
        private void init() {
            //实例化画笔
            mPaint = new Paint();
            //设置画笔颜色
            mPaint.setColor(mPaintColor);
            //设置它的填充方法，用的多的是FILL 和 STORKE
            mPaint.setStyle(Paint.Style.FILL);
        }

        /**
         * 重写onDraw方法 可以在绘制文字前后进行一些自己的操作
         * super.onDraw(canvas);调用父类方法绘制文字
         * 如果绘制矩形的代码写在它的后边，文字就会被覆盖
         */
        @Override
        protected void onDraw(Canvas canvas) {
            //在回调父类方法之前，对TextView来说是绘制文本内容之前
            //绘制里层矩形 参数：左、上、右、下、画笔
            //除了绘制矩形，用的多的还可以绘制线，圆，扇形，Path等
            canvas.drawRoundRect(rectF, dip2px(50), dip2px(50), mPaint);
            super.onDraw(canvas);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            rectF = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        }

        public void showToast(long time) {
            AnimationSet animationSet = new AnimationSet(true);
            AlphaAnimation trans1 = new AlphaAnimation(0.1f, 1.0f);
            AlphaAnimation trans2 = new AlphaAnimation(1.0f, 0.1f);
            trans1.setDuration(ANIMATION_TIME);
            trans2.setStartOffset(ANIMATION_TIME + time);
            trans2.setDuration(ANIMATION_TIME);
            animationSet.addAnimation(trans1);
            animationSet.addAnimation(trans2);
            this.startAnimation(animationSet);
            animationSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    isShow = true;
                    ToastText.this.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isShow = false;
                    ToastText.this.setVisibility(View.GONE);
                    ((ViewGroup)ToastText.this.getParent()).removeView(ToastText.this);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }
}
