package com.example.medtek.network.request;

import com.google.gson.annotations.SerializedName;

/**
 * Model untuk request Login
 */

public class LoginRequest {

    @SerializedName("email")
    private String email = "";

    @SerializedName("password")
    private String password = "";

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginRequest() {
    }
}

