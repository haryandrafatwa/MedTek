package com.example.medtek.network;

import com.example.medtek.App;
import com.example.medtek.constant.APPConstant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.medtek.BuildConfig.BASE_URL;
import static com.example.medtek.utils.PropertyUtil.ACCESS_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;

public class APIService {

    public DataManager dataManager;

    public APIService() {
        dataManager = new DataManager(getClientService().create(APIInterface.class));
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
                            .header("Content-type", "application/json")
                            .header("Accept", "application/json")
                            .header("Connection", "close")
//                            .addHeader("Accept-Encoding", "identity")
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

    public static Retrofit getClientService() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(SelfSigningClientBuilder.createClient(App.getInstance().getApplicationContext()))
                .build();

        return retrofit;
    }


    public static HashMap<String, String> getAuthHeader() {
        HashMap<String, String> authHeader = new HashMap<>();
        String authToken = (String) getData(ACCESS_TOKEN);
        authHeader.put("Authorization", "Bearer " + authToken);
        return authHeader;
    }
}
