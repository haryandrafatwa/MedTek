package com.example.medtek.network.response;

import com.example.medtek.model.UserModel;
import com.example.medtek.network.base.BaseResponse;
import com.google.gson.annotations.SerializedName;

/**
 * Model untuk collect data response User
 */

public class GetInfoUserResponse extends BaseResponse {
    @SerializedName("data")
    private UserModel data;

    public UserModel getData() {
        return data;
    }
}
