package com.example.medtek.network.response;

import com.example.medtek.network.base.BaseResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetConversationListResponse extends BaseResponse {
    @SerializedName("data")
    private List<ConversationList> data;

    public List<ConversationList> getData() {
        return data;
    }

    public static class ConversationList {
        @SerializedName("id")
        private int idConversationList = 0;

        @SerializedName("created_at")
        private String createdAt = "";

        @SerializedName("updated_at")
        private String updatedAt = "";

        public int getIdConversationList() {
            return idConversationList;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }

}
