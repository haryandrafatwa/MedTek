package com.example.medtek.network.base;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {
    @SerializedName("message")
    String message = "";

    @SerializedName("exception")
    String exception = "";

    public String getMessage() {
        return message;
    }

    public String getException() {
        return exception;
    }
}
