package com.example.medtek.network.response;

import com.example.medtek.model.AppointmentModel;
import com.example.medtek.network.base.BaseResponse;
import com.google.gson.annotations.SerializedName;

public class GetJanjiSingleResponse extends BaseResponse {
    @SerializedName("data")
    private AppointmentModel data;

    public AppointmentModel getData() {
        return data;
    }
}
