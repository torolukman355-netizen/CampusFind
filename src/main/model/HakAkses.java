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
    }
}
