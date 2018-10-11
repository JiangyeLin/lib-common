package com.trc.android.common.h5.devtool;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class FloatingButton {
    private static final int NUM_VALUES1 = 2;
    private static final int NUM_VALUES2 = 10;
    private static final int NUM_VALUES3 = 60;
    private static final int NUM_VALUES4 = 100;
    private static final int NUM_VALUES5 = 200;
    private static final int NUM_VALUES6 = 1000;

    @SuppressLint("ClickableViewAccessibility")
    public static ImageView create(ViewGroup windowContentViewContainer, @DrawableRes int imgSrcId, int widthDp, int heightDp, View.OnClickListener clickListener) {
        Context context = windowContentViewContainer.getContext();
        FrameLayout frameLayout = new FrameLayout(context);
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(imgSrcId);
        int density = (int) context.getResources().getDisplayMetrics().density;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(density * widthDp, density * heightDp);
        layoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        frameLayout.addView(imageView, layoutParams);
        windowContentViewContainer.addView(frameLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setOnTouchListener(new View.OnTouchListener() {
                                         float downX;
                                         float downY;
                                         float originTop;
                                         float originLeft;
                                         long downTime;
                                         int originBottomMargin;
                                         int originRightMargin;
                                         int maxBottomMargin;
                                         int maxRightMargin;

                                         ViewGroup.MarginLayoutParams params;

                                         @Override
                                         public boolean onTouch(View v, MotionEvent event) {
                                             if (params == null) {
                                                 params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                                             }
                                             switch (event.getAction()) {
                                                 case MotionEvent.ACTION_DOWN:
                                                     downTime = System.currentTimeMillis();
                                                     downX = event.getRawX();
                                                     downY = event.getRawY();
                                                     originTop = v.getTop();
                                                     originLeft = v.getLeft();
                                                     originBottomMargin = params.bottomMargin;
                                                     originRightMargin = params.rightMargin;
                                                     maxBottomMargin = ((ViewGroup) v.getParent()).getHeight() - v.getHeight();
                                                     maxRightMargin = ((ViewGroup) v.getParent()).getWidth() - v.getWidth();
                                                     break;
                                                 case MotionEvent.ACTION_UP:
                                                     if (System.currentTimeMillis() - downTime < NUM_VALUES5 && Math.abs((downX - event.getRawX()) + Math.abs(downY - event.getRawY())) < NUM_VALUES4) {
                                                         clickListener.onClick(imageView);
                                                     } else {
                                                         ViewGroup viewGroup = (ViewGroup) imageView.getParent();
                                                         int childCenter = params.rightMargin + imageView.getWidth() / 2;
                                                         if (params.bottomMargin < imageView.getHeight() * NUM_VALUES1) {
                                                             ValueAnimator valueAnimator = ValueAnimator.ofInt(params.bottomMargin, 0);
                                                             valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                                                 @Override
                                                                 public void onAnimationUpdate(ValueAnimator animation) {
                                                                     params.bottomMargin = (int) animation.getAnimatedValue();
                                                                     imageView.requestLayout();
                                                                 }
                                                             });
                                                             valueAnimator.start();
                                                         }
                                                         //在屏幕上偏左
                                                         else if (childCenter > viewGroup.getWidth() / NUM_VALUES1) {
                                                             ValueAnimator valueAnimator = ValueAnimator.ofInt(params.rightMargin, viewGroup.getWidth() - imageView.getWidth());
                                                             valueAnimator.addUpdateListener(animation -> {
                                                                 params.rightMargin = (int) animation.getAnimatedValue();
                                                                 imageView.requestLayout();
                                                             });
                                                             valueAnimator.start();
                                                         } else {
                                                             ValueAnimator valueAnimator = ValueAnimator.ofInt(params.rightMargin, 0);
                                                             valueAnimator.addUpdateListener(animation -> {
                                                                 params.rightMargin = (int) animation.getAnimatedValue();
                                                                 imageView.requestLayout();
                                                             });
                                                             valueAnimator.start();
                                                         }
                                                     }
                                                     break;
                                                 case MotionEvent.ACTION_MOVE:
                                                     int newBottomMargin = (int) (originBottomMargin - event.getRawY() + downY);
                                                     int newRightMargin = (int) (originRightMargin - event.getRawX() + downX);
                                                     if (newBottomMargin > maxBottomMargin) {
                                                         newBottomMargin = maxBottomMargin;
                                                     }
                                                     if (newRightMargin > maxRightMargin) {
                                                         newRightMargin = maxRightMargin;
                                                     }
                                                     if (newBottomMargin < 0) {
                                                         newBottomMargin = 0;
                                                     }
                                                     if (newRightMargin < 0) {
                                                         newRightMargin = 0;
                                                     }
                                                     boolean needFresh = (newBottomMargin != params.bottomMargin) || (newRightMargin != params.rightMargin);
                                                     params.bottomMargin = newBottomMargin;
                                                     params.rightMargin = newRightMargin;
                                                     int newTop = (int) (originTop + event.getRawY() - downY);
                                                     int newLeft = (int) (originLeft + event.getRawX() - downX);
                                                     if (newTop < 0) {
                                                         newTop = 0;
                                                     }
                                                     if (newLeft < 0) {
                                                         newLeft = 0;
                                                     }
                                                     if (newTop > ((ViewGroup) v.getParent()).getHeight() - v.getHeight()) {
                                                         newTop = ((ViewGroup) v.getParent()).getHeight() - v.getHeight();
                                                     }
                                                     if (newLeft > ((ViewGroup) v.getParent()).getWidth() - v.getWidth()) {
                                                         newLeft = ((ViewGroup) v.getParent()).getWidth() - v.getWidth();
                                                     }
                                                     if (needFresh) {
                                                         v.layout(newLeft, newTop, newLeft + v.getWidth(), newTop + v.getHeight());
                                                     }
                                                     break;
                                                 default:
                                                     break;
                                             }
                                             return true;
                                         }
                                     }

        );
        return imageView;
    }

}
