package com.campusfind.model;

import java.io.Serializable;

public enum StatusKlaim implements Serializable {
    DIAJUKAN("Diajukan"),
    DISETUJUI("Disetujui"),
    DITOLAK("Ditolak"),
    DIBATALKAN("Dibatalkan"),
    SELESAI("Selesai"),

    /**
     * @deprecated Dipertahankan semata-mata untuk kompatibilitas deserialisasi data versi lama (schemaVersion 1).
     * Dimigrasikan otomatis menjadi {@link #DIAJUKAN} oleh FileDataStore.
     */
    @Deprecated
    MENUNGGU_VERIFIKASI("Diajukan");

    private static final long serialVersionUID = 1L;

    private final String label;

    StatusKlaim(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
