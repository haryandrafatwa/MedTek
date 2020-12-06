package com.example.medtek.callback;

public interface BaseCallback<T> extends ErrorHandlingCallback {

    void onSuccess(T result);

    void onError(Throwable t);
}
