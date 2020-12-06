package com.example.medtek.model.pasien;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class HospitalModel implements Parcelable {

    private String name, no_telp, jalan, no_bangunan, rtrw, kelurahan, kecamatan, kota, provinsi, generalInformation, jenis, imageURL;
    private int id;
    private double distance;
    private List<DokterModel> listDokter;

    public HospitalModel(String name, String no_telp, String jalan, String no_bangunan, String rtrw, String kelurahan, String kecamatan, String kota, String provinsi, String generalInformation, String jenis, String imageURL, int id, double distance, List<DokterModel> listDokter) {
        this.name = name;
        this.no_telp = no_telp;
        this.jalan = jalan;
        this.no_bangunan = no_bangunan;
        this.rtrw = rtrw;
        this.kelurahan = kelurahan;
        this.kecamatan = kecamatan;
        this.kota = kota;
        this.provinsi = provinsi;
        this.generalInformation = generalInformation;
        this.imageURL = imageURL;
        this.jenis = jenis;
        this.id = id;
        this.distance = distance;
        this.listDokter = listDokter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNo_telp() {
        return no_telp;
    }

    public void setNo_telp(String no_telp) {
        this.no_telp = no_telp;
    }

    public String getJalan() {
        return jalan;
    }

    public void setJalan(String jalan) {
        this.jalan = jalan;
    }

    public String getNo_bangunan() {
        return no_bangunan;
    }

    public void setNo_bangunan(String no_bangunan) {
        this.no_bangunan = no_bangunan;
    }

    public String getRtrw() {
        return rtrw;
    }

    public void setRtrw(String rtrw) {
        this.rtrw = rtrw;
    }

    public String getKelurahan() {
        return kelurahan;
    }

    public void setKelurahan(String kelurahan) {
        this.kelurahan = kelurahan;
    }

    public String getKecamatan() {
        return kecamatan;
    }

    public void setKecamatan(String kecamatan) {
        this.kecamatan = kecamatan;
    }

    public String getKota() {
        return kota;
    }

    public void setKota(String kota) {
        this.kota = kota;
    }

    public String getProvinsi() {
        return provinsi;
    }

    public void setProvinsi(String provinsi) {
        this.provinsi = provinsi;
    }

    public String getGeneralInformation() {
        return generalInformation;
    }

    public void setGeneralInformation(String generalInformation) {
        this.generalInformation = generalInformation;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public List<DokterModel> getListDokter() {
        return listDokter;
    }

    public void setListDokter(List<DokterModel> listDokter) {
        this.listDokter = listDokter;
    }

    protected HospitalModel(Parcel in) {
        name = in.readString();
        no_telp = in.readString();
        jalan = in.readString();
        no_bangunan = in.readString();
        rtrw = in.readString();
        kelurahan = in.readString();
        kecamatan = in.readString();
        kota = in.readString();
        provinsi = in.readString();
        generalInformation = in.readString();
        imageURL = in.readString();
        jenis = in.readString();
        id = in.readInt();
        distance = in.readDouble();
        listDokter = in.createTypedArrayList(DokterModel.CREATOR);
    }

    public static final Creator<HospitalModel> CREATOR = new Creator<HospitalModel>() {
        @Override
        public HospitalModel createFromParcel(Parcel in) {
            return new HospitalModel(in);
        }

        @Override
        public HospitalModel[] newArray(int size) {
            return new HospitalModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(no_telp);
        dest.writeString(jalan);
        dest.writeString(no_bangunan);
        dest.writeString(rtrw);
        dest.writeString(kelurahan);
        dest.writeString(kecamatan);
        dest.writeString(kota);
        dest.writeString(provinsi);
        dest.writeString(generalInformation);
        dest.writeString(jenis);
        dest.writeString(imageURL);
        dest.writeInt(id);
        dest.writeDouble(distance);
        dest.writeTypedList(listDokter);
    }
}
