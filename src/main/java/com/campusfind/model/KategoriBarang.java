package com.campusfind.model;

import java.io.Serializable;

public enum KategoriBarang implements Serializable {
    ELEKTRONIK("Elektronik"),
    KUNCI_KENDARAAN("Kunci & Kendaraan"),
    DOKUMEN_KARTU("Dokumen & Kartu"),
    TAS_DOMPET("Tas & Dompet"),
    PAKAIAN_AKSESORIS("Pakaian & Aksesoris"),
    ALAT_TULIS_BUKU("Alat Tulis & Buku"),
    LAINNYA("Lainnya");

    private static final long serialVersionUID = 1L;

    private final String label;

    KategoriBarang(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
