package com.campusfind.model;

/**
 * Interface Pelapor (Dokumen Konsep v2.0).
 * Menjamin seluruh warga kampus (Mahasiswa, Petugas/Security/CS, Admin) dapat membuat laporan barang.
 */
public interface Pelapor {
    void buatLaporan(Laporan laporan);
}
