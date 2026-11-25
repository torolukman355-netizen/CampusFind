package com.campusfind.model;

/**
 * Klasifikasi jenis petugas di lapangan dan di pos pengamanan (Dokumen Konsep v2.0).
 */
public enum JenisPetugas {
    SECURITY("Security (Verifikator Pos)"),
    CLEANING_SERVICE("Cleaning Service (Pelapor Lapangan)");

    private final String label;

    JenisPetugas(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
