package com.smart.android.uploadqueue;

import java.util.List;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190705
 */
public interface OnProgressUpdateListener {

    void onUploadCompleted(List<FileModel> models);
}
