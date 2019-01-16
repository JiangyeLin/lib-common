package com.trc.android.common.util;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trc.common.R;

/**
 * @author HuangMing on 2019/1/15.
 */

public class CustomToastUtil {

    private Activity mActivity;
    private ToastLayout mToast;
    private ViewGroup mView;
    private String text;
    private long times;
    private static CustomToastUtil mToastInstance;
    private FrameLayout.LayoutParams mViewGroupParams;
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
    public static int height = 30;

    /**
     * 设置高度
     *
     * @param height
     */
    public static void setHeight(int height) {
        CustomToastUtil.height = height;
    }

    /**
     * 背景色
     *
     * @param bgColor
     */
    public static void setBgColor(int bgColor) {
        bgColor = bgColor;
    }

    /**
     * 文字颜色
     *
     * @param textColor
     */
    public static void setTextColor(int textColor) {
        textColor = textColor;
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
     * @param mActivity
     * @param text
     * @param times
     */
    public CustomToastUtil(Activity mActivity, String text, long times) {
        this.mActivity = mActivity;
        this.text = text;
        this.times = times;
    }

    /**
     * 构造函数，上下文为View
     *
     * @param mView
     * @param text
     * @param times
     */
    public CustomToastUtil(ViewGroup mView, String text, long times) {
        this.mView = mView;
        this.text = text;
        this.times = times;
    }

    /**
     * 调用方法，上下文为activity
     *
     * @param mActivity
     * @param text
     * @param times
     * @return
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
     * @param mView
     * @param text
     * @param times
     * @return
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
                mToast = new ToastLayout(mActivity);
                initToast(mToast);
                mActivity.addContentView(mToast, mViewGroupParams);
            }
            mToast.setContent(text);
            mToast.showToast(times);
            return;
        } else if (mView != null) {
            initLayoutParams(mView.getContext());
            if (mToast != null && mToast.isShow()) {
                return;
            } else {
                mToast = new ToastLayout(mView.getContext());
                initToast(mToast);
                mView.addView(mToast, mViewGroupParams);
            }
            mToast.setContent(text);
            mToast.showToast(times);
        }
    }

    /**
     * 初始化页面布局
     *
     * @param context
     */
    private void initLayoutParams(Context context) {
        mViewGroupParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ToastLayout.dip2px(context, height));
        mViewGroupParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
        mViewGroupParams.bottomMargin = 200;
    }

    /**
     * 设置各个参数
     *
     * @param mToast
     */
    private void initToast(ToastLayout mToast) {
        if (textColor != 0) {
            mToast.setTextColor(textColor);
        }
        if (bgColor != 0) {
            mToast.setBgColor(bgColor);
        }
        mToast.setHeight(height);
    }

    private boolean isShowToast() {
        return mToast != null && mToast.isShow();
    }

    /**
     * 是否在展示
     *
     * @return
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

    static class ToastLayout extends RelativeLayout {
        private static final int ANIMATION_TIME = 200;
        private TextView mContent;
        private View view;
        private boolean isShow;
        private int height;

        public boolean isShow() {
            return isShow;
        }

        public ToastLayout(Context context) {
            this(context, null);
        }

        public ToastLayout(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ToastLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            view = LayoutInflater.from(getContext()).inflate(R.layout.lib_common_toast, null);
            addView(view);
            mContent = view.findViewById(R.id.tv_content);
            height = 60;
        }

        public void setTextColor(int color) {
            mContent.setTextColor(color);
        }

        public void setBgColor(int color) {
            mContent.setBackgroundColor(color);
        }

        public void setHeight(int height) {
            this.height = height;
        }


        public void setContent(String content) {
            if (mContent != null) {
                mContent.setText(content);
            }
        }

        public void showToast(long time) {
            AnimationSet animationSet = new AnimationSet(true);
            TranslateAnimation trans1 = new TranslateAnimation(0, 0, -dip2px(getContext(), height), 0);
            TranslateAnimation trans2 = new TranslateAnimation(0, 0, 0, -dip2px(getContext(), height));
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
                    ToastLayout.this.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    isShow = false;
                    ToastLayout.this.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        /**
         * 将dip或dp值转换为px值，保证尺寸大小不变
         *
         * @param context
         * @param dipValue
         * @return
         */
        public static int dip2px(Context context, float dipValue) {
            float density = context.getResources().getDisplayMetrics().density;
            return (int) (dipValue * density + 0.5f);
        }
    }
}
