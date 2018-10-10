package com.trc.android.common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class HomeFrame extends FrameLayout {
    private PageAdapter pageAdapter;
    private Fragment[] fragments;
    private FragmentActivity hostActivity;
    private Fragment currentFragment;
    private int pageIndex;
    private int[] postInitPageIndexes;
    boolean isAttachedToWindow;

    public HomeFrame(Context context) {
        super(context);
    }

    public HomeFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public HomeFrame(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public HomeFrame setHostActivity(FragmentActivity activity) {
        hostActivity = activity;
        return this;
    }

    public HomeFrame setPageNumber(int pageNumber) {
        this.fragments = new Fragment[pageNumber];
        return this;
    }

    public HomeFrame setPageAdapter(PageAdapter adapter) {
        this.pageAdapter = adapter;
        return this;
    }

    public HomeFrame setCurrentIndex(int pageIndex) {
        this.pageIndex = pageIndex;
        if (isAttachedToWindow && fragments != null && fragments.length > 0) {
            Fragment fragment = fragments[pageIndex];
            FragmentTransaction fragmentTransaction = hostActivity.getSupportFragmentManager().beginTransaction();

            if (currentFragment != fragment && null != currentFragment) {
                fragmentTransaction.hide(currentFragment);
                if (currentFragment.getView() != null)
                    currentFragment.getView().setVisibility(View.GONE);
                currentFragment.setUserVisibleHint(false);
            }

            if (null == fragment) {
                fragment = this.pageAdapter.getFragment(pageIndex);
                fragmentTransaction.add(getId(), fragment, fragment.getClass().getName() + pageIndex);
                fragments[pageIndex] = fragment;
            } else {
                fragmentTransaction.show(fragment);
                fragment.setUserVisibleHint(true);
            }
            currentFragment = fragment;
            if (currentFragment.getView() != null)
                currentFragment.getView().setVisibility(View.VISIBLE);
            fragmentTransaction.commit();
        }
        return this;
    }

    public HomeFrame postInit(int... indexes) {
        postInitPageIndexes = indexes;
        return this;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public Fragment getFragment(int pageIndex) {
        return fragments[pageIndex];
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;//不关心是否detachedFromWindow
        loadFragment();
    }


    private void loadFragment() {
        setCurrentIndex(pageIndex);
        postInitInvisibleFragments();
    }

    private void postInitInvisibleFragments() {
        if (null != postInitPageIndexes) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Looper.getMainLooper().getQueue().addIdleHandler(() -> {
                    initInvisibleFragments();
                    return false;
                });
            } else {
                post(this::initInvisibleFragments);
            }
        }
    }

    private void initInvisibleFragments() {
        FragmentTransaction fragmentTransaction = hostActivity.getSupportFragmentManager().beginTransaction();
        for (int index : postInitPageIndexes) {
            Fragment fragment = fragments[index];
            if (fragment == null) {
                fragment = pageAdapter.getFragment(index);
                fragments[index] = fragment;
                fragmentTransaction.add(getId(), fragment, fragment.getClass().getName() + index).hide(fragment);
            }
        }
        fragmentTransaction.commit();
    }

    public void tripMemory() {
        FragmentTransaction fragmentTransaction = hostActivity.getSupportFragmentManager().beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            Fragment fragment = fragments[i];
            if (fragment != currentFragment) {
                fragmentTransaction.remove(fragment);
                fragments[i] = null;
            }
        }
        fragmentTransaction.commit();
    }

    public interface PageAdapter {
        Fragment getFragment(int position);
    }

}
