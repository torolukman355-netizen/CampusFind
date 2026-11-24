package com.campusfind.model;


/**
 * US-001, US-002, US-003:
 * Enum Hak Akses Pengguna CampusFind.
 * Digunakan untuk mengontrol otorisasi akses role Mahasiswa, Petugas (Bintang, Jepin, Fakhri), dan Admin (Lukman).
 */
public enum HakAkses {
    // Hak Akses Role Mahasiswa (Pitya, Nadia)
    BUAT_LAPORAN("Buat Laporan Barang Hilang / Temuan"),
    KLAIM_BARANG("Pengajuan Klaim Kepemilikan Barang"),
    LIHAT_LAPORAN_SENDIRI("Melihat Riwayat Laporan Milik Sendiri"),

    // Hak Akses Role Petugas (Bintang, Jepin, Fakhri)
    VERIFIKASI_LAPORAN("Verifikasi Laporan Masuk (US-011)"),
    KONTROL_STATUS("Kontrol Transisi Status Berurutan (US-012)"),
    KELOLA_LOKASI_SIMPAN("Manajemen Lokasi Penyimpanan Fisik Temuan (US-013)"),
    KONFIRMASI_MATCHING("Konfirmasi Hasil Smart Matching (US-015)"),
    KELOLA_QR_CODE("Cetak, Simpan, dan Pelacakan QR Code (US-017, US-018)"),
    KEPUTUSAN_KLAIM("Pemeriksaan dan Keputusan Klaim Mahasiswa (US-020)"),
    SERAH_TERIMA_BARANG("Pencatatan Serah Terima dan Penguncian Data (US-021)"),

    // Hak Akses Role Admin (Lukman)
    LIHAT_SEMUA_BARANG("Lihat Seluruh Data Barang (US-003, US-007)"),
    KELOLA_SEMUA_LAPORAN("Pengelolaan Semua Laporan Sistem"),
    MANAJEMEN_PENGGUNA("Manajemen Akun dan Hak Akses Pengguna (US-022)"),
    LIHAT_STATISTIK("Layanan & Visualisasi Statistik Sistem (US-023, US-031)");

    private final String deskripsi;

    HakAkses(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}
