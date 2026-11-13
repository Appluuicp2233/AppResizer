package com.appresizer;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AppInfo info);
    }

    private final List<AppInfo> items = new ArrayList<AppInfo>();
    private final OnItemClickListener listener;
    private boolean showStatus = true;

    public AppListAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setShowStatus(boolean show) {
        if (showStatus != show) {
            showStatus = show;
            notifyDataSetChanged();
        }
    }

    public void setItems(List<AppInfo> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    public List<AppInfo> getItems() {
        return items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final AppInfo info = items.get(position);
        Context context = holder.itemView.getContext();
        holder.icon.setImageDrawable(info.icon);
        holder.label.setText(info.label);
        holder.pkg.setText(info.packageName);
        if (!showStatus) {
            holder.status.setVisibility(View.GONE);
        } else if (info.configured) {
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setText(R.string.configured);
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.accent));
        } else {
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setText(R.string.unconfigured);
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onItemClick(info);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView label;
        TextView pkg;
        TextView status;

        ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.app_icon);
            label = (TextView) itemView.findViewById(R.id.app_name);
            pkg = (TextView) itemView.findViewById(R.id.app_package);
            status = (TextView) itemView.findViewById(R.id.app_status);
        }
    }
}
