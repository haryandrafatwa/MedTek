package com.example.medtek.network.response;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {
    @SerializedName("id")
    private final int idChat = 0;

    @SerializedName("idConversation")
    private final int idConversation = 0;

    @SerializedName("sender_id")
    private final int idSender = 0;

    @SerializedName("receiver_id")
    private final int idReceiver = 0;

    @SerializedName("message")
    private final String message = "";

    @SerializedName("attachment")
    private final String attachment = "";

    @SerializedName("is_read")
    private final boolean isRead = false;

    public int getIdChat() {
        return idChat;
    }

    public int getIdConversation() {
        return idConversation;
    }

    public int getIdSender() {
        return idSender;
    }

    public int getIdReceiver() {
        return idReceiver;
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
}
