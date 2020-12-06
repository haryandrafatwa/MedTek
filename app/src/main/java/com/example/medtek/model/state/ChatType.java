package com.example.medtek.model.state;

import java.util.Locale;

public enum ChatType {
    TEXT,
    IMAGE,
    VIDEO,
    FILE,
    END;

    public String canonicalForm() {
        return this.name().toLowerCase(Locale.US);
    }
}
