package com.trc.android.common.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

        public void setCallback(Callback callback, Intent intent) {
            this.callback = callback;
            this.intent = intent;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (null != callback) {
                callback.onCreate(this);
            }
            if (null != intent) {
                startActivityForResult(intent, REQUEST_CODE);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (null != callback) {
                callback.onResume(this);
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
        Fragment hostFragment;
        @CallSuper
        void onCreate(Fragment fragment) {
            hostFragment = fragment;
        }
        void onResume(Fragment fragment) {
        }
        void onPause(Fragment fragment) {
        }
        void onActivityResult(Fragment fragment, int resultCode, Intent data) {
        }
        void onRequestPermissionsResult(Fragment fragment, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        }

        public void removeCallback(){
            hostFragment.getFragmentManager().beginTransaction().remove(hostFragment).commit();
        }

    }
}
