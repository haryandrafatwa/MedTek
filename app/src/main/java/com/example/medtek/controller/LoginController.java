package com.example.medtek.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.example.medtek.App;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.network.APIService;
import com.example.medtek.network.response.AuthTokenResponse;
import com.example.medtek.network.response.GetUserResponse;
import com.example.medtek.utils.RxUtils;

import java.util.HashMap;

import static com.example.medtek.utils.ErrorHandler.handlerError;
import static com.example.medtek.utils.PropertyUtil.REFRESH_TOKEN;
import static com.example.medtek.utils.PropertyUtil.getData;
import static com.example.medtek.utils.Utils.isLoading;

public class LoginController extends APIService {
    private static final String TAG = LoginController.class.getSimpleName();

    private final Context context;

    public LoginController() {
        this.context = App.getContext();
    }

    @SuppressLint("CheckResult")
    public void getUser(String token, View loadingView, BaseCallback<GetUserResponse> callback) {
        HashMap<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + token);
        dataManager.getUser(header)
                .doOnTerminate(() -> {
                    isLoading(false, loadingView);
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(getUserResponse -> {
                    if (!getUserResponse.getError()) {
                        callback.onSuccess(getUserResponse);
                    } else {
                        String msg = getUserResponse.getMessage();
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
    public void getNewToken(BaseCallback<AuthTokenResponse> callback) {
        HashMap<String, String> body = new HashMap<>();
        body.put("refresh_token", (String) getData(REFRESH_TOKEN));
        dataManager.getNewAuthToken(body)
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(authTokenResponse -> {
                    if (!authTokenResponse.getError()) {
                        callback.onSuccess(authTokenResponse);
                    } else {
                        String msg = authTokenResponse.getMessage();
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
}
