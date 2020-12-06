package com.example.medtek.callback;

public interface ErrorHandlingCallback {
    void onNoConnection();

    void onServerBroken();
}
