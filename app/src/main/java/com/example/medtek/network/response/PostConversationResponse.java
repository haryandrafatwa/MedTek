package com.example.medtek.network.response;

import com.example.medtek.network.base.BaseResponse;
import com.google.gson.annotations.SerializedName;

public class PostConversationResponse extends BaseResponse {
    @SerializedName("data")
    private ConversationModel data;

    public ConversationModel getData() {
        return data;
    }

    public static class ConversationModel {
        @SerializedName("idConversation")
        private final int idConversation = 0;

        @SerializedName("message")
        private final String message = "";

        @SerializedName("sender_id")
        private final int senderId = 0;

        @SerializedName("created_at")
        private final String createdAt = "";

        @SerializedName("updated_at")
        private final String updatedAt = "";

        @SerializedName("id")
        private final int idChat = 0;

        @SerializedName("attachment")
        private final String attachment = "";

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

