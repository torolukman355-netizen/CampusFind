package com.campusfind.model;

public enum KategoriBarang {
    ELEKTRONIK("Elektronik"),
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
