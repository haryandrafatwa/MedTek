package com.example.medtek.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AppointmentModel {
    @SerializedName("id")
    private int idJanji = 0;

    @SerializedName("idPasien")
    private int idPasien = 0;

    @SerializedName("idDokter")
    private int idDokter = 0;

    @SerializedName("idTransaksi")
    private int idTransaksi = 0;

    @SerializedName("idConversation")
    private int idConversation = 0;

    @SerializedName("idReport")
    private int idReport = 0;

    @SerializedName("day_id")
    private int idDay = 0;

    @SerializedName("tglJanji")
    private String tglJanji = "";

    @SerializedName("detailJanji")
    private String detailJanji = "";

    @SerializedName("idStatus")
    private int idStatus = 0;

    @SerializedName("created_at")
    private String createdAt = "";

    @SerializedName("updated_at")
    private String updatedAt = "";

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
        private int idTransaksi = 0;

        @SerializedName("janji_id")
        private int idJanji = 0;

        @SerializedName("type_id")
        private int idType = 0;

        @SerializedName("wallet_id")
        private int idWallet = 0;

        @SerializedName("totalHarga")
        private int totalHarga = 0;

        @SerializedName("harga")
        private int harga = 0;

        @SerializedName("uniqueCode")
        private int uniqueCode = 0;

        @SerializedName("serviceFee")
        private int serviceFee = 0;

        @SerializedName("penerima")
        private String penerima = "";

        @SerializedName("rekeningPenerima")
        private String rekeningPenerima = "";

        @SerializedName("pengirim")
        private String pengirim = "";

        @SerializedName("rekeningPengirim")
        private String rekeningPengirim = "";

        @SerializedName("is_paid")
        private boolean isPaid = false;

        @SerializedName("created_at")
        private String createdAt = "";

        @SerializedName("updated_at")
        private String updatedAt = "";

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
        private int idDay = 0;

        @SerializedName("day")
        private String day = "";
    }

    public class Status {
        @SerializedName("id")
        private int idDay = 0;

        @SerializedName("status")
        private String status = "";
    }

    public class Report {
        @SerializedName("id")
        private int idDay = 0;

        @SerializedName("subjective")
        private String subjective = "";

        @SerializedName("objective")
        private String objective = "";

        @SerializedName("assessment")
        private String assessment = "";

        @SerializedName("planning")
        private String planning = "";

        @SerializedName("created_at")
        private String createdAt = "";

        @SerializedName("updated_at")
        private String updatedAt = "";
    }
}

