package com.zhihan.android.upload.net.retrofit;


import com.zhihan.android.upload.net.QiniuToken;

import io.reactivex.Observable;
import retrofit2.http.POST;

/**
 * @author Relish Wang
 * @since 2018/5/20
 */
public interface ClientApi {

    /**
     * 发送验证码
     */
    @POST("organize/app/qiniu/uptoken")
    Observable<StandResponse<QiniuToken>> getQiniuToken();
}
