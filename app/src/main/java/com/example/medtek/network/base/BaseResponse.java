package com.example.medtek.network.base;

import com.google.gson.annotations.SerializedName;

public class BaseResponse {
    @SerializedName("error")
    private final Boolean error = false;

    @SerializedName("success")
    private final Boolean success = false;

    @SerializedName("message")
    private final String message = "";

    public Boolean getError() {
        return error;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
