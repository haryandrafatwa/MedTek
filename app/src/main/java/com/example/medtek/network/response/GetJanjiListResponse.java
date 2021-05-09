package com.example.medtek.network.response;

import com.example.medtek.model.AppointmentModel;
import com.example.medtek.network.base.BaseResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model untuk collect data response Janji List
 */

public class GetJanjiListResponse extends BaseResponse {
    @SerializedName("data")
    private List<AppointmentModel> data;

    public List<AppointmentModel> getData() {
        return data;
    }
}
