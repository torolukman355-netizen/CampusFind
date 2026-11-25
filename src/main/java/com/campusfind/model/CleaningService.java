package com.campusfind.model;

/**
 * Subclass CleaningService (Dokumen Konsep v2.0).
 * Ujung tombak penemu barang di lapangan; berhak membuat laporan temuan (Pelapor)
 * dan menyerahkan fisik barang ke Pos Security.
 * Tidak memiliki hak verifikasi klaim (prinsip hak akses minimum / least privilege).
 */
public class CleaningService extends Petugas {

    private static final long serialVersionUID = 1L;

    public CleaningService(String nama, String nip) {
        super(nama, nip, JenisPetugas.CLEANING_SERVICE);
    }

    public void serahkanBarangKePos(Laporan laporan) {
        if (laporan == null) {
            throw new IllegalArgumentException("Laporan tidak boleh null.");
        }
        if (laporan.getJenis() != JenisLaporan.TEMUAN) {
            throw new IllegalStateException("Hanya barang TEMUAN yang diserahkan ke pos Security.");
        }
        laporan.setDiserahkanKePos(true);
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Cleaning Service: Buat Laporan Temuan Lapangan & Serahkan Barang ke Pos Security.";
    }
}
