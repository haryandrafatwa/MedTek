package com.example.medtek.Pasien.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class TransactionModel implements Parcelable {

    private int id, idType;
    private long totalHarga;
    private String date;

    public TransactionModel(int id, int idType, long totalHarga, String date) {
        this.id = id;
        this.idType = idType;
        this.totalHarga = totalHarga;
        this.date = date;
    }

    protected TransactionModel(Parcel in) {
        id = in.readInt();
        idType = in.readInt();
        totalHarga = in.readLong();
        date = in.readString();
    }

    public static final Creator<TransactionModel> CREATOR = new Creator<TransactionModel>() {
        @Override
        public TransactionModel createFromParcel(Parcel in) {
            return new TransactionModel(in);
        }

        @Override
        public TransactionModel[] newArray(int size) {
            return new TransactionModel[size];
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
        dest.writeLong(totalHarga);
        dest.writeString(date);
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

    public long getTotalHarga() {
        return totalHarga;
    }

    public void setTotalHarga(long totalHarga) {
        this.totalHarga = totalHarga;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
