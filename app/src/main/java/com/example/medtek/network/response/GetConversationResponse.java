package com.example.medtek.network.response;

import com.example.medtek.network.base.BaseResponse;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GetConversationResponse extends BaseResponse {
    @SerializedName("data")
    private Conversation data;

    public Conversation getData() {
        return data;
    }

    public static class Conversation {
        @SerializedName("id")
        private int idConversation = 0;

        @SerializedName("chat")
        private List<ChatModel> chats = new ArrayList<>();

        @SerializedName("created_at")
        private String createdAt = "";

        @SerializedName("updated_at")
        private String updatedAt = "";

        public int getIdConversation() {
            return idConversation;
        }

        public List<ChatModel> getChats() {
            return chats;
        }

        public class ChatModel {
            @SerializedName("id")
            private int idChat = 0;

            @SerializedName("idConversation")
            private int idConversation = 0;

            @SerializedName("sender_id")
            private int idSender = 0;

            @SerializedName("message")
            private String message = "";

            @SerializedName("attachment")
            private String attachment = "";

            @SerializedName("is_read")
            private boolean isRead = false;

            @SerializedName("created_at")
            private String createdAt = "";

            @SerializedName("updated_at")
            private String updatedAt = "";

            public int getIdChat() {
                return idChat;
            }

            public int getIdConversation() {
                return idConversation;
            }

            public int getIdSender() {
                return idSender;
            }

            public String getMessage() {
                return message;
            }

            public String getAttachment() {
                return attachment;
            }

            public boolean isRead() {
                return isRead;
            }

            public String getCreatedAt() {
                return createdAt;
            }

            public String getUpdatedAt() {
                return updatedAt;
            }
        }
    }
}

