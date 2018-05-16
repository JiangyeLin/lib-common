package com.trc.android.common.util;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

public class LifeCircleCallbackUtil {
    public static void start(FragmentActivity currentActivity, Intent intent, Callback commonCallback) {
        LifeCircleFragment fragment = new LifeCircleFragment();
        fragment.setCallback(commonCallback, intent);
        currentActivity.getSupportFragmentManager().beginTransaction().add(Window.ID_ANDROID_CONTENT, fragment).commit();
    }

    public static void inject(FragmentActivity currentActivity, Callback commonCallback) {
        LifeCircleFragment fragment = new LifeCircleFragment();
        fragment.setCallback(commonCallback, null);
        currentActivity.getSupportFragmentManager().beginTransaction().add(Window.ID_ANDROID_CONTENT, fragment).commit();
    }

    public static class LifeCircleFragment extends Fragment {
        private Callback callback;
        private Intent intent;
        public static final int REQUEST_CODE = 100;
        boolean isFirstResume = true;

        public void setCallback(Callback callback, Intent intent) {
            this.callback = callback;
            this.intent = intent;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (isFirstResume && null != intent) {
                startActivityForResult(intent, REQUEST_CODE);
                isFirstResume = false;
                if (null != callback) {
                    callback.onLoad(this);
                }
            } else {
                if (null != callback) {
                    callback.onResume(this);
                }
            }
        }

        @Override
        public void onPause() {
            if (null != callback) {
                super.onPause();
                if (null != callback) {
                    callback.onPause(this);
                }
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (null != callback) {
                if (requestCode == REQUEST_CODE)
                    callback.onActivityResult(this, resultCode, data);
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            if (null != callback) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                callback.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
            }
        }

    }


    public static abstract class Callback {
        void onLoad(Fragment fragment) {

        }

        void onResume(Fragment fragment) {
        }

        void onPause(Fragment fragment) {
        }

        void onActivityResult(Fragment fragment, int resultCode, Intent data) {
        }

        void onRequestPermissionsResult(Fragment fragment, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        }

    }
}
