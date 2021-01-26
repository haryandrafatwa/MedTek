package com.example.medtek.network.response;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {
    @SerializedName("id")
    private int idChat = 0;

    @SerializedName("idConversation")
    private int idConversation = 0;

    @SerializedName("sender_id")
    private int idSender = 0;

    @SerializedName("receiver_id")
    private int idReceiver = 0;

    @SerializedName("message")
    private String message = "";

    @SerializedName("attachment")
    private String attachment = "";

    @SerializedName("is_read")
    private boolean isRead = false;

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
