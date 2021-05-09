package com.example.medtek.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.medtek.App;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.network.APIService;
import com.example.medtek.network.response.GetInfoUserResponse;
import com.example.medtek.utils.RxUtils;

import static com.example.medtek.utils.ErrorHandler.handlerError;

/**
 * Controller untuk FLow User
 */

public class UserController extends APIService {
    private static final String TAG = UserController.class.getSimpleName();

    private final Context context;

    public UserController() {
        this.context = App.getContext();
    }

    @SuppressLint("CheckResult")
    public void getPasien(String id, BaseCallback<GetInfoUserResponse> callback) {
        dataManager.getPasien(id)
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(getInfoUserResponse -> {
                    if (!getInfoUserResponse.getError()) {
                        callback.onSuccess(getInfoUserResponse);
                    } else {
                        String msg = getInfoUserResponse.getMessage();
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
    public void getDokter(String id, BaseCallback<GetInfoUserResponse> callback) {
        dataManager.getDokter(id)
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(getInfoUserResponse -> {
                    if (!getInfoUserResponse.getError()) {
                        callback.onSuccess(getInfoUserResponse);
                    } else {
                        String msg = getInfoUserResponse.getMessage();
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

