package model.enums;

/**
 * US-012: Enum Status Barang dengan aturan transisi berurutan:
 * DILAPORKAN -> DIVERIFIKASI -> COCOK -> DIKLAIM -> DIKEMBALIKAN
 * Serta status penolakan DITOLAK jika verifikasi ditolak petugas.
 */
public enum StatusItem {
    DILAPORKAN("Menunggu Verifikasi"),
    DIVERIFIKASI("Terverifikasi & Masuk Smart Matching"),
    COCOK("Terkonfirmasi Cocok"),
    DIKLAIM("Klaim Disetujui"),
    DIKEMBALIKAN("Selesai Dikembalikan (Data Terkunci)"),
    DITOLAK("Laporan Ditolak");

    private final String label;

    StatusItem(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
