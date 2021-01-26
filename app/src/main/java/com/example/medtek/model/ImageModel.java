package com.example.medtek.model;

import com.google.gson.annotations.SerializedName;

public class ImageModel {
    @SerializedName("id")
    private int id = 0;

    @SerializedName("article_id")
    private int articleId = 0;

    @SerializedName("hospital_id")
    private int hopitalId = 0;

    @SerializedName("user_id")
    private int userId = 0;

    @SerializedName("type_id")
    private int typeId = 0;

    @SerializedName("filename")
    private String filename = "";

    @SerializedName("path")
    private String path = "";

    @SerializedName("created_at")
    private String createdAt = "";

    @SerializedName("updated_at")
    private String updatedAt = "";

    @SerializedName("type")
    private Type type;

    public ImageModel(int id, int articleId, int hopitalId, int userId, int typeId, String filename, String path, String createdAt, String updatedAt, Type type) {
        this.id = id;
        this.articleId = articleId;
        this.hopitalId = hopitalId;
        this.userId = userId;
        this.typeId = typeId;
        this.filename = filename;
        this.path = path;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.type = type;
    }

    public int getHopitalId() {
        return hopitalId;
    }

    public int getId() {
        return id;
    }

    public int getArticleId() {
        return articleId;
    }

    public Type getType() {
        return type;
    }

    public int getUserId() {
        return userId;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public class Type {
        @SerializedName("id")
        private int idImage = 0;

        @SerializedName("type")
        private String type = "";

        public int getIdImage() {
            return idImage;
        }

        public String getType() {
            return type;
        }
    }
}

