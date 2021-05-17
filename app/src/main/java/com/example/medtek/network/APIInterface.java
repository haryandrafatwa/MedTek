package com.example.medtek.network;

import androidx.annotation.Nullable;

import com.example.medtek.constant.APIConstant;
import com.example.medtek.network.base.BaseResponse;
import com.example.medtek.network.request.JanjiRequest;
import com.example.medtek.network.request.LoginRequest;
import com.example.medtek.network.response.AuthTokenResponse;
import com.example.medtek.network.response.GetConversationListResponse;
import com.example.medtek.network.response.GetConversationResponse;
import com.example.medtek.network.response.GetInfoUserResponse;
import com.example.medtek.network.response.GetJanjiListResponse;
import com.example.medtek.network.response.GetJanjiSingleResponse;
import com.example.medtek.network.response.GetUserResponse;
import com.example.medtek.network.response.PostConversationResponse;

import org.json.JSONObject;

import java.util.HashMap;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface APIInterface {

    @Headers({"Accept:application/json"})
    @POST("register-dokter")
    @Multipart
    Call<JSONObject> register_dokter(
            @Query("name") String name,
            @Query("email") String email,
            @Query("password") String password,
            @Query("password_confirmation") String password_confirmation,
            @Part MultipartBody.Part SIP
    );

    @Headers({"Accept:application/json"})
    @POST("register")
    @FormUrlEncoded
    Call<ResponseBody> register_pasien(
            @Query("name") String name,
            @Query("email") String email,
            @Query("password") String password,
            @Field("password_confirmation") String password_confirmation
    );

    @Headers({"Accept:application/json"})
    @POST("login")
    Call<ResponseBody> login(
            @Body LoginRequest body
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
    @GET("transaction-history/")
    Call<ResponseBody> getTransaction(
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
    @Multipart
    Call<ResponseBody> postTopup(
            @Header("Authorization") String token,
            @Query("topup") int topup,
            @Query("penerima") String penerima,
            @Query("rekeningPenerima") String rekeningPenerima,
            @Query("pengirim") String pengirim,
            @Query("rekeningPengirim") String rekeningPengirim,
            @Part MultipartBody.Part bukti

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

    @Headers({"'Accept':'application/json'","'Connection':'close'"})
    @GET("janji-file/{id}")
    Call<ResponseBody> getFile(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @Headers({"Connection:close","Content-Type:image/*","Accept:application/json"})
    @GET("ktp/")
    Call<ResponseBody> getKTP(
            @Header("Authorization") String token
    );

    @Headers({"Connection:close","Content-Type:image/*","Accept:application/json"})
    @GET("get-pasienktp/{id}")
    Call<ResponseBody> getPasienKTP(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @Headers({"Accept:application/json"})
    @POST("janji-file/{id}")
    @Multipart
    Call<ResponseBody> uploadFile(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Part MultipartBody.Part file
    );

    @Headers({"Accept:application/json"})
    @POST("edit-ktp")
    @Multipart
    Call<ResponseBody> uploadKTP(
            @Header("Authorization") String token,
            @Part MultipartBody.Part ktp
    );

    @Headers({"Accept:application/json"})
    @POST("edit-avatar/")
    @Multipart
    Call<ResponseBody> uploadAvatar(
            @Header("Authorization") String token,
            @Part MultipartBody.Part profilePic
    );

    @Headers({"Accept:application/json","Content-Type:application/json"})
    @PUT("editprofile")
    Call<ResponseBody> updateProfile(
            @Header("Authorization") String token,
            @Body RequestBody body
    );

    @Headers({"Accept:application/json"})
    @POST("janji")
//    @FormUrlEncoded
    Call<ResponseBody> buatJanji(
            @Header("Authorization") String token,
            @Body JanjiRequest body
//            @Query("idDokter") int idDokter,
//            @Query("tglJanji") String tglJanji,
//            @Query("detailJanji") String detailJanji,
//            @Field("day") String day
    );

    @Headers({"Accept:application/json"})
    @GET("janji/")
    @Nullable
    Call<ResponseBody> getUserJanji(
            @Header("Authorization") String token
    );

    @Headers({"Accept:application/json"})
    @GET("janji-queue")
    @Nullable
    Call<ResponseBody> getAntrianUser(
            @Header("Authorization") String token
    );

    @Headers({"Accept:application/json"})
    @GET("janji-queue/{id}")
    @Nullable
    Call<ResponseBody> getAntrianId(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @Headers({"Accept:application/json"})
    @POST("payment")
    @FormUrlEncoded
    Call<ResponseBody> bayar(
            @Header("Authorization") String token,
            @Field("idTransaksi") int idTransaksi,
            @Field("idJanji") int idJanji
    );

    @Headers({"Accept:application/json"})
    @POST("payment/snap")
    @FormUrlEncoded
    Call<ResponseBody> payment(
            @Header("Authorization") String token,
            @Field("idJanji") int idJanji
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

    @Headers({"Accept:application/json"})
    @GET("janji-decline/{id}")
    Call<ResponseBody> declineJanji(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @Headers({"Accept:application/json"})
    @GET("city/?key=21878b04bed20a2f2bb05bf41cd68d9c")
    Call<ResponseBody> getCityRajaOngkir(
    );

    @Headers({"Accept:application/json"})
    @GET("get-city/")
    Call<ResponseBody> getCity(
    );

    @Headers({"Accept:application/json"})
    @GET("get-servicefee/")
    Call<ResponseBody> getServiceFee(
    );

    //Ghazi


    //GET

    @GET(APIConstant.GET_USER)
    Observable<GetUserResponse> getUser(@HeaderMap HashMap<String, String> token);

    @GET(APIConstant.GET_PASIEN)
    Observable<GetInfoUserResponse> getPasien(@Path("id") String id);

    @GET(APIConstant.GET_DOKTER)
    Observable<GetInfoUserResponse> getDokter(@Path("id") String id);

    @GET(APIConstant.GET_JANJI_LIST)
    Observable<GetJanjiListResponse> getJanjiList(@HeaderMap HashMap<String, String> token);

    @GET(APIConstant.GET_JANJI_SINGLE)
    Observable<GetJanjiSingleResponse> getJanjiSingle(@HeaderMap HashMap<String, String> token, @Path("id") String id);

    @GET(APIConstant.GET_JANJI_QUEUE)
    Observable<BaseResponse> getJanjiQueue(@HeaderMap HashMap<String, String> token, @Path("id") String id);

    @GET(APIConstant.GET_JANJI_DEQUEUE)
    Observable<BaseResponse> getJanjiDequeue(@HeaderMap HashMap<String, String> token, @Path("id") String id);

    @GET(APIConstant.GET_JANJI_START)
    Observable<BaseResponse> getJanjiStart(@HeaderMap HashMap<String, String> token, @Path("id") String id);

    @GET(APIConstant.GET_JANJI_END)
    Observable<BaseResponse> getJanjiEnd(@HeaderMap HashMap<String, String> token, @Path("id") String id);

    @GET(APIConstant.GET_JANJI_DECLINE)
    Observable<BaseResponse> getJanjiDecline(@HeaderMap HashMap<String, String> token, @Path("id") String id);

    @GET(APIConstant.GET_JANJI_QUEUE_LIST)
    Observable<GetJanjiListResponse> getJanjiQueueList(@HeaderMap HashMap<String, String> token);

    @GET(APIConstant.GET_CONVERSATION)
    Observable<GetConversationResponse> getConversation(@HeaderMap HashMap<String, String> token, @Path("id") String id);

    @GET(APIConstant.GET_CONVERSATION_LIST)
    Observable<GetConversationListResponse> getConversationList(@HeaderMap HashMap<String, String> token);

    //POST

    @POST(APIConstant.POST_LOGIN)
    Observable<AuthTokenResponse> loginUser(@Body LoginRequest body);

    @POST(APIConstant.POST_REFRESH_TOKEN)
    Observable<AuthTokenResponse> getNewAuthToken(@HeaderMap HashMap<String, String> token, @Body HashMap<String, String> body);

    @POST(APIConstant.POST_CONVERSATION)
    Call<PostConversationResponse> postConversation(@HeaderMap HashMap<String, String> token, @Body HashMap<String, String> body, @Path("id") String id);

    @POST(APIConstant.POST_CONVERSATION)
    @Multipart
    Call<PostConversationResponse> postConversationFile(@HeaderMap HashMap<String, String> token,
                                                        @Part("message") RequestBody message,
                                                        @Part MultipartBody.Part file,
                                                        @Path("id") String id);

    @POST(APIConstant.POST_DOWNLOAD_IMAGE)
    @Streaming
    Call<ResponseBody> getMessageImage(@HeaderMap HashMap<String, String> token, @Body HashMap<String, String> body);

}
