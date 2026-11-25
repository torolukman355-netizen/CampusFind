package com.campusfind.model;

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
    KLAIM_BARANG("Pengajuan Klaim Kepemilikan Barang"),
    LIHAT_KLAIM_SENDIRI("Melihat klaim milik sendiri"),
    LIHAT_LAPORAN_SENDIRI("Melihat Riwayat Laporan Milik Sendiri"),

    // ---- Hak akses Petugas ----
    VERIFIKASI_LAPORAN("Memverifikasi laporan yang masuk"),
    KELOLA_STATUS_ITEM("Mengubah status barang"),
    KONTROL_STATUS("Kontrol Transisi Status Berurutan (US-012)"),
    KELOLA_LOKASI_SIMPAN("Mengelola lokasi penyimpanan barang"),
    KONFIRMASI_KANDIDAT("Mengonfirmasi kandidat barang cocok"),
    KONFIRMASI_MATCHING("Konfirmasi Hasil Smart Matching (US-015)"),
    KELOLA_QR_CODE("Membuat dan membaca QR code barang"),
    PUTUSKAN_KLAIM("Menyetujui atau menolak klaim"),
    KEPUTUSAN_KLAIM("Pemeriksaan dan Keputusan Klaim Mahasiswa (US-020)"),
    SERAH_TERIMA("Mencatat serah terima barang"),
    SERAH_TERIMA_BARANG("Pencatatan Serah Terima dan Penguncian Data (US-021)"),
    LIHAT_SEMUA_KLAIM("Melihat seluruh klaim"),

    // ---- Hak akses Admin ----
    LIHAT_SEMUA_BARANG("Lihat Seluruh Data Barang (US-003, US-007)"),
    KELOLA_SEMUA_LAPORAN("Pengelolaan Semua Laporan Sistem"),
    MANAJEMEN_PENGGUNA("Manajemen Akun dan Hak Akses Pengguna (US-022)"),
    LIHAT_STATISTIK("Melihat statistik layanan");

    private final String label;

    HakAkses(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getDeskripsi() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
