package com.example.medtek.model.dokter;

import android.os.Parcel;
import android.os.Parcelable;

public class JadwalModel implements Parcelable {

    private int id, idDay, idDokter;
    private String startHour, endHour, day;

    public JadwalModel(int id, int idDay, int idDokter, String startHour, String endHour, String day) {
        this.id = id;
        this.idDay = idDay;
        this.idDokter = idDokter;
        this.startHour = startHour;
        this.endHour = endHour;
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdDay() {
        return idDay;
    }

    public void setIdDay(int idDay) {
        this.idDay = idDay;
    }

    public int getIdDokter() {
        return idDokter;
    }

    public void setIdDokter(int idDokter) {
        this.idDokter = idDokter;
    }

    public String getStartHour() {
        return startHour;
    }

    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }

    public String getEndHour() {
        return endHour;
    }

    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    protected JadwalModel(Parcel in) {
        id = in.readInt();
        idDokter = in.readInt();
        idDay = in.readInt();
        startHour = in.readString();
        endHour = in.readString();
        day = in.readString();
    }

    public static final Creator<JadwalModel> CREATOR = new Creator<JadwalModel>() {
        @Override
        public JadwalModel createFromParcel(Parcel in) {
            return new JadwalModel(in);
        }

        @Override
        public JadwalModel[] newArray(int size) {
            return new JadwalModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(idDokter);
        dest.writeInt(idDay);
        dest.writeString(startHour);
        dest.writeString(endHour);
        dest.writeString(day);
    }
}
