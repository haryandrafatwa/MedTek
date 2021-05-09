package com.example.medtek.network.response;

import com.example.medtek.network.base.BaseResponse;
import com.google.gson.annotations.SerializedName;

/**
 * Model untuk collect data response Post Chat
 */

public class PostConversationResponse extends BaseResponse {
    @SerializedName("data")
    private ConversationModel data;

    public ConversationModel getData() {
        return data;
    }

    public static class ConversationModel {
        @SerializedName("idConversation")
        private int idConversation = 0;

        @SerializedName("message")
        private String message = "";

        @SerializedName("sender_id")
        private int senderId = 0;

        @SerializedName("created_at")
        private String createdAt = "";

        @SerializedName("updated_at")
        private String updatedAt = "";

        @SerializedName("id")
        private int idChat = 0;

        @SerializedName("attachment")
        private String attachment = "";

        public int getIdConversation() {
            return idConversation;
        }

        public String getMessage() {
            return message;
        }

        public int getSenderId() {
            return senderId;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public int getIdChat() {
            return idChat;
        }

        public String getAttachment() {
            return attachment;
        }
    }
}

