package com.campusfind.model;

<<<<<<< HEAD:src/main/model/Admin.java
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
    }

    public Admin(String nama, String namaPengguna, String email, String passwordHash, boolean aktif) {
        super(nama, namaPengguna, email, passwordHash, aktif);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
=======

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
>>>>>>> ca2e27ff81bd0eaf0f0496ef3f356f4006abcbb8:src/main/java/com/campusfind/model/Admin.java
    }
}
