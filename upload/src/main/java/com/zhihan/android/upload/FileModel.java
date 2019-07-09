package com.zhihan.android.upload;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

/**
 * @author wangxin
 * @since 20190705
 */
public class FileModel implements Parcelable {

    /** 类似ID的作用, 用于唯一标示一个文件 */
    private String key;

    /** 文件名(例: 照片1.png) */
    private String fileName;

    /** 文件大小(单位: B) */
    private long fileSize;

    /** 上传进度(0~1之间, 例: 0.78) */
    private double progress;

    /** 上传速度(单位: B/s) */
    private double speed;

    /** 文件状态(等待中, 上传中, 暂停中, 上传失败, 上传完成) */
    @FileStatus
    private int status;

    /** 本地地址 */
    private String localPath;

    /** 网络地址(上传成功后才有) */
    private String url;

    /** 手动操作的时间戳(也可用于排序), 比如: 选取文件上传、暂停、继续上传等操作都会更新这个字段 */
    private long operationTime;

    /** 手动操作的时间戳时的上传进度 */
    private double operationTimeProcess = 0;


    protected FileModel(Parcel in) {
        key = in.readString();
        fileName = in.readString();
        fileSize = in.readLong();
        progress = in.readDouble();
        speed = in.readDouble();
        status = in.readInt();
        localPath = in.readString();
        url = in.readString();
        operationTime = in.readLong();
        operationTimeProcess = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(fileName);
        dest.writeLong(fileSize);
        dest.writeDouble(progress);
        dest.writeDouble(speed);
        dest.writeInt(status);
        dest.writeString(localPath);
        dest.writeString(url);
        dest.writeLong(operationTime);
        dest.writeDouble(operationTimeProcess);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FileModel> CREATOR = new Creator<FileModel>() {
        @Override
        public FileModel createFromParcel(Parcel in) {
            return new FileModel(in);
        }

        @Override
        public FileModel[] newArray(int size) {
            return new FileModel[size];
        }
    };

    public FileModel() {
    }

    static FileModel newLocal(String localPath) {
        FileModel model = new FileModel();
        model.fileName = Utils.getFileSimpleName(localPath);
        model.fileSize = Utils.getFileSize(localPath);
        model.progress = 0;
        model.speed = 0;
        model.status = FileStatus.PENDING;
        model.localPath = localPath;
        model.url = "";
        model.key = Utils.toMD5(localPath);
        model.operationTime = System.currentTimeMillis();
        return model;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public double getSpeed() {
        return speed;
    }

    @FileStatus
    public int getStatus() {
        return status;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getUrl() {
        return url;
    }

    public double getProgress() {
        return progress;
    }

    String getKey() {
        return key;
    }

    void setProgress(double progress) {
        this.progress = progress;
    }

    void setSpeed(double speed) {
        this.speed = speed;
    }

    void setStatus(int status) {
        this.status = status;
    }

    void setUrl(String url) {
        this.url = url;
        setOperationTime(System.currentTimeMillis());
    }

    long getOperationTime() {
        return operationTime;
    }

    void setOperationTime(long operationTime) {
        this.operationTime = operationTime;
        this.operationTimeProcess = progress;
    }

    double getOperationTimeProcess() {
        return operationTimeProcess;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
