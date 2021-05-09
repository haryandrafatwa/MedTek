package com.example.medtek.callback;

/**
 * Interface handling error server dari API
 */

public interface ErrorHandlingCallback {
    void onNoConnection();

    void onServerBroken();
}
