package com.example.medtek.callback;


/**
 * Interface handling error dan success dari API
 */

public interface BaseCallback<T> extends ErrorHandlingCallback {

    void onSuccess(T result);

    void onError(Throwable t);
}
