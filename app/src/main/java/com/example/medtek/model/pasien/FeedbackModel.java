package com.example.medtek.model.pasien;

import android.os.Parcel;
import android.os.Parcelable;

public class FeedbackModel implements Parcelable {

    private int id;
    private String name, message, post_date, imageURL;
    private float rating;

    public FeedbackModel(int id, String name, String message, String post_date, String imageURL, float rating) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.post_date = post_date;
        this.imageURL = imageURL;
        this.rating = rating;
    }

    protected FeedbackModel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        message = in.readString();
        post_date = in.readString();
        imageURL = in.readString();
        rating = in.readFloat();
    }

    public static final Creator<FeedbackModel> CREATOR = new Creator<FeedbackModel>() {
        @Override
        public FeedbackModel createFromParcel(Parcel in) {
            return new FeedbackModel(in);
        }

        @Override
        public FeedbackModel[] newArray(int size) {
            return new FeedbackModel[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPost_date() {
        return post_date;
    }

    public void setPost_date(String post_date) {
        this.post_date = post_date;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(message);
        dest.writeString(post_date);
        dest.writeFloat(rating);
        dest.writeString(name);
        dest.writeString(imageURL);
    }
}
