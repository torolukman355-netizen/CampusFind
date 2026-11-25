package com.campusfind.model;

import java.io.Serializable;

/**
 * Kategori barang. Hanya dibedakan menjadi barang elektronik dan
 * non-elektronik; kategori elektronik memakai subclass {@link BarangElektronik}.
 */
public enum KategoriBarang implements Serializable {
    ELEKTRONIK("Elektronik"),
    NON_ELEKTRONIK("Non-Elektronik"),
    KUNCI_KENDARAAN("Kunci & Kendaraan"),
    DOKUMEN_KARTU("Dokumen & Kartu"),
    TAS_DOMPET("Tas & Dompet"),
    PAKAIAN_AKSESORIS("Pakaian & Aksesoris"),
    ALAT_TULIS_BUKU("Alat Tulis & Buku"),
    LAINNYA("Lainnya");

    private final String label;

    KategoriBarang(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
