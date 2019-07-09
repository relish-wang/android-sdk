package com.zhihan.android.upload.net;


import java.io.Serializable;

/**
 * 标准接口返回类型
 *
 * @param <V> 接口接收实际数据类型
 * @author wangxin
 * @since 20190706
 */
@SuppressWarnings("unused")
public class StdResponse<V> implements Serializable {

    private int code; //":20000,//状态码
    private String message; //":"请求成功",//状态码对应提示语
    private String status; // ":"succ",//succ成功,fail异常
    private V data;
    private long systemTime;//":1500000000000,//服务器当前时间戳

    public StdResponse() {
    }

    public V getData() {
        return this.data;
    }
}
