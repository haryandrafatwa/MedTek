package com.example.medtek.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RajaOngkirRetrofitClient {

    private String BASE_URL;
    private static RajaOngkirRetrofitClient mInstace;
    private final Retrofit retrofit;

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

    public APIInterface getApi(){
        return retrofit.create(APIInterface.class);
    }
}
