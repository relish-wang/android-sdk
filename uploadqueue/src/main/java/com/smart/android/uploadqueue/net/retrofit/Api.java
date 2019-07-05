package com.smart.android.uploadqueue.net.retrofit;

import com.smart.android.uploadqueue.UploadManager;
import com.smart.android.uploadqueue.net.QiniuToken;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author relish
 * @since 2018/5/20
 */
public class Api extends RetrofitManager {

    protected ClientApi clientApi;

    private static volatile Api pApi;

    private OkHttpClient okHttpClient;

    public static synchronized Api getInstance() {
        if (pApi == null) {
            pApi = new Api();
        }
        return pApi;
    }

    public Api() {
        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
        okBuilder.connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);
        if (UploadManager.isDebug()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okBuilder.addNetworkInterceptor(loggingInterceptor);
        }
        RetrofitManager.configTrustAll(okBuilder);

        Retrofit.Builder builder = new Retrofit.Builder();

        okHttpClient = okBuilder.build();

        builder.baseUrl(UploadManager.sManager.getBaseUrl())
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        clientApi = builder.build().create(ClientApi.class);
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    /**
     * 获取验证码
     */
    public Observable<StandResponse<QiniuToken>> getQiniuToken() {
        return clientApi.getQiniuToken();
    }




}