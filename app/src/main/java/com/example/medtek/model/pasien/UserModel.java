package com.example.medtek.model.pasien;

import android.os.Parcel;
import android.os.Parcelable;

public class UserModel implements Parcelable {
    private String name,email;
    private int isVerified;
    private float wallet;

    public UserModel(Parcel in) {
        this.name = in.readString();
        this.email = in.readString();
        this.isVerified = in.readInt();
        this.wallet = in.readFloat();
    }

    public UserModel(String name, String email,int isVerified,float wallet) {
        this.name = name;
        this.email = email;
        this.isVerified = isVerified;
        this.wallet = wallet;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(int isVerified) {
        this.isVerified = isVerified;
    }

    public float getWallet() {
        return wallet;
    }

    public void setWallet(float wallet) {
        this.wallet = wallet;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.email);
        dest.writeInt(this.isVerified);
        dest.writeFloat(this.isVerified);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

}
