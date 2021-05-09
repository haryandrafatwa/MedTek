package com.example.medtek.utils;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.medtek.network.base.BaseException;

import java.net.SocketTimeoutException;

import retrofit2.HttpException;

/**
 * Class untuk handling error dari API
 */

public class ErrorHandler {
    public static String handlerError(@Nullable Throwable throwable) {
        Log.d("zzz", "error handler");
        Logger.log(throwable);

        if (throwable == null) {
            Log.d("Connection", "Error device network");
            return "Error Connection";
        }
        if (throwable instanceof BaseException) {
            BaseException baseException = (BaseException) throwable;
            if (baseException.getResponse() != null) {
                String msg = baseException.getResponse().getMessage();
                if (msg.isEmpty()) {
                    return "Error Connection";
                } else {
                    return msg;
                }
            }
        }
        if (throwable instanceof HttpException) {

        }

        if (throwable instanceof SocketTimeoutException) {
            return "Server Broken";
        }
        return "Error Connection";
    }
}
