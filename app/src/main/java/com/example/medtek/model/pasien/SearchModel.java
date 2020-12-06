package com.example.medtek.model.pasien;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchModel implements Parcelable {

    private String nama, detail,image_url, city;
    private int id;

    public SearchModel(int id, String nama, String detail, String image_url, String city) {
        this.id = id;
        this.nama = nama;
        this.detail = detail;
        this.image_url = image_url;
        this.city = city;
    }

    protected SearchModel(Parcel in) {
        nama = in.readString();
        detail = in.readString();
        image_url = in.readString();
        city = in.readString();
        id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nama);
        dest.writeString(detail);
        dest.writeString(image_url);
        dest.writeString(city);
        dest.writeInt(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SearchModel> CREATOR = new Creator<SearchModel>() {
        @Override
        public SearchModel createFromParcel(Parcel in) {
            return new SearchModel(in);
        }

        @Override
        public SearchModel[] newArray(int size) {
            return new SearchModel[size];
        }
    };

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
