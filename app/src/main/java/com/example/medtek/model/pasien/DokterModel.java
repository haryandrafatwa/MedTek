package com.example.medtek.model.pasien;

import android.os.Parcel;
import android.os.Parcelable;

public class DokterModel implements Parcelable {

    private String nama, email, spesialisasi, nama_rs, alamat_rs,image_url;
    private int id, harga, isVerified,lamakerja;
    private float rating;

    protected DokterModel(Parcel in) {
        id = in.readInt();
        nama = in.readString();
        email = in.readString();
        spesialisasi = in.readString();
        nama_rs = in.readString();
        alamat_rs = in.readString();
        image_url = in.readString();
        harga = in.readInt();
        rating = in.readFloat();
        isVerified = in.readInt();
        lamakerja = in.readInt();
    }

    public DokterModel(int id, String nama, String email, String spesialisasi, String nama_rs, String alamat_rs, String image_url, int harga, float rating, int isVerified,int lamakerja) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.spesialisasi = spesialisasi;
        this.nama_rs = nama_rs;
        this.alamat_rs = alamat_rs;
        this.image_url = image_url;
        this.harga = harga;
        this.rating = rating;
        this.isVerified = isVerified;
        this.lamakerja = lamakerja;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpesialisasi() {
        return spesialisasi;
    }

    public void setSpesialisasi(String spesialisasi) {
        this.spesialisasi = spesialisasi;
    }

    public String getNama_rs() {
        return nama_rs;
    }

    public void setNama_rs(String nama_rs) {
        this.nama_rs = nama_rs;
    }

    public String getAlamat_rs() {
        return alamat_rs;
    }

    public void setAlamat_rs(String alamat_rs) {
        this.alamat_rs = alamat_rs;
    }

    public int getHarga() {
        return harga;
    }

    public void setHarga(int harga) {
        this.harga = harga;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getLamakerja() {
        return lamakerja;
    }

    public void setLamakerja(int lamakerja) {
        this.lamakerja = lamakerja;
    }

    public int getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(int isVerified) {
        this.isVerified = isVerified;
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
        dest.writeString(email);
        dest.writeString(spesialisasi);
        dest.writeString(nama_rs);
        dest.writeString(alamat_rs);
        dest.writeString(image_url);
        dest.writeInt(harga);
        dest.writeFloat(rating);
        dest.writeInt(isVerified);
        dest.writeInt(lamakerja);
    }
}
