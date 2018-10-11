package com.trc.android.common.h5.devtool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.trc.android.common.util.Contexts;
import com.trc.common.R;

/**
 * JiangyeLin on 2018/8/9
 * webview调试工具的入口悬浮框
 */
public class WebDevFloatView extends View {
    private boolean isOpen = false;
    private View view;
    private WindowManager windowManager;
    private OnClickListener onClickerListener;
    private static WebDevFloatView instance;

    public static WebDevFloatView getInstance() {
        if (instance == null) {
            instance = new WebDevFloatView(Contexts.getInstance());
        }
        return instance;
    }

    public WebDevFloatView(Context context) {
        super(context);
        view = LayoutInflater.from(getContext()).inflate(R.layout.lib_common_webdevtool_floatview, null);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        instance = this;
    }

    public void open(Activity activity) {
        try {
            if (isOpen) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(Contexts.getInstance())) {
                new AlertDialog.Builder(activity, android.R.style.Theme_Holo_Light_Dialog_NoActionBar)
                        .setTitle("提示")
                        .setMessage("请在设置中开启悬浮窗权限,以正常使用调试工具")
                        .setPositiveButton("确定", (dialog, which) -> {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                            intent.setData(uri);
                            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                                activity.startActivity(intent);
                            } else {
                                activity.startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
                            }
                        })
                        .show();
                return;
            }

            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            params.height = FrameLayout.LayoutParams.WRAP_CONTENT;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            } else {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            }
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            params.format = PixelFormat.TRANSLUCENT;
            params.gravity = Gravity.START | Gravity.TOP;

            windowManager = activity.getWindowManager();

            view.setOnClickListener(v -> {
                if (onClickerListener != null) {
                    onClickerListener.onClick(v);
                }
            });
            view.setOnTouchListener(new OnTouchListener() {
                float lastX = 0;
                float lastY = 0;

                long downTime;
                float downX = 0;
                float downY = 0;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            lastX = event.getRawX();
                            lastY = event.getRawY();

                            downX = lastX;
                            downY = lastY;

                            downTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float moveX = event.getRawX() - lastX;
                            float moveY = event.getRawY() - lastY;

                            lastX += moveX;
                            lastY += moveY;

                            params.x += moveX;
                            params.y += moveY;

                            windowManager.updateViewLayout(view, params);
                            break;
                        case MotionEvent.ACTION_UP:
                            float upX = event.getRawX();
                            float upY = event.getRawY();

                            if (System.currentTimeMillis() - downTime < 800 &&
                                    Math.abs(upX - downX) < 20 &&
                                    Math.abs(upY - downY) < 20) {
                                onClickerListener.onClick(v);
                            }
                            break;
                    }
                    return true;
                }
            });
            windowManager.addView(view, params);
            isOpen = true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void hide() {
        if (isOpen) {
            windowManager.removeView(view);
            isOpen = false;
        }
    }

    public void release() {
        view = null;
        instance = null;
    }

    public void setOnClickerListener(OnClickListener onClickerListener) {
        this.onClickerListener = onClickerListener;
    }
}
