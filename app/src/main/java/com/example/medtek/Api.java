package com.example.medtek;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface Api {

    @Headers({"Accept:application/json"})
    @POST("register-dokter/")
    @Multipart
    Call<JSONObject> register_dokter(
            @Query("name") String name,
            @Query("email") String email,
            @Query("password") String password,
            @Query("password_confirmation") String password_confirmation,
            @Part MultipartBody.Part SIP
    );
}
