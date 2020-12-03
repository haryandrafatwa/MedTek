package com.example.medtek.API;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RajaOngkirRetrofitClient {

    private String BASE_URL;
    private static RajaOngkirRetrofitClient mInstace;
    private Retrofit retrofit;

    private RajaOngkirRetrofitClient(){
        if (BASE_URL == null){
            BASE_URL = "https://api.rajaongkir.com/starter/";
        }
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static synchronized RajaOngkirRetrofitClient getInstance(){
        if(mInstace == null){
            mInstace = new RajaOngkirRetrofitClient();
        }
        return mInstace;

    }

    public Api getApi(){
        return retrofit.create(Api.class);
    }
}
