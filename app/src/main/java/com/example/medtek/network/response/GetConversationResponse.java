package com.example.medtek.network.response;

import com.example.medtek.network.base.BaseResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetConversationResponse extends BaseResponse {
    @SerializedName("data")
    private Conversation data;

    public Conversation getData() {
        return data;
    }

    public static class Conversation {
        @SerializedName("id")
        private final int idConversation = 0;

        @SerializedName("chat")
        private List<ChatModel> chats;

        @SerializedName("created_at")
        private final String createdAt = "";

        @SerializedName("updated_at")
        private final String updatedAt = "";

        public int getIdConversation() {
            return idConversation;
        }

        public List<ChatModel> getChats() {
            return chats;
        }

        public class ChatModel {
            @SerializedName("id")
            private final int idChat = 0;

            @SerializedName("idConversation")
            private final int idConversation = 0;

            @SerializedName("sender_id")
            private final int idSender = 0;

            @SerializedName("message")
            private final String message = "";

            @SerializedName("attachment")
            private final String attachment = "";

            @SerializedName("is_read")
            private final boolean isRead = false;

            @SerializedName("created_at")
            private final String createdAt = "";

            @SerializedName("updated_at")
            private final String updatedAt = "";

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

