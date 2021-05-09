package com.example.medtek.network.request;

import com.google.gson.annotations.SerializedName;

/**
 * Model untuk request buatJanji
 */

public class JanjiRequest {

    @SerializedName("idDokter")
    private int id = 0;

    @SerializedName("tglJanji")
    private String tglJanji = "";

    @SerializedName("detailJanji")
    private String detailJanji = "";

    @SerializedName("day")
    private String day = "";

    public JanjiRequest(int id, String tglJanji, String detailJanji, String day) {
        this.id = id;
        this.tglJanji = tglJanji;
        this.detailJanji = detailJanji;
        this.day = day;
    }
}
