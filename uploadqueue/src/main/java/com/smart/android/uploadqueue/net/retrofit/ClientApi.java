package com.smart.android.uploadqueue.net.retrofit;


import com.smart.android.uploadqueue.net.QiniuToken;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author Relish Wang
 * @since 2018/5/20
 */
public interface ClientApi {

    /**
     * 发送验证码
     */
    @POST("/qiniu/uptoken")
    Observable<StandResponse<QiniuToken>> getQiniuToken();
}
