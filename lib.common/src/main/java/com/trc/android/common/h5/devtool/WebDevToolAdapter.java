package com.trc.android.common.h5.devtool;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trc.common.R;

import java.util.List;

/**
 * JiangyeLin on 2018/7/16
 */
public class WebDevToolAdapter extends RecyclerView.Adapter {
    private List<WebDevTool.RecorderModel> list;

    WebDevToolAdapter(List<WebDevTool.RecorderModel> list) {
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
        WebDevTool.RecorderModel model = list.get(position);
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
