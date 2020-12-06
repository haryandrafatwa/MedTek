package com.example.medtek.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AppointmentModel {
    @SerializedName("id")
    private final int idJanji = 0;

    @SerializedName("idPasien")
    private final int idPasien = 0;

    @SerializedName("idDokter")
    private final int idDokter = 0;

    @SerializedName("idTransaksi")
    private final int idTransaksi = 0;

    @SerializedName("idConversation")
    private final int idConversation = 0;

    @SerializedName("idReport")
    private final int idReport = 0;

    @SerializedName("day_id")
    private final int idDay = 0;

    @SerializedName("tglJanji")
    private final String tglJanji = "";

    @SerializedName("detailJanji")
    private final String detailJanji = "";

    @SerializedName("idStatus")
    private int idStatus = 0;

    @SerializedName("created_at")
    private final String createdAt = "";

    @SerializedName("updated_at")
    private final String updatedAt = "";

    @SerializedName("transaksi")
    private ArrayList<Transaksi> transaksi;

    @SerializedName("dokter")
    private UserModel dokter;

    @SerializedName("pasien")
    private UserModel pasien;

    @SerializedName("day")
    private Day dayJanji;

    @SerializedName("status")
    private Status status;

    @SerializedName("report")
    private Report report;

    private ImageModel imageModel = null;

    private UserModel.Jadwal jadwal = null;

    public int getIdJanji() {
        return idJanji;
    }

    public int getIdPasien() {
        return idPasien;
    }

    public int getIdDokter() {
        return idDokter;
    }

    public int getIdTransaksi() {
        return idTransaksi;
    }

    public int getIdConversation() {
        return idConversation;
    }

    public int getIdDay() {
        return idDay;
    }

    public String getTglJanji() {
        return tglJanji;
    }

    public String getDetailJanji() {
        return detailJanji;
    }

    public int getIdStatus() {
        return idStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public ArrayList<Transaksi> getTransaksi() {
        return transaksi;
    }

    public UserModel getDokter() {
        return dokter;
    }

    public UserModel getPasien() {
        return pasien;
    }

    public Day getDayJanji() {
        return dayJanji;
    }

    public Status getStatus() {
        return status;
    }

    public ImageModel getImageModel() {
        return imageModel;
    }

    public void setImageModel(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    public UserModel.Jadwal getJadwal() {
        return jadwal;
    }

    public void setJadwal(UserModel.Jadwal jadwal) {
        this.jadwal = jadwal;
    }

    public void setIdStatus(int idStatus) {
        this.idStatus = idStatus;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public class Transaksi {
        @SerializedName("id")
        private final int idTransaksi = 0;

        @SerializedName("janji_id")
        private final int idJanji = 0;

        @SerializedName("type_id")
        private final int idType = 0;

        @SerializedName("wallet_id")
        private final int idWallet = 0;

        @SerializedName("totalHarga")
        private final int totalHarga = 0;

        @SerializedName("harga")
        private final int harga = 0;

        @SerializedName("uniqueCode")
        private final int uniqueCode = 0;

        @SerializedName("serviceFee")
        private final int serviceFee = 0;

        @SerializedName("penerima")
        private final String penerima = "";

        @SerializedName("rekeningPenerima")
        private final String rekeningPenerima = "";

        @SerializedName("pengirim")
        private final String pengirim = "";

        @SerializedName("rekeningPengirim")
        private final String rekeningPengirim = "";

        @SerializedName("is_paid")
        private final boolean isPaid = false;

        @SerializedName("created_at")
        private final String createdAt = "";

        @SerializedName("updated_at")
        private final String updatedAt = "";

        public int getIdTransaksi() {
            return idTransaksi;
        }

        public int getIdJanji() {
            return idJanji;
        }

        public int getIdType() {
            return idType;
        }

        public int getIdWallet() {
            return idWallet;
        }

        public int getTotalHarga() {
            return totalHarga;
        }

        public int getHarga() {
            return harga;
        }

        public int getUniqueCode() {
            return uniqueCode;
        }

        public int getServiceFee() {
            return serviceFee;
        }

        public String getPenerima() {
            return penerima;
        }

        public String getRekeningPenerima() {
            return rekeningPenerima;
        }

        public String getPengirim() {
            return pengirim;
        }

        public String getRekeningPengirim() {
            return rekeningPengirim;
        }

        public boolean isPaid() {
            return isPaid;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }

    public class Day {
        @SerializedName("id")
        private final int idDay = 0;

        @SerializedName("day")
        private final String day = "";
    }

    public class Status {
        @SerializedName("id")
        private final int idDay = 0;

        @SerializedName("status")
        private final String status = "";
    }

    public class Report {
        @SerializedName("id")
        private final int idDay = 0;

        @SerializedName("subjective")
        private final String subjective = "";

        @SerializedName("objective")
        private final String objective = "";

        @SerializedName("assessment")
        private final String assessment = "";

        @SerializedName("planning")
        private final String planning = "";

        @SerializedName("created_at")
        private final String createdAt = "";

        @SerializedName("updated_at")
        private final String updatedAt = "";
    }
}

