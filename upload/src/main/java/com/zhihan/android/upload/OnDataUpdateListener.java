package com.zhihan.android.upload;

import java.util.List;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190708
 */
public interface OnDataUpdateListener {

    void onDataUpdate(List<FileModel> fileModelList);
}
