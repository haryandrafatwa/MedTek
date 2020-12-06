package com.example.medtek.network.response;

import com.google.gson.annotations.SerializedName;

public class JanjiResponse {
    @SerializedName("idPasien")
    private final int idPasien = 0;

    @SerializedName("idDokter")
    private final int idDokter = 0;

    @SerializedName("day_id")
    private final int idDay = 0;

    @SerializedName("idConversation")
    private final int idConversation = 0;

    @SerializedName("idStatus")
    private final int idStatus = 0;

    @SerializedName("tglJanji")
    private final String tglJanji = "";

    @SerializedName("detailJanji")
    private final String detailJanji = "";

    @SerializedName("is_started")
    private final boolean isStarted = false;

    @SerializedName("is_completed")
    private final boolean isCompleted = false;

    @SerializedName("endHour")
    private final String endHour = "";

    public int getIdPasien() {
        return idPasien;
    }

    public int getIdDokter() {
        return idDokter;
    }

    public int getIdDay() {
        return idDay;
    }

    public int getIdConversation() {
        return idConversation;
    }

    public int getIdStatus() {
        return idStatus;
    }

    public String getTglJanji() {
        return tglJanji;
    }

    public String getDetailJanji() {
        return detailJanji;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public String getEndHour() {
        return endHour;
    }
}

