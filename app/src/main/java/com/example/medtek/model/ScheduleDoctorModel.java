package com.example.medtek.model;

import java.util.ArrayList;

public class ScheduleDoctorModel {
    private String tglJanji = "";

    private ArrayList<AppointmentModel> appointmentModelList;

    public ScheduleDoctorModel(String tglJanji, ArrayList<AppointmentModel> appointmentModelList) {
        this.tglJanji = tglJanji;
        this.appointmentModelList = appointmentModelList;
    }

    public ScheduleDoctorModel(String tglJanji) {
        this.tglJanji = tglJanji;
        this.appointmentModelList = new ArrayList<>();
    }

    public String getTglJanji() {
        return tglJanji;
    }

    public ArrayList<AppointmentModel> getAppointmentModelList() {
        return appointmentModelList;
    }

    public void setAppointmentModelList(ArrayList<AppointmentModel> appointmentModelList) {
        this.appointmentModelList = appointmentModelList;
    }
}
