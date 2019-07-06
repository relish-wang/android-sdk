package com.zhihan.android.upload.net;

import java.io.Serializable;

/**
 * Created by Hyu on 2018/8/9.
 * Email: fvaryu@163.com
 */
public class QiniuToken implements Serializable {

    private String uptoken;

    public QiniuToken() {
    }

    public QiniuToken(String uptoken) {
        this.uptoken = uptoken;
    }

    public String getUptoken() {
        return uptoken;
    }

    @Override
    public String toString() {
        return "QiniuToken{" +
                "uptoken='" + uptoken + '\'' +
                '}';
    }
}
