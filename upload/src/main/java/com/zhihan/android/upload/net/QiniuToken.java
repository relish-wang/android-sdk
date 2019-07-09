package com.zhihan.android.upload.net;

import java.io.Serializable;

/**
 * @author wangxin
 * @since 20190621
 */
public class QiniuToken implements Serializable {

    private String uptoken;

    public String getUptoken() {
        return uptoken;
    }
}
