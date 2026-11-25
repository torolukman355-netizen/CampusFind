package com.campusfind.model;

public enum StatusItem {
    DILAPORKAN("Dilaporkan"),
    DIVERIFIKASI("Diverifikasi"),
    COCOK("Cocok"),
    DICARI("Dicari"),
    TERSEDIA("Tersedia"),
    DALAM_PROSES_KLAIM("Dalam Proses Klaim"),
    DIKLAIM("Diklaim"),
    DIKEMBALIKAN("Dikembalikan"),
    DISERAHKAN("Diserahkan");

    private final String label;

    StatusItem(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean bolehBerubahKe(StatusItem tujuan) {
        if (tujuan == null || this == tujuan) return false;
        return switch (this) {
            case DILAPORKAN -> tujuan == DIVERIFIKASI || tujuan == DICARI || tujuan == TERSEDIA;
            case DIVERIFIKASI, DICARI, TERSEDIA -> tujuan == COCOK || tujuan == DALAM_PROSES_KLAIM || tujuan == DIKLAIM;
            case COCOK -> tujuan == DALAM_PROSES_KLAIM || tujuan == DIKLAIM;
            case DALAM_PROSES_KLAIM, DIKLAIM -> tujuan == DIKEMBALIKAN || tujuan == DISERAHKAN || tujuan == TERSEDIA;
            case DIKEMBALIKAN, DISERAHKAN -> false;
        };
    }

    public StatusItem berikutnya() {
        return switch (this) {
            case DILAPORKAN -> DIVERIFIKASI;
            case DIVERIFIKASI, DICARI, TERSEDIA -> COCOK;
            case COCOK -> DIKLAIM;
            case DALAM_PROSES_KLAIM, DIKLAIM -> DIKEMBALIKAN;
            case DIKEMBALIKAN, DISERAHKAN -> throw new IllegalStateException("Status akhir sudah tercapai dan terkunci.");
        };
    }
}