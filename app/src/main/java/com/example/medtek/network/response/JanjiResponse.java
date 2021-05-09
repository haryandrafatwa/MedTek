package com.example.medtek.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * Model untuk collect data response Janji socket
 */

public class JanjiResponse {
    @SerializedName("idPasien")
    private int idPasien = 0;

    @SerializedName("idDokter")
    private int idDokter = 0;

    @SerializedName("day_id")
    private int idDay = 0;

    @SerializedName("idConversation")
    private int idConversation = 0;

    @SerializedName("idStatus")
    private int idStatus = 0;

    @SerializedName("tglJanji")
    private String tglJanji = "";

    @SerializedName("detailJanji")
    private String detailJanji = "";

    @SerializedName("is_started")
    private boolean isStarted = false;

    @SerializedName("is_completed")
    private boolean isCompleted = false;

    @SerializedName("endHour")
    private String endHour = "";

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

