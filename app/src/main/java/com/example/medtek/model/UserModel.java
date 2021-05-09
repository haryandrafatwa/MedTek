package com.example.medtek.model;

import com.example.medtek.network.base.BaseResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UserModel extends BaseResponse {
    @SerializedName("id")
    private int idUser = 0;

    @SerializedName("name")
    private String name = "";

    @SerializedName("email")
    private String email = "";

    @SerializedName("email_verified_at")
    private String emailVerifiedAt = "";

    @SerializedName("hospital_id")
    private int hospitalId = 0;

    @SerializedName("alamat")
    private Alamat alamat;

    @SerializedName("alamat_id")
    private int alamatId;

    @SerializedName("tglLahir")
    private String tglLahir = "";

    @SerializedName("notelp")
    private String noTelp = "";

    @SerializedName("jenis_kelamin")
    private String jenisKelamin = "";

    @SerializedName("harga")
    private int harga = 0;

    @SerializedName("berat_badan")
    private int beratBadan = 0;

    @SerializedName("tinggi_badan")
    private int tinggiBadan = 0;

    @SerializedName("lingkar_badan")
    private int lingkarBadan = 0;

    @SerializedName("rating")
    private String rating = "";

    @SerializedName("lulusan")
    private String lulusan = "";

    @SerializedName("lama_kerja")
    private String lamaKerja = "";

    @SerializedName("created_at")
    private String createdAt = "";

    @SerializedName("updated_at")
    private String updatedAt = "";

    @SerializedName("role_id")
    private int roleId;

    @SerializedName("role")
    private Role role;

    @SerializedName("specialization_id")
    private int specializationId;

    @SerializedName("specialization")
    private Specialization specialization;

    @SerializedName("hospital")
    private Hospital hospital;

    @SerializedName("image")
    private List<ImageModel> image;

    @SerializedName("jadwal")
    private List<Jadwal> jadwal;

    public UserModel(int idUser, String name, String email, String emailVerifiedAt, int hospitalId, Alamat alamat, int alamatId, String tglLahir, String noTelp, String jenisKelamin, int harga, String rating, String lulusan, String lamaKerja, String createdAt, String updatedAt, int roleId, Role role, int specializationId, Specialization specialization, Hospital hospital, List<ImageModel> image, List<Jadwal> jadwal) {
        this.idUser = idUser;
        this.name = name;
        this.email = email;
        this.emailVerifiedAt = emailVerifiedAt;
        this.hospitalId = hospitalId;
        this.alamat = alamat;
        this.alamatId = alamatId;
        this.tglLahir = tglLahir;
        this.noTelp = noTelp;
        this.jenisKelamin = jenisKelamin;
        this.harga = harga;
        this.rating = rating;
        this.lulusan = lulusan;
        this.lamaKerja = lamaKerja;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roleId = roleId;
        this.role = role;
        this.specializationId = specializationId;
        this.specialization = specialization;
        this.hospital = hospital;
        this.image = image;
        this.jadwal = jadwal;
    }

    public UserModel(int idUser, String name, List<ImageModel> image) {
        this.idUser = idUser;
        this.name = name;
        this.image = image;
    }

    public UserModel() {
    }

    public int getIdUser() {
        return idUser;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public int getHospitalId() {
        return hospitalId;
    }

    public Alamat getAlamat() {
        return alamat;
    }

    public String getTglLahir() {
        return tglLahir;
    }

    public String getNoTelp() {
        return noTelp;
    }

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public int getHarga() {
        return harga;
    }

    public String getRating() {
        return rating;
    }

    public String getLulusan() {
        return lulusan;
    }

    public String getLamaKerja() {
        return lamaKerja;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Role getRole() {
        return role;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public List<ImageModel> getImage() {
        return image;
    }

    public List<Jadwal> getJadwal() {
        return jadwal;
    }

    public void setJadwal(List<Jadwal> jadwal) {
        this.jadwal = jadwal;
    }

    public static class Role {
        @SerializedName("id")
        private int id = 0;

        @SerializedName("role")
        private String role = "";

        public Role(int id, String role) {
            this.id = id;
            this.role = role;
        }

        public Role() {
        }

        public int getId() {
            return id;
        }

        public String getRole() {
            return role;
        }
    }

    public static class Specialization {
        @SerializedName("id")
        private int id = 0;

        @SerializedName("specialization")
        private String specialization = "";

        public Specialization(int id, String specialization) {
            this.id = id;
            this.specialization = specialization;
        }

        public Specialization() {
        }

        public int getId() {
            return id;
        }

        public String getSpecialization() {
            return specialization;
        }
    }

    public static class Hospital {
        @SerializedName("id")
        private int idHospital = 0;

        @SerializedName("name")
        private String nameHospital = "";

        @SerializedName("alamat_id")
        private int idAlamat = 0;

        @SerializedName("alamat")
        private String addressHospital = "";

        @SerializedName("notelp")
        private String noTelpHospital = "";

        @SerializedName("info")
        private String infoHospital = "";
    }

    private class Alamat {
        @SerializedName("id")
        private int idAlamat = 0;

        @SerializedName("jalan")
        private String jalan = "";

        @SerializedName("nomor_bangunan")
        private String nomorBangunan = "";

        @SerializedName("rtrw")
        private String rtRw = "";

        @SerializedName("kelurahan")
        private String kelurahan = "";

        @SerializedName("kecamatan")
        private String kecamatan = "";

        @SerializedName("kota")
        private String kota = "";

        @SerializedName("lat")
        private String latitude = "";

        @SerializedName("long")
        private String longitude = "";

        @SerializedName("created_at")
        private String createdAt = "";

        @SerializedName("updated_at")
        private String updatedAt = "";
    }

    public class Jadwal {
        @SerializedName("id")
        private int idJadwal = 0;

        @SerializedName("idDokter")
        private int idDokter = 0;

        @SerializedName("day_id")
        private int idDay = 0;

        @SerializedName("startHour")
        private String startHour = "";

        @SerializedName("endHour")
        private String endHour = "";

        @SerializedName("created_at")
        private String createdAt = "";

        @SerializedName("updated_at")
        private String updatedAt = "";

        @SerializedName("day")
        private AppointmentModel.Day day;

        public int getIdJadwal() {
            return idJadwal;
        }

        public int getIdDokter() {
            return idDokter;
        }

        public int getIdDay() {
            return idDay;
        }

        public String getStartHour() {
            return startHour;
        }

        public String getEndHour() {
            return endHour;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public AppointmentModel.Day getDay() {
            return day;
        }
    }

    public class Wallet {
        @SerializedName("id")
        private int idJadwal = 0;

        @SerializedName("idUser")
        private int idUser = 0;

        @SerializedName("balance")
        private int balance = 0;

        @SerializedName("created_at")
        private String createdAt = "";

        @SerializedName("updated_at")
        private String updatedAt = "";
    }
}

