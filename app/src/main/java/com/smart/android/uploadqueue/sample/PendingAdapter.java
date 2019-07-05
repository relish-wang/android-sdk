package com.smart.android.uploadqueue.sample;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smart.android.uploadqueue.FileModel;
import com.smart.android.uploadqueue.FileStatus;

import java.util.List;

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
        Glide.with(holder.iv).load(model.getLocalPath()).into(holder.iv);
        holder.tvName.setText(model.getFileName());
        holder.tvProgress.setText(String.valueOf(model.getProgress()));
        holder.tvSpeed.setText(model.getSpeed() + " B/s");
        holder.tvSize.setText(model.getFileSize() + " B");
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

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iv;
        TextView tvName;
        TextView tvProgress;
        TextView tvSpeed;
        TextView tvSize;
        Button btnDel;
        Button btnPause;


        public ViewHolder(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.iv);
            tvName = itemView.findViewById(R.id.tv_name);
            tvProgress = itemView.findViewById(R.id.tv_progress);
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
}
