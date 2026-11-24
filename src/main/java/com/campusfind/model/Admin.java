package com.campusfind.model;


/**
 * Role Admin (Lukman).
 * US-003, US-010, US-022, US-023:
 * Memiliki hak akses pengelolaan menyeluruh, visualisasi statistik, dan manajemen akun pengguna.
 */
public class Admin extends User {

    private static final long serialVersionUID = 1L;

    public Admin(String nama, String idAdmin) {
        super(nama, idAdmin);
        daftarHakAkses.add(HakAkses.LIHAT_SEMUA_BARANG);
        daftarHakAkses.add(HakAkses.KELOLA_SEMUA_LAPORAN);
        daftarHakAkses.add(HakAkses.MANAJEMEN_PENGGUNA);
        daftarHakAkses.add(HakAkses.LIHAT_STATISTIK);
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Admin: Ringkasan Statistik Sistem, Return Rate, & Manajemen Pengguna.";
    }
}
