package com.example.medtek.Dokter.Home;

import android.os.Parcel;
import android.os.Parcelable;

public class JanjiModel implements Parcelable {

    private int id, idPasien, idDokter, idConversation, idReport, idDay, idStatus;
    private String tglJanji, detailJanji, filePath;

    public JanjiModel(int id, int idPasien, int idDokter, int idConversation, int idReport, int idDay, int idStatus, String tglJanji, String detailJanji, String filePath) {
        this.id = id;
        this.idPasien = idPasien;
        this.idDokter = idDokter;
        this.idConversation = idConversation;
        this.idReport = idReport;
        this.idDay = idDay;
        this.idStatus = idStatus;
        this.tglJanji = tglJanji;
        this.detailJanji = detailJanji;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPasien() {
        return idPasien;
    }

    public void setIdPasien(int idPasien) {
        this.idPasien = idPasien;
    }

    public int getIdDokter() {
        return idDokter;
    }

    public void setIdDokter(int idDokter) {
        this.idDokter = idDokter;
    }

    public int getIdConversation() {
        return idConversation;
    }

    public void setIdConversation(int idConversation) {
        this.idConversation = idConversation;
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public int getIdDay() {
        return idDay;
    }

    public void setIdDay(int idDay) {
        this.idDay = idDay;
    }

    public int getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(int idStatus) {
        this.idStatus = idStatus;
    }

    public String getTglJanji() {
        return tglJanji;
    }

    public void setTglJanji(String tglJanji) {
        this.tglJanji = tglJanji;
    }

    public String getDetailJanji() {
        return detailJanji;
    }

    public void setDetailJanji(String detailJanji) {
        this.detailJanji = detailJanji;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    protected JanjiModel(Parcel in) {
        id = in.readInt();
        idPasien = in.readInt();
        idDokter = in.readInt();
        idConversation = in.readInt();
        idReport = in.readInt();
        idDay = in.readInt();
        idStatus = in.readInt();
        tglJanji = in.readString();
        detailJanji = in.readString();
        filePath = in.readString();
    }

    public static final Creator<JanjiModel> CREATOR = new Creator<JanjiModel>() {
        @Override
        public JanjiModel createFromParcel(Parcel in) {
            return new JanjiModel(in);
        }

        @Override
        public JanjiModel[] newArray(int size) {
            return new JanjiModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(idPasien);
        dest.writeInt(idDokter);
        dest.writeInt(idConversation);
        dest.writeInt(idReport);
        dest.writeInt(idDay);
        dest.writeInt(idStatus);
        dest.writeString(tglJanji);
        dest.writeString(detailJanji);
        dest.writeString(filePath);
    }
}
