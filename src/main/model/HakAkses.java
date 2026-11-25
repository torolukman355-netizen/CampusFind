package com.campusfind.model;

<<<<<<< HEAD:src/main/model/HakAkses.java
/**
 * Daftar hak akses (permission) yang dapat dimiliki sebuah {@link Role}.
 * <p>
 * Hak akses dipakai untuk mengontrol fitur apa saja yang boleh dibuka
 * oleh pengguna yang sedang login, tanpa perlu memeriksa {@code instanceof}.
 */
public enum HakAkses {

    // ---- Hak akses umum (semua role) ----
    LIHAT_TEMUAN("Melihat katalog barang temuan"),
    LIHAT_NOTIFIKASI("Melihat notifikasi pribadi"),

    // ---- Hak akses Mahasiswa ----
    BUAT_LAPORAN("Membuat laporan barang hilang/temuan"),
    AJUKAN_KLAIM("Mengajukan klaim barang temuan"),
    LIHAT_KLAIM_SENDIRI("Melihat klaim milik sendiri"),

    // ---- Hak akses Petugas ----
    VERIFIKASI_LAPORAN("Memverifikasi laporan yang masuk"),
    KELOLA_STATUS_ITEM("Mengubah status barang"),
    KELOLA_LOKASI_SIMPAN("Mengelola lokasi penyimpanan barang"),
    KONFIRMASI_KANDIDAT("Mengonfirmasi kandidat barang cocok"),
    KELOLA_QR_CODE("Membuat dan membaca QR code barang"),
    PUTUSKAN_KLAIM("Menyetujui atau menolak klaim"),
    SERAH_TERIMA("Mencatat serah terima barang"),
    LIHAT_SEMUA_KLAIM("Melihat seluruh klaim"),

    // ---- Hak akses Admin ----
    LIHAT_STATISTIK("Melihat statistik layanan");

    private final String label;

    HakAkses(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
=======

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
>>>>>>> ca2e27ff81bd0eaf0f0496ef3f356f4006abcbb8:src/main/java/com/campusfind/model/HakAkses.java
    }
}
