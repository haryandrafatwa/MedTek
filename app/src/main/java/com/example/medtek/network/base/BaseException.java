package com.example.medtek.network.base;

/**
 * Base untuk handling Exception dari API
 */

public class BaseException extends Exception {
    int responseCode;
    //    BaseResponse<JsonElement> response;
    ErrorResponse response;

    public BaseException(ErrorResponse response) {
        this.response = response;
        this.responseCode = 200;
    }

    public BaseException(int responseCode, ErrorResponse response) {
        this.response = response;
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ErrorResponse getResponse() {
        return response;
    }
}
