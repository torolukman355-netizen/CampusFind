package com.campusfind.model;

/**
 * Role Petugas Keamanan / Pos Kampus.
 * Memiliki hak akses penuh atas operasional pos, verifikasi, lokasi simpan, matching, QR, klaim, dan serah terima.
 */
public class Petugas extends User {

    private static final long serialVersionUID = 1L;

    public Petugas(String nama, String nip) {
        super(nama, nip);
        isiHakAksesPetugas();
    }

    public Petugas(String nama, String nip, String email, String passwordHash, boolean aktif) {
        super(nama, nip, email, passwordHash, aktif);
        isiHakAksesPetugas();
    }

    private void isiHakAksesPetugas() {
        daftarHakAkses.add(HakAkses.LIHAT_TEMUAN);
        daftarHakAkses.add(HakAkses.LIHAT_NOTIFIKASI);
        daftarHakAkses.add(HakAkses.VERIFIKASI_LAPORAN);
        daftarHakAkses.add(HakAkses.KELOLA_STATUS_ITEM);
        daftarHakAkses.add(HakAkses.KONTROL_STATUS);
        daftarHakAkses.add(HakAkses.KELOLA_LOKASI_SIMPAN);
        daftarHakAkses.add(HakAkses.KONFIRMASI_KANDIDAT);
        daftarHakAkses.add(HakAkses.KONFIRMASI_MATCHING);
        daftarHakAkses.add(HakAkses.KELOLA_QR_CODE);
        daftarHakAkses.add(HakAkses.PUTUSKAN_KLAIM);
        daftarHakAkses.add(HakAkses.KEPUTUSAN_KLAIM);
        daftarHakAkses.add(HakAkses.SERAH_TERIMA);
        daftarHakAkses.add(HakAkses.SERAH_TERIMA_BARANG);
        daftarHakAkses.add(HakAkses.LIHAT_SEMUA_KLAIM);
        daftarHakAkses.add(HakAkses.LIHAT_SEMUA_BARANG);
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Petugas: Antrean Verifikasi Laporan, Pemeriksaan Klaim, Smart Matching & Serah Terima.";
    }

    @Override
    public Role getRole() {
        return Role.PETUGAS;
    }
}