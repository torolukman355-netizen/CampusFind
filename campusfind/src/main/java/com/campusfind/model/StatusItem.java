package com.campusfind.model;

import java.io.Serializable;

public enum StatusItem implements Serializable {
    DICARI("Dicari"),
    TERSEDIA("Tersedia"),
    DALAM_PROSES_KLAIM("Dalam Proses Klaim"),
    DISERAHKAN("Diserahkan");

    private static final long serialVersionUID = 1L;

    private final String label;

    StatusItem(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
