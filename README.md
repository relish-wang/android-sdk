# AndroidUploadModule

> Android图片/文件上传队列。

- 支持断点续传
- 支持单/多文件上传
- 支持线程池管理
- 支持文件上传进度/状态回调
- 支持回调文件自定义排序规则

## 依赖

```groovy
implementation "com.smart.android:upload:0.0.1"
```

## 使用方法

具体的使用方法可以参看Demo里的`SingleFileActivity`和`MultiFileActivity`的代码。

### 1 初始化

```java
UploadSdk.init(BuildConfig.DEBUG)
        // 上传地址的host, 默认是学智的host
        .setBaseUrl(host)
        // 排序用的比较器, 未设置的话会用默认的比较器(FileComparator.DEFAULT)
        .setFileComparator(mComparator)
        // 分离器, 用于将回调的数据分离为Map<String, List<FileModel>>的格式(有默认实现Separator.DEFAULT)
        .setSeparator(mSeparator)
        // 线程池大小(设定可以允许多少个线程同时运行, 默认为2, 最小为1, 最大为6)
        .setThreadCount(2)
```

### 2 选取照片/文件上传
#### 2.1 单文件上传与回调
上传(不带回调):

```java
UploadSdk.upload(localPath);
```

上传(带回调):

```java
UploadSdk.upload(localPath, new OnSingleFileListener(){
    @Override
    public void onDataUpdate(@NonNull FileModel single) {
       // FileModel中包含了文件上传进度/上传状态的所有信息
    }
});
```

#### 2.2 多文件上传与回调

上传
```java
// localPaths: List<String>
UploadSdk.upload(localPaths);
```

回调:
```java
UploadSdk.addOnDataUpdateListener(new OnDataUpdateListener(){
    @Override
    public void onDataUpdate(
          @Nullable Boolean isAllPaused,
          @NonNull Map<String, List<FileModel>> map) {
          // isAllPaused: true-所有文件暂停上传;false-并非所有文件暂停上传;null-保持现状
          // map: 默认的分离器会将所有的FileModel分离为
          //      1 "upload": 除上传完成外其他状态的所有文件任务
          //      2 "completed": 上传完成的文件任务
    }
});
```
### 3 操作

#### 3.1 删除
```java
UploadSdk.remove(fileModel);
```
#### 3.2 暂停上传
注: 只有**上传中**的文件上传任务可以执行**暂停上传**
```java
UploadSdk.pause(fileModel);
```
#### 3.3 恢复上传

注: 只有**暂停中**和**上传失败**的文件上传任务可以执行**恢复上传**

这里单文件和多文件恢复有所不同。实际上的暂停上传其实是取消上传, 由于有断点续传的功能存在, 效果上像是恢复上传。由于多文件上传的监听器是全局的, 故可以生效，但单文件上传的监听器是局部的，如果想恢复上传, 需要重新添加监听器。

**多文件上传的恢复**
```java
UploadSdk.resume(fileModel);
```
**单文件上传的恢复**
```java
UploadSdk.resume(fileModel, (fileModel)->{
		// 刷新UI
});
```

#### 3.4 全部暂停
```java
UploadSdk.pauseAll();
```
#### 3.5 全部恢复上传
```java
UploadSdk.resumeAll();
```
#### 3.6 清空已完成任务
```java
UploadSdk.clearCompleted();
```

## 混淆配置

```groovy
# Parcelable
-keepnames class * implements android.os.Parcelable {
 public static final ** CREATOR;
}

-keepclassmembers class * implements android.os.Parcelable {
public <fields>;
private <fields>;
}
# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
```

## 版本日志

[CHANGELOG.md](./CHANGELOG.md)





