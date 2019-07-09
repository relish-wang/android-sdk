package com.zhihan.android.upload.sample.other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smart.android.imagepickerlib.ImagePicker;
import com.smart.android.imagepickerlib.bean.ImageItem;
import com.smart.android.imagepickerlib.ui.ImagePreviewActivity;
import com.smart.android.utils.ToastUtils;
import com.zhihan.android.upload.FileModel;
import com.zhihan.android.upload.FileStatus;
import com.zhihan.android.upload.sample.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author wangxin
 * @since 20190705
 */
public class PendingAdapter extends BaseAdapter {

    List<FileModel> mData;
    OnOperationListener mListener;

    public PendingAdapter(List<FileModel> data, OnOperationListener l) {
        this.mData = data;
        mListener = l;
    }

    @SuppressLint("SetTextI18n")
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
                ToastUtils.showShort("喵喵喵???");
                break;
            case FileStatus.UPLOADING:
                Glide.with(holder.iv).load(model.getLocalPath()).into(holder.iv);
                holder.tvSpeed.setTextColor(Color.parseColor("#999999"));
                double speed = model.getSpeed();
                String speedStr = "" + speed;
                if (speed < 1024) {
                    speedStr = format("B/s", speed);
                } else if (speed < 1024.0 * 1024.0) {
                    speedStr = format("KB/s", speed / 1024.0);
                } else if (speed < 1024.0 * 1024.0 * 1024.0) {
                    speedStr = format("MB/s", speed / 1024.0 / 1024.0);
                } else if (speed < 1024.0 * 1024.0 * 1024.0 * 1024) {
                    speedStr = format("GB/s", speed / 1024.0 / 1024.0 / 1024.0);
                }
                holder.tvSpeed.setText(speedStr);
                holder.btnPause.setVisibility(View.VISIBLE);
                holder.btnPause.setText("暂停");
                break;
        }
        holder.iv.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);
            ImageItem value = new ImageItem();
            value.path = model.getLocalPath();
            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS,
                    new ArrayList<>(Collections.singletonList(value)));
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);
            context.startActivity(intent);
        });
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

    private static String format(String unit, double speed) {
        return String.format(Locale.getDefault(), "%.2f " + unit, speed);
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
    public int getCount() {
        return mData.size();
    }

    @Override
    public FileModel getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_pending, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        onBindViewHolder(holder, position);
        return convertView;
    }

    public static class ViewHolder {

        ImageView iv;
        TextView tvName;
        ProgressBar pb;
        TextView tvSpeed;
        TextView tvSize;
        Button btnDel;
        Button btnPause;


        public ViewHolder(View itemView) {
            iv = itemView.findViewById(R.id.iv);
            tvName = itemView.findViewById(R.id.tv_name);
            pb = itemView.findViewById(R.id.tv_progress);
            tvSpeed = itemView.findViewById(R.id.tv_speed);
            tvSize = itemView.findViewById(R.id.tv_size);
            btnDel = itemView.findViewById(R.id.btn_del);
            btnPause = itemView.findViewById(R.id.btn_pause_or_resume);
        }
    }

    public interface OnOperationListener {
        void onDelete(View v, FileModel model, int position);

        void onPause(View v, FileModel model, int position);

        void onResume(View v, FileModel model, int position);
    }

    public static class OnOperationAdapter implements OnOperationListener {

        @Override
        public void onDelete(View v, FileModel model, int position) {

        }

        @Override
        public void onPause(View v, FileModel model, int position) {

        }

        @Override
        public void onResume(View v, FileModel model, int position) {

        }
    }

    public void setNewData(List<FileModel> mData) {
        this.mData = mData;
        notifyDataSetChanged();
    }
}
