package com.example.medtek.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.medtek.App;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.network.APIService;

import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.medtek.utils.ErrorHandler.handlerError;

public class ConversationController extends APIService {
    private static final String TAG = ConversationController.class.getSimpleName();

    private final Context context;

    public ConversationController() {
        this.context = App.getContext();
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
