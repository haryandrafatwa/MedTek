package com.example.medtek.Pasien.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class ListDokterModel implements Parcelable {

    private String nama, spesialisasi,image_url;
    private int id;

    protected ListDokterModel(Parcel in) {
        id = in.readInt();
        nama = in.readString();
        spesialisasi = in.readString();
        image_url = in.readString();
    }

    public ListDokterModel(int id, String nama, String spesialisasi, String image_url) {
        this.id = id;
        this.nama = nama;
        this.spesialisasi = spesialisasi;
        this.image_url = image_url;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getSpesialisasi() {
        return spesialisasi;
    }

    public void setSpesialisasi(String spesialisasi) {
        this.spesialisasi = spesialisasi;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static final Creator<DokterModel> CREATOR = new Creator<DokterModel>() {
        @Override
        public DokterModel createFromParcel(Parcel in) {
            return new DokterModel(in);
        }

        @Override
        public DokterModel[] newArray(int size) {
            return new DokterModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nama);
        dest.writeString(spesialisasi);
        dest.writeString(image_url);
    }
}
