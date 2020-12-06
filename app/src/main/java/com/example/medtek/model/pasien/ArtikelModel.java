package com.example.medtek.model.pasien;

import android.os.Parcel;
import android.os.Parcelable;

public class ArtikelModel implements Parcelable {
    private int id_artikel;
    private String judul, isi, slug, image_url, upload_date,author,topic;

    public ArtikelModel(int id_artikel, String judul, String isi, String slug, String image_url, String upload_date, String author, String topic) {
        this.id_artikel = id_artikel;
        this.judul = judul;
        this.isi = isi;
        this.slug = slug;
        this.image_url = image_url;
        this.upload_date = upload_date;
        this.author = author;
        this.topic = topic;
    }

    protected ArtikelModel(Parcel in) {
        id_artikel = in.readInt();
        judul = in.readString();
        isi = in.readString();
        slug = in.readString();
        image_url = in.readString();
        upload_date = in.readString();
        author = in.readString();
        topic = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ArtikelModel createFromParcel(Parcel in) {
            return new ArtikelModel(in);
        }

        public ArtikelModel[] newArray(int size) {
            return new ArtikelModel[size];
        }
    };

    public int getId_artikel() {
        return id_artikel;
    }

    public void setId_artikel(int id_artikel) {
        this.id_artikel = id_artikel;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getIsi() {
        return isi;
    }

    public void setIsi(String isi) {
        this.isi = isi;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(String upload_date) {
        this.upload_date = upload_date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.judul);
        dest.writeString(this.isi);
        dest.writeString(this.slug);
        dest.writeString(this.image_url);
        dest.writeInt(this.id_artikel);
        dest.writeString(this.author);
        dest.writeString(this.upload_date);
        dest.writeString(this.topic);
    }
}
