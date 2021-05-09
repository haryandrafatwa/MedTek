package com.example.medtek.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.medtek.App;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.network.APIService;
import com.example.medtek.network.response.GetConversationListResponse;
import com.example.medtek.network.response.GetConversationResponse;
import com.example.medtek.utils.RxUtils;

import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.medtek.utils.ErrorHandler.handlerError;
import static java.lang.String.valueOf;

/**
 * Controller untuk FLow Chat
 */

public class ConversationController extends APIService {
    private static final String TAG = ConversationController.class.getSimpleName();

    private final Context context;

    public ConversationController() {
        this.context = App.getContext();
    }

    @SuppressLint("CheckResult")
    public void getConversation(int idConversation, BaseCallback<GetConversationResponse> callback) {
        dataManager.getConversation(valueOf(idConversation))
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(getConversationResponse -> {
                    GetConversationResponse.Conversation response  = getConversationResponse.getData();
                    Log.d(TAG, "CheckId :" + response.getIdConversation());
                    if (!getConversationResponse.getError()) {
                        try {
                            callback.onSuccess(getConversationResponse);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        String msg = getConversationResponse.getMessage();
                        Log.e(TAG, msg);
                        callback.onError(new Throwable("Failed to get data"));
                    }
                }, throwable -> {
                    String errorMsg = handlerError(throwable);
                    if (errorMsg.equals("Server Broken")) {
                        callback.onServerBroken();
                    } else {
                        callback.onNoConnection();
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void getConversationList(BaseCallback<GetConversationListResponse> callback) {
        dataManager.getConversationList()
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(getConversationListResponse -> {
                    if (!getConversationListResponse.getError()) {
                        try {
                            callback.onSuccess(getConversationListResponse);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        String msg = getConversationListResponse.getMessage();
                        Log.e(TAG, msg);
                        callback.onError(new Throwable("Failed to get data"));
                    }
                }, throwable -> {
                    String errorMsg = handlerError(throwable);
                    if (errorMsg.equals("Server Broken")) {
                        callback.onServerBroken();
                    } else {
                        callback.onNoConnection();
                    }
                });
    }

    @SuppressLint("StaticFieldLeak")
    public void getImageMessage(String path, BaseCallback<ResponseBody> callback) {
        Log.d(TAG, path);
//        String pathFormat = path.substring(8);

        HashMap<String, String> body = new HashMap<>();
        body.put("path", path);

        Call<ResponseBody> call = dataManager.getMessageImage(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {

                if (response.isSuccessful()) {
                    ResponseBody data = response.body();
                    if (data != null) {
                        callback.onSuccess(response.body());
                    }
                } else {
                    if (response.code() == HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                        callback.onServerBroken();
                    } else {
                        callback.onError(new Throwable("Failed to get data"));
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                String errorMsg = handlerError(t);
                if (errorMsg.equals("Server Broken")) {
                    callback.onServerBroken();
                } else {
                    callback.onNoConnection();
                }
            }
        });
    }
}
