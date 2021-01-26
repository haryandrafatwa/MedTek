package com.example.medtek.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.medtek.model.state.ChatType;

public class MediaModel implements Parcelable {
    public static final Parcelable.Creator<MediaModel> CREATOR = new Parcelable.Creator<MediaModel>() {
        @Override
        public MediaModel createFromParcel(Parcel source) {
            return new MediaModel(source);
        }

        @Override
        public MediaModel[] newArray(int size) {
            return new MediaModel[size];
        }
    };
    private final int idMedia;
    private final int idChat;
    private final String filename;
    private String path;
    private final String extension;
    private final ChatType type;

    public MediaModel(int idMedia, int idChat, String filename, String path, String extension, ChatType type) {
        this.idMedia = idMedia;
        this.idChat = idChat;
        this.filename = filename;
        this.path = path;
        this.extension = extension;
        this.type = type;
    }

    protected MediaModel(Parcel in) {
        this.idMedia = in.readInt();
        this.idChat = in.readInt();
        this.filename = in.readString();
        this.path = in.readString();
        this.extension = in.readString();
        int tmpType = in.readInt();
        this.type = tmpType == -1 ? null : ChatType.values()[tmpType];
    }

    public int getIdMedia() {
        return idMedia;
    }

    public int getIdChat() {
        return idChat;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExtension() {
        return extension;
    }

    public ChatType getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.idMedia);
        dest.writeInt(this.idChat);
        dest.writeString(this.filename);
        dest.writeString(this.path);
        dest.writeString(this.extension);
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
    }
}

