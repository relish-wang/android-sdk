package com.zhihan.android.upload.net;


import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.POST;

/**
 * @author wangxin
 * @since 20190621
 */
public interface ClientApi {

    /**
     * 发送验证码
     */
    @POST("organize/app/qiniu/uptoken")
    Call<StdResponse<QiniuToken>> getQiniuToken();

    /**
     * 发送验证码
     */
    @POST("organize/app/qiniu/uptoken")
    Observable<StdResponse<QiniuToken>> getQiniuTokenObservable();
}
