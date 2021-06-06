package com.example.medtek.model.pasien;

import android.os.Parcel;
import android.os.Parcelable;

public class RiwayatModel implements Parcelable {

    private int id, idType;
    private long harga;
    private String date, nama, spesialis, detailJanji, imagePath;

    public RiwayatModel(int id, int idType, long harga, String date, String nama, String spesialis, String detailJanji, String imagePath) {
        this.id = id;
        this.idType = idType;
        this.harga = harga;
        this.date = date;
        this.nama = nama;
        this.spesialis = spesialis;
        this.detailJanji = detailJanji;
        this.imagePath = imagePath;
    }

    protected RiwayatModel(Parcel in) {
        id = in.readInt();
        idType = in.readInt();
        harga = in.readLong();
        date = in.readString();
        nama = in.readString();
        spesialis = in.readString();
        detailJanji = in.readString();
        imagePath = in.readString();
    }

    public static final Creator<RiwayatModel> CREATOR = new Creator<RiwayatModel>() {
        @Override
        public RiwayatModel createFromParcel(Parcel in) {
            return new RiwayatModel(in);
        }

        @Override
        public RiwayatModel[] newArray(int size) {
            return new RiwayatModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(idType);
        dest.writeLong(harga);
        dest.writeString(date);
        dest.writeString(nama);
        dest.writeString(spesialis);
        dest.writeString(detailJanji);
        dest.writeString(imagePath);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdType() {
        return idType;
    }

    public void setIdType(int idType) {
        this.idType = idType;
    }

    public long getHarga() {
        return harga;
    }

    public void setHarga(long harga) {
        this.harga = harga;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getSpesialis() {
        return spesialis;
    }

    public void setSpesialis(String spesialis) {
        this.spesialis = spesialis;
    }

    public String getDetailJanji() {
        return detailJanji;
    }

    public void setDetailJanji(String detailJanji) {
        this.detailJanji = detailJanji;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
