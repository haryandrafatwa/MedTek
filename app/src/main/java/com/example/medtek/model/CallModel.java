package com.example.medtek.model;

import com.google.gson.annotations.SerializedName;

public class CallModel {
    private boolean state = false;

    public CallModel(boolean state) {
        this.state = state;
    }

    public boolean getState() {
        return state;
    }
}
