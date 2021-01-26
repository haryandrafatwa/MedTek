package com.example.medtek.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.medtek.model.state.ChatType;

import java.util.ArrayList;
import java.util.List;

public class ChatsModel implements Parcelable {
    private int idConversation = 0;

    public static final Parcelable.Creator<ChatsModel> CREATOR = new Parcelable.Creator<ChatsModel>() {
        @Override
        public ChatsModel createFromParcel(Parcel source) {
            return new ChatsModel(source);
        }

        @Override
        public ChatsModel[] newArray(int size) {
            return new ChatsModel[size];
        }
    };
    private int idJanji = 0;

    private String senderName;
    private int idSender;
    private String senderAvatar = "";
    private List<Chat> chats;
    private boolean isActive = true;

    private String createdAt;

    private String finishedAt;

    public int getIdConversation() {
        return idConversation;
    }

    private boolean isDone = false;

    protected ChatsModel(Parcel in) {
        this.idConversation = in.readInt();
        this.idJanji = in.readInt();
        this.idSender = in.readInt();
        this.senderName = in.readString();
        this.senderAvatar = in.readString();
        this.chats = in.createTypedArrayList(Chat.CREATOR);
        this.isActive = in.readByte() != 0;
        this.isDone = in.readByte() != 0;
        this.createdAt = in.readString();
        this.finishedAt = in.readString();
    }

    public void setIdConversation(int idConversation) {
        this.idConversation = idConversation;
    }

    public int getIdJanji() {
        return idJanji;
    }

    public void setIdJanji(int idJanji) {
        this.idJanji = idJanji;
    }

    public int getIdSender() {
        return idSender;
    }

    public void setIdSender(int idSender) {
        this.idSender = idSender;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public List<Chat> getChats() {
        return chats;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isDone() {
        return isDone;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public ChatsModel(int idConversation, int idJanji, String senderName, int idSender, String createdAt, String senderAvatar) {
        this.idConversation = idConversation;
        this.idJanji = idJanji;
        this.senderName = senderName;
        this.idSender = idSender;
        this.createdAt = createdAt;
        this.senderAvatar = senderAvatar;
        this.chats = new ArrayList<>();
    }

    public ChatsModel(int idConversation, List<Chat> chats) {
        this.idConversation = idConversation;
        this.chats = chats;
    }

    public ChatsModel(int idConversation, String senderName, String createdAt, String senderAvatar) {
        this.idConversation = idConversation;
        this.chats = new ArrayList<>();
        this.senderName = senderName;
        this.createdAt = createdAt;
        this.senderAvatar = senderAvatar;
    }

    public static class Chat implements Parcelable {
        private int idChat = 0;
        private int idSender = 0;
        private int idReceiver = 0;
        private String time;
        private boolean isRead;
        private ChatType type;
        private String message;

        public Chat(int idChat, int idSender, int idReceiver, String time, boolean isRead, ChatType type, String message) {
            this.idChat = idChat;
            this.idSender = idSender;
            this.idReceiver = idReceiver;
            this.time = time;
            this.isRead = isRead;
            this.type = type;
            this.message = message;
        }


        public int getIdChat() {
            return idChat;
        }

        public String getMessage() {
            return message;
        }

        public int getIdSender() {
            return idSender;
        }

        public int getIdReceiver() {
            return idReceiver;
        }

        public String getTime() {
            return time;
        }

        public boolean isRead() {
            return isRead;
        }

        public ChatType getType() {
            return type;
        }

        public static final Creator<Chat> CREATOR = new Creator<Chat>() {
            @Override
            public Chat createFromParcel(Parcel source) {
                return new Chat(source);
            }

            @Override
            public Chat[] newArray(int size) {
                return new Chat[size];
            }
        };

        protected Chat(Parcel in) {
            this.idChat = in.readInt();
            this.idSender = in.readInt();
            this.idReceiver = in.readInt();
            this.time = in.readString();
            this.isRead = in.readByte() != 0;
            int tmpType = in.readInt();
            this.type = tmpType == -1 ? null : ChatType.values()[tmpType];
            this.message = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.idChat);
            dest.writeInt(this.idSender);
            dest.writeInt(this.idReceiver);
            dest.writeString(this.time);
            dest.writeByte(this.isRead ? (byte) 1 : (byte) 0);
            dest.writeInt(this.type == null ? -1 : this.type.ordinal());
            dest.writeString(this.message);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.idConversation);
        dest.writeInt(this.idJanji);
        dest.writeInt(this.idSender);
        dest.writeString(this.senderName);
        dest.writeString(this.senderAvatar);
        dest.writeTypedList(this.chats);
        dest.writeByte(this.isActive ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isDone ? (byte) 1 : (byte) 0);
        dest.writeString(this.createdAt);
        dest.writeString(this.finishedAt);
    }
}
