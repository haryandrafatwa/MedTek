package com.example.medtek.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.medtek.App;
import com.example.medtek.callback.BaseCallback;
import com.example.medtek.network.APIService;
import com.example.medtek.network.base.BaseResponse;
import com.example.medtek.network.response.GetJanjiListResponse;
import com.example.medtek.network.response.GetJanjiSingleResponse;
import com.example.medtek.utils.RxUtils;

import static com.example.medtek.utils.ErrorHandler.handlerError;

public class AppointmentController extends APIService {
    private static final String TAG = AppointmentController.class.getSimpleName();

    private final Context context;

    public AppointmentController() {
        this.context = App.getContext();
    }

    @SuppressLint("CheckResult")
    public void getJanjiList(BaseCallback<GetJanjiListResponse> callback) {
        dataManager.getJanjiList()
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(getJanjiListResponse -> {
                    if (!getJanjiListResponse.getError()) {
                        try {
                            callback.onSuccess(getJanjiListResponse);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        String msg = getJanjiListResponse.getMessage();
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
    public void getJanjiStart(String idJanji, BaseCallback<BaseResponse> callback) {
        dataManager.getJanjiStart(idJanji)
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(baseResponse -> {
                    if (baseResponse.getSuccess()) {
                        callback.onSuccess(baseResponse);
                    } else {
                        String msg = baseResponse.getMessage();
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
    public void getJanjiSingle(String idJanji, BaseCallback<GetJanjiSingleResponse> callback) {
        dataManager.getJanjiSingle(idJanji)
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(getJanjiSingleResponse -> {
                    if (!getJanjiSingleResponse.getError()) {
                        callback.onSuccess(getJanjiSingleResponse);
                    } else {
                        String msg = getJanjiSingleResponse.getMessage();
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
    public void getJanjiQueue(String idJanji, BaseCallback<BaseResponse> callback) {
        dataManager.getJanjiQueue(idJanji)
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(baseResponse -> {
                    if (baseResponse.getSuccess()) {
                        callback.onSuccess(baseResponse);
                    } else {
                        String msg = baseResponse.getMessage();
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
    public void getJanjiDequeue(String idJanji, BaseCallback<BaseResponse> callback) {
        dataManager.getJanjiDequeue(idJanji)
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(baseResponse -> {
                    if (baseResponse.getSuccess()) {
                        callback.onSuccess(baseResponse);
                    } else {
                        String msg = baseResponse.getMessage();
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
    public void getJanjiEnd(String idJanji, BaseCallback<BaseResponse> callback) {
        dataManager.getJanjiEnd(idJanji)
                .doOnTerminate(() -> {
                })
                .compose(RxUtils.INSTANCE.applyScheduler())
                .compose(RxUtils.INSTANCE.applyApiCall())
                .subscribe(baseResponse -> {
                    if (baseResponse.getSuccess()) {
                        callback.onSuccess(baseResponse);
                    } else {
                        String msg = baseResponse.getMessage();
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
