package model.enums;

/**
 * US-003: Enum Hak Akses Sistem CampusFind
 * Digunakan untuk kontrol hak akses role (Petugas, Mahasiswa, Admin).
 */
public enum HakAkses {
    // Hak Akses Mahasiswa & Umum
    BUAT_LAPORAN,
    KLAIM_BARANG,
    LIHAT_LAPORAN_SENDIRI,

    // Hak Akses Petugas (Bintang, Jepin, Fakhri)
    VERIFIKASI_LAPORAN,     // US-011
    KONTROL_STATUS,         // US-012
    KELOLA_LOKASI_SIMPAN,   // US-013
    KONFIRMASI_MATCHING,    // US-015
    KELOLA_QR_CODE,         // US-017, US-018
    KEPUTUSAN_KLAIM,        // US-020
    SERAH_TERIMA_BARANG,    // US-021

    // Hak Akses Admin (Lukman - US-003, US-022, US-023)
    LIHAT_SEMUA_BARANG,     // US-003 (Admin & Petugas)
    KELOLA_SEMUA_LAPORAN,   // US-003
    MANAJEMEN_PENGGUNA,     // US-022
    LIHAT_STATISTIK         // US-023
}
