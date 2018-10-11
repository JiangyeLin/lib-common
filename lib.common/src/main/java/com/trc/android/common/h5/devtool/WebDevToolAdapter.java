package com.trc.android.common.h5.devtool;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trc.common.R;

import java.util.List;

/**
 * JiangyeLin on 2018/7/16
 */
public class WebDevToolAdapter extends RecyclerView.Adapter {
    private List<WebviewRecorderModel> list;

    WebDevToolAdapter(List<WebviewRecorderModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lib_common_webdevtool_log_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WebviewRecorderModel model = list.get(position);
        ((ViewHolder) holder).tvKey.setText(model.key + ": ");
        ((ViewHolder) holder).tvContent.setText(model.desc);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        TextView tvKey;

        ViewHolder(View view) {
            super(view);
            tvKey = view.findViewById(R.id.tvKey);
            tvContent = view.findViewById(R.id.tvContent);
        }
    }
}
