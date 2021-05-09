package com.example.medtek.network;

import com.example.medtek.BuildConfig;
import com.example.medtek.constant.APPConstant;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class Retrofit untuk Flow Menu Home dan Others
 */

public class RetrofitClient {

    public static final String BASE_URL = BuildConfig.BASE_URL;
    public static final String BASE_SOCKET_URL = BuildConfig.BASE_URL + ":6001";

    private static final String BASE_URL_API = BuildConfig.BASE_URL + "/api/";
    private static RetrofitClient mInstace;
    private final Retrofit retrofit;

    private RetrofitClient(){
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL_API).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient()).build();
    }

    private static OkHttpClient okHttpClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
//                .retryOnConnectionFailure(true)
                .addInterceptor(interceptor)
                .addInterceptor(chain -> {
                    Request originalRequest = chain.request();

                    Request request = originalRequest.newBuilder()
                            .build();

                    return chain.proceed(request);
                })
//                .addNetworkInterceptor(chain -> {
//                    Request request = chain.request().newBuilder().addHeader("Connection", "close").build();
//                    return chain.proceed(request);
//                })
                .connectTimeout(APPConstant.API_TIMEOUT, TimeUnit.MINUTES)
                .readTimeout(APPConstant.API_TIMEOUT, TimeUnit.MINUTES)
                .writeTimeout(APPConstant.API_TIMEOUT, TimeUnit.MINUTES)
                .build();

        return httpClient;
    }

    public static synchronized  RetrofitClient getInstance(){
        if(mInstace == null){
            mInstace = new RetrofitClient();
        }
        return mInstace;

    }

    public APIInterface getApi(){
        return retrofit.create(APIInterface.class);
    }

}
