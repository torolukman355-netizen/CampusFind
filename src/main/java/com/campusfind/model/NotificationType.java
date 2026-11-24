package com.campusfind.model;

/**
 * Tipe klasifikasi notifikasi sistem CampusFind.
 */
public enum NotificationType {
    KLAIM_DITERIMA("Klaim Diterima"),
    KLAIM_DISETUJUI("Klaim Disetujui"),
    KLAIM_DITOLAK("Klaim Ditolak"),
    KANDIDAT_COCOK("Kandidat Cocok");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
