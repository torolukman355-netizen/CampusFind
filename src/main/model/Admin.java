package com.campusfind.model;

import java.io.Serializable;

/**
 * Pengguna dengan role {@link Role#ADMIN}.
 * <p>
 * Admin memiliki seluruh hak akses Petugas ditambah akses statistik layanan.
 * Nomor identitas admin berupa nama pengguna (bukan NIM/NIP).
 */
public class Admin extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    public Admin(String nama, String namaPengguna) {
        super(nama, namaPengguna);
        daftarHakAkses.add(HakAkses.LIHAT_SEMUA_BARANG);
        daftarHakAkses.add(HakAkses.KELOLA_SEMUA_LAPORAN);
        daftarHakAkses.add(HakAkses.MANAJEMEN_PENGGUNA);
        daftarHakAkses.add(HakAkses.LIHAT_STATISTIK);
    }

    public Admin(String nama, String namaPengguna, String email, String passwordHash, boolean aktif) {
        super(nama, namaPengguna, email, passwordHash, aktif);
        daftarHakAkses.add(HakAkses.LIHAT_SEMUA_BARANG);
        daftarHakAkses.add(HakAkses.KELOLA_SEMUA_LAPORAN);
        daftarHakAkses.add(HakAkses.MANAJEMEN_PENGGUNA);
        daftarHakAkses.add(HakAkses.LIHAT_STATISTIK);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    public String tampilDashboard() {
        return "Dashboard Admin: Ringkasan Statistik Sistem, Return Rate, & Manajemen Pengguna.";
    }
}
