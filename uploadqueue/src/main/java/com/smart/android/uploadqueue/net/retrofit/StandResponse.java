package com.smart.android.uploadqueue.net.retrofit;


public class StandResponse<V> {


    protected int code; //":20000,//状态码
    protected String message; //":"请求成功",//状态码对应提示语
    protected String status; // ":"succ",//succ成功,fail异常
    protected V data;
    protected long systemTime;//":1500000000000,//服务器当前时间戳

    public StandResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(long systemTime) {
        this.systemTime = systemTime;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public V getData() {
        return this.data;
    }

    public void setData(V data) {
        this.data = data;
    }
}
