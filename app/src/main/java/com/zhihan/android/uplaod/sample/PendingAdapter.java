package com.zhihan.android.uplaod.sample;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.zhihan.android.upload.FileModel;
import com.zhihan.android.upload.FileStatus;

import java.util.List;
import java.util.Locale;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190705
 */
public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.ViewHolder> {

    List<FileModel> mData;
    OnOperationListener mListener;

    public PendingAdapter(List<FileModel> data, OnOperationListener l) {
        this.mData = data;
        mListener = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_pending, parent, false);
        return new ViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileModel model = mData.get(position);
        holder.tvName.setText(model.getFileName());
        holder.pb.setProgress((int) (model.getProgress() * 1000));
        @FileStatus int fileStatus = model.getStatus();
        switch (fileStatus) {
            case FileStatus.COMPLETED:
                Glide.with(holder.iv).load(model.getUrl()).into(holder.iv);
                holder.tvSpeed.setTextColor(Color.parseColor("#00FF00"));
                holder.tvSpeed.setText("已完成");
                holder.btnPause.setVisibility(View.GONE);
                break;
            case FileStatus.FAILED:
                Glide.with(holder.iv).load(R.mipmap.ic_launcher).into(holder.iv);
                holder.tvSpeed.setTextColor(Color.parseColor("#FF0000"));
                holder.tvSpeed.setText("上传失败");
                holder.btnPause.setVisibility(View.VISIBLE);
                holder.btnPause.setText("重试");
                break;
            case FileStatus.PAUSED:
                Glide.with(holder.iv).load(model.getLocalPath()).into(holder.iv);
                holder.tvSpeed.setTextColor(Color.parseColor("#999999"));
                holder.tvSpeed.setText("暂停中");
                holder.btnPause.setVisibility(View.VISIBLE);
                holder.btnPause.setText("继续上传");
                break;
            case FileStatus.PENDING:
                Glide.with(holder.iv).load(model.getLocalPath()).into(holder.iv);
                holder.tvSpeed.setTextColor(Color.parseColor("#999999"));
                holder.tvSpeed.setText("等待上传");
                holder.btnPause.setVisibility(View.GONE);
                break;
            case FileStatus.REMOVE:
                // never occur
                break;
            case FileStatus.UPLOADING:
                Glide.with(holder.iv).load(model.getLocalPath()).into(holder.iv);
                holder.tvSpeed.setTextColor(Color.parseColor("#999999"));
                double speed = model.getSpeed();
                String speedStr = "" + speed;
                if (speed < 1024) {
                    speedStr = String.format(Locale.getDefault(), "%.2f B/s", speed);
                } else if (speed < 1024.0 * 1024.0) {
                    speedStr = String.format(Locale.getDefault(), "%.2f KB/s", speed / 1024.0);
                } else if (speed < 1024.0 * 1024.0 * 1024.0) {
                    speedStr = String.format(Locale.getDefault(), "%.2f MB/s", speed / 1024.0 / 1024.0);
                } else if (speed < 1024.0 * 1024.0 * 1024.0 * 1024) {
                    speedStr = String.format(Locale.getDefault(), "%.2f GB/s", speed / 1024.0 / 1024.0 / 1024.0);
                }
                holder.tvSpeed.setText(speedStr);
                holder.btnPause.setVisibility(View.VISIBLE);
                holder.btnPause.setText("暂停");
                break;
        }
        holder.tvSize.setText(getFileSize(model.getFileSize()));
        holder.btnDel.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDelete(v, model, position);
            }
        });
        holder.btnPause.setOnClickListener(v -> {
            if (mListener != null) {
                int status = model.getStatus();
                if (status == FileStatus.PAUSED) {
                    mListener.onResume(v, model, position);
                } else if (status == FileStatus.UPLOADING) {
                    mListener.onPause(v, model, position);
                }
            }
        });
    }

    private String getFileSize(long b) {

        if (b < 1024) {
            return String.format(Locale.getDefault(), "%.2f B", b * 1.0);
        }
        double kb = b / 1024.0;
        if (kb < 1024) {
            return String.format(Locale.getDefault(), "%.2f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format(Locale.getDefault(), "%.2f MB", mb);
        }
        double gb = mb / 1024.0;
        return String.format(Locale.getDefault(), "%.2f GB", gb);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iv;
        TextView tvName;
        ProgressBar pb;
        TextView tvSpeed;
        TextView tvSize;
        Button btnDel;
        Button btnPause;


        public ViewHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv);
            tvName = itemView.findViewById(R.id.tv_name);
            pb = itemView.findViewById(R.id.tv_progress);
            tvSpeed = itemView.findViewById(R.id.tv_speed);
            tvSize = itemView.findViewById(R.id.tv_size);
            btnDel = itemView.findViewById(R.id.btn_del);
            btnPause = itemView.findViewById(R.id.btn_pause_or_resume);
        }
    }

    interface OnOperationListener {
        void onDelete(View v, FileModel model, int position);

        void onPause(View v, FileModel model, int position);

        void onResume(View v, FileModel model, int position);
    }

    public void setNewData(List<FileModel> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }
}
