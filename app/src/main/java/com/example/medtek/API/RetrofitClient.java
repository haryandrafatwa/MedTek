package com.example.medtek.API;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://192.168.137.1:8000/api/";
    private static RetrofitClient mInstace;
    private Retrofit retrofit;

    private RetrofitClient(){
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static synchronized  RetrofitClient getInstance(){
        if(mInstace == null){
            mInstace = new RetrofitClient();
        }
        return mInstace;

    }

    public Api getApi(){
        return retrofit.create(Api.class);
    }

}
