package com.campusfind.model;

public enum StatusItem {
    DILAPORKAN("Dilaporkan"),
    DIVERIFIKASI("Diverifikasi"),
    DITOLAK("Ditolak"),
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
            case DILAPORKAN -> tujuan == DIVERIFIKASI || tujuan == DITOLAK;
            case DIVERIFIKASI, DICARI, TERSEDIA -> tujuan == COCOK || tujuan == DIKLAIM || tujuan == DALAM_PROSES_KLAIM;
            case COCOK -> tujuan == DIKLAIM || tujuan == DALAM_PROSES_KLAIM;
            case DALAM_PROSES_KLAIM, DIKLAIM -> tujuan == DIKEMBALIKAN || tujuan == DISERAHKAN || tujuan == TERSEDIA;
            case DITOLAK, DIKEMBALIKAN, DISERAHKAN -> false;
        };
    }

    public StatusItem berikutnya() {
        return switch (this) {
            case DILAPORKAN -> DIVERIFIKASI;
            case DIVERIFIKASI, DICARI, TERSEDIA -> COCOK;
            case COCOK -> DIKLAIM;
            case DALAM_PROSES_KLAIM, DIKLAIM -> DIKEMBALIKAN;
            case DITOLAK, DIKEMBALIKAN, DISERAHKAN -> throw new IllegalStateException("Status akhir sudah tercapai dan terkunci.");
        };
    }
}