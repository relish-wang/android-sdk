package com.zhihan.android.upload;

import android.app.Application;

/**
 * @author relish <a href="mailto:relish.wang@gmail.com">Contact me.</a>
 * @since 20190706
 */
public class UploadSdk {


    private static UploadSdkConfig sConfig;

    public static UploadSdkConfig.Editor init(Application app, boolean isDebug) {
        sConfig = new UploadSdkConfig(app, isDebug);
        return sConfig.editor();
    }

    public static UploadSdkConfig getConfig() {
        return sConfig;
    }

    private UploadSdk() {
        throw new UnsupportedOperationException();
    }

}
