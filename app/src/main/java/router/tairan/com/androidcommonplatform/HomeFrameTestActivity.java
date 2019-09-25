package router.tairan.com.androidcommonplatform;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trc.android.common.widget.HomeFrame;

import java.util.Random;

public class HomeFrameTestActivity extends FragmentActivity {
    String[] TITLES = new String[]{"首页", "商城", "购物车", "我的"};
    private HomeFrame homeFrame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_frame_test);
        homeFrame = findViewById(R.id.homeFrame);
        homeFrame.setHostActivity(this)
                .setPageNumber(4)
//                .postInit(2,3)//预加载Page4
                .setPageAdapter(title -> PageIndexFragment.newInstance(TITLES[title]));


        LinearLayout linearLayout = findViewById(R.id.btnGroup);
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            int pageIndex = i;
            linearLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    homeFrame.setCurrentIndex(pageIndex);
                }
            });
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        homeFrame.trimMemory();
    }

    public static class PageIndexFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            TextView textView = new TextView(container.getContext());
            textView.setText(getArguments().getString("title"));
            int color = new Random().nextInt();
            textView.setBackgroundColor(color);
            textView.setTextSize(150);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeFrameTestActivity activity = (HomeFrameTestActivity) v.getContext();
                    activity.onTrimMemory(Activity.TRIM_MEMORY_RUNNING_LOW);
                }
            });
            return textView;
        }

        public static PageIndexFragment newInstance(String pageTitle) {
            Bundle bundle = new Bundle();
            bundle.putString("title", "" + pageTitle);
            PageIndexFragment pageIndexFragment = new PageIndexFragment();
            pageIndexFragment.setArguments(bundle);
            return pageIndexFragment;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            Log.e(getClass().getName(), "Fragment:" + getArguments().getString("title") + ":setUserVisibleHint:" + isVisibleToUser);
        }

        @Override
        public void onHiddenChanged(boolean hidden) {
            super.onHiddenChanged(hidden);
            Log.e(getClass().getName(), "Fragment:" + getArguments().getString("title") + ":onHiddenChanged:" + hidden);
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.e(getClass().getName(), "Fragment:" + getArguments().getString("title") + ":onResume");
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.e(getClass().getName(), "Fragment:" + getArguments().getString("title") + ":onPause");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.e(getClass().getName(), "Fragment:" + getArguments().getString("title") + ":onDestroy");
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            Log.e(getClass().getName(), "Fragment:" + getArguments().getString("title") + ":onAttach");
        }
    }
}


