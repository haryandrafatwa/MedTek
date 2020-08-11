package com.example.medtek.API;

import androidx.annotation.Nullable;

import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
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

    @Headers({"Accept:application/json"})
    @POST("register/")
    @FormUrlEncoded
    Call<ResponseBody> register_pasien(
            @Query("name") String name,
            @Query("email") String email,
            @Query("password") String password,
            @Field("password_confirmation") String password_confirmation
    );

    @Headers({"Accept:application/json"})
    @POST("login/")
    @FormUrlEncoded
    Call<ResponseBody> login(
            @Query("email") String email,
            @Field("password") String password
    );

    @Headers({"Accept:application/json"})
    @POST("refresh-token/")
    @FormUrlEncoded
    Call<ResponseBody> refresh_token(
            @Field("refresh_token") String refresh_token
    );

    @Headers({"Accept:application/json"})
    @GET("user/")
    Call<ResponseBody> getUser(
            @Header("Authorization") String token
    );

    @Headers({"Accept:application/json"})
    @GET("logout/")
    Call<ResponseBody> logout(
            @Header("Authorization") String token
    );

    @Headers({"Accept:application/json"})
    @GET("article")
    Call<ResponseBody> getAllArticles();

    @Headers({"Accept:application/json"})
    @GET("article/{id}")
    Call<ResponseBody> getArtikelId(
            @Path("id") int id
    );

    @Headers({"Accept:application/json"})
    @GET("wallet/")
    Call<ResponseBody> getUserWallet(
            @Header("Authorization") String token
    );

    @Headers({"Accept:application/json"})
    @POST("wallet/topup")
    @FormUrlEncoded
    Call<ResponseBody> postTopup(
            @Header("Authorization") String token,
            @Field("topup") int topup

    );

    @Headers({"Accept:application/json"})
    @GET("get-dokter/{id}")
    Call<ResponseBody> getDokterId(
            @Path("id") int id
    );

    @Headers({"Accept:application/json"})
    @GET("get-dokter/")
    Call<ResponseBody> getAllDokter();

    @Headers({"Accept:application/json"})
    @GET("get-dokter-rated/")
    Call<ResponseBody> getDokterRated();

    @Headers({"Accept:application/json"})
    @GET("feedback/{id}")
    Call<ResponseBody> getFeedbackId(
            @Path("id") int id
    );

    @Headers({"Accept:application/json"})
    @GET("get-hospital/")
    Call<ResponseBody> getAllHospital();

    @Headers({"Accept:application/json"})
    @GET("get-hospital/{id}")
    Call<ResponseBody> getHospitalId(
            @Path("id") int id
    );

    @Headers({"Connection:close"})
    @GET("ktp/")
    Call<ResponseBody> getKTP(
            @Header("Authorization") String token
    );

    @Headers({"Accept:application/json"})
    @POST("edit-ktp/")
    @Multipart
    Call<ResponseBody> uploadKTP(
            @Header("Authorization") String token,
            @Part MultipartBody.Part ktp
    );

    @Headers({"Accept:application/json","Content-Type:application/json"})
    @PUT("editprofile/")
    Call<ResponseBody> updateProfileTglLahir(
            @Header("Authorization") String token,
            @Body RequestBody body
    );

    @Headers({"Accept:application/json"})
    @POST("janji/")
    @FormUrlEncoded
    Call<ResponseBody> buatJanji(
            @Header("Authorization") String token,
            @Query("idDokter") int idDokter,
            @Query("tglJanji") String tglJanji,
            @Query("detailJanji") String detailJanji,
            @Field("day") String day
    );

    @Headers({"Accept:application/json"})
    @GET("janji/")
    @Nullable
    Call<ResponseBody> getUserJanji(
            @Header("Authorization") String token
    );

    @Headers({"Accept:application/json"})
    @POST("payment/")
    @FormUrlEncoded
    Call<ResponseBody> bayar(
            @Header("Authorization") String token,
            @Field("idTransaksi") int idTransaksi
    );

    @Headers({"Accept:application/json"})
    @GET("get-pasien/{id}")
    Call<ResponseBody> getPasienId(
            @Path("id") int id
    );

    @Headers({"Accept:application/json"})
    @GET("janji/{id}")
    Call<ResponseBody> getJanjiId(
            @Header("Authorization") String token,
            @Path("id") int id
    );
}
