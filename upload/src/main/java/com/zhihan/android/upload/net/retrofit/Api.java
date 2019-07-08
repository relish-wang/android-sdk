package com.zhihan.android.upload.net.retrofit;

import com.zhihan.android.upload.UploadSdk;
import com.zhihan.android.upload.net.QiniuToken;
import com.zhihan.android.upload.net.retrofit.ClientApi;
import com.zhihan.android.upload.net.retrofit.HttpLoggingInterceptor;
import com.zhihan.android.upload.net.retrofit.RetrofitManager;
import com.zhihan.android.upload.net.retrofit.StandResponse;

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
        if (UploadSdk.getConfig().isDebug()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okBuilder.addNetworkInterceptor(loggingInterceptor);
        }
        RetrofitManager.configTrustAll(okBuilder);

        Retrofit.Builder builder = new Retrofit.Builder();

        okHttpClient = okBuilder.build();

        builder.baseUrl(UploadSdk.getConfig().getBaseUrl())
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create());

        clientApi = builder.build().create(ClientApi.class);
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }


    public ClientApi getClientApi() {
        return clientApi;
    }


}