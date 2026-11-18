package model;

import model.enums.HakAkses;

/**
 * Model Role Admin (Lukman).
 * US-003, US-010, US-022, US-023:
 * Memiliki hak akses penuh untuk melihat semua barang, statistik, dan manajemen akun pengguna.
 */
public class Admin extends User {

    public Admin(String id, String nama, String email, String password) {
        super(id, nama, email, password);

        // US-003: Hak Akses Khusus Admin
        daftarHakAkses.add(HakAkses.LIHAT_SEMUA_BARANG);
        daftarHakAkses.add(HakAkses.KELOLA_SEMUA_LAPORAN);
        daftarHakAkses.add(HakAkses.MANAJEMEN_PENGGUNA);
        daftarHakAkses.add(HakAkses.LIHAT_STATISTIK);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Admin: Ringkasan Statistik Sistem, Return Rate, & Manajemen Pengguna.";
    }
}
