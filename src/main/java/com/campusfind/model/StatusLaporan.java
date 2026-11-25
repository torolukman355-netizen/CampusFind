package com.campusfind.model;

/**
 * Status siklus hidup laporan kejadian barang hilang/temuan (Dokumen Konsep v2.0).
 */
public enum StatusLaporan {
    DILAPORKAN("Dilaporkan"),
    DIVERIFIKASI("Diverifikasi"),
    COCOK("Cocok"),
    DALAM_PROSES_KLAIM("Dalam Proses Klaim"),
    DIKEMBALIKAN("Dikembalikan"),
    DITOLAK("Ditolak"),
    DIARSIPKAN("Diarsipkan");

    private final String label;

    StatusLaporan(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
