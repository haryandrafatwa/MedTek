package com.example.medtek.network;

import com.example.medtek.network.base.BaseResponse;
import com.example.medtek.network.request.LoginRequest;
import com.example.medtek.network.response.AuthTokenResponse;
import com.example.medtek.network.response.GetConversationResponse;
import com.example.medtek.network.response.GetInfoUserResponse;
import com.example.medtek.network.response.GetJanjiListResponse;
import com.example.medtek.network.response.GetJanjiSingleResponse;
import com.example.medtek.network.response.GetUserResponse;

import java.util.HashMap;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class DataManager {

    private final APIInterface apiInterface;

    public DataManager(APIInterface apiInterface) {
        this.apiInterface = apiInterface;
    }

    public Observable<AuthTokenResponse> getLoginUser(LoginRequest body) {
        return apiInterface.loginUser(body);
    }

    public Observable<AuthTokenResponse> getNewAuthToken(HashMap<String, String> body) {
        return apiInterface.getNewAuthToken(APIService.getAuthHeader(), body);
    }

    public Observable<GetUserResponse> getUser(HashMap<String, String> header) {
        return apiInterface.getUser(header);
    }

    public Observable<GetInfoUserResponse> getPasien(String idPasien) {
        return apiInterface.getPasien(idPasien);
    }

    public Observable<GetInfoUserResponse> getDokter(String idDokter) {
        return apiInterface.getDokter(idDokter);
    }

    public Observable<GetJanjiListResponse> getJanjiList() {
        return apiInterface.getJanjiList(APIService.getAuthHeader());
    }

    public Observable<GetJanjiSingleResponse> getJanjiSingle(String id) {
        return apiInterface.getJanjiSingle(APIService.getAuthHeader(), id);
    }

    public Observable<BaseResponse> getJanjiQueue(String idJanji) {
        return apiInterface.getJanjiQueue(APIService.getAuthHeader(), idJanji);
    }

    public Observable<BaseResponse> getJanjiDequeue(String idJanji) {
        return apiInterface.getJanjiDequeue(APIService.getAuthHeader(), idJanji);
    }

    public Observable<BaseResponse> getJanjiStart(String idJanji) {
        return apiInterface.getJanjiStart(APIService.getAuthHeader(), idJanji);
    }

    public Observable<BaseResponse> getJanjiEnd(String idJanji) {
        return apiInterface.getJanjiEnd(APIService.getAuthHeader(), idJanji);
    }

    public Observable<BaseResponse> getJanjiDecline(String idJanji) {
        return apiInterface.getJanjiDecline(APIService.getAuthHeader(), idJanji);
    }

    public Observable<GetJanjiListResponse> getJanjiQueueList() {
        return apiInterface.getJanjiQueueList(APIService.getAuthHeader());
    }

//    public Observable<PostConversationResponse> postConversation(HashMap<String, String> body, String idConversation) {
//        return apiInterface.postConversation(APIService.getAuthHeader(), body, idConversation);
//    }

    public Observable<GetConversationResponse> getConversation(String idConversation) {
        return apiInterface.getConversation(APIService.getAuthHeader(), idConversation);
    }

    public Call<ResponseBody> getMessageImage(HashMap<String, String> body) {
        return apiInterface.getMessageImage(APIService.getAuthHeader(), body);
    }
}
