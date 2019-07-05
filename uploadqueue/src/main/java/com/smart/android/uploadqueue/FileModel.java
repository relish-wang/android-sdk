package com.smart.android.uploadqueue;

import com.smart.android.utils.MD5;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190705
 */
public class FileModel /*implements Parcelable*/ {

    /** 类似ID的作用, 用于唯一标示一个文件 */
    private String key;

    /** 文件名(例: 照片1.png) */
    private String fileName;

    /** 文件大小(单位: B) */
    private long fileSize;

    /** 上传进度(单位: B/s) */
    private double progress;

    /** 上传速度(单位: B/s) */
    private double speed;

    /** 文件状态(等待中, 上传中, 上传失败, 上传完成) */
    @FileStatus
    private int status;

    /** 本地地址 */
    private String localPath;

    /** 网络地址(上传成功后才有) */
    private String url;

    /** 开始上传的时间戳 */
    private long startTime;


    public static FileModel newLocal(String localPath) {
        FileModel model = new FileModel();
        model.fileName = Utils.getFileSimpleName(localPath);
        model.fileSize = Utils.getFileSize(localPath);
        model.progress = 0;
        model.speed = 0;
        model.status = FileStatus.PENDING;
        model.localPath = localPath;
        model.url = "";
        model.key = MD5.toMD5(localPath);
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
