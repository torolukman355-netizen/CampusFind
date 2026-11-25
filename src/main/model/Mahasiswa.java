package com.campusfind.model;


/**
 * Role Mahasiswa (Pitya, Nadia).
 * Pengguna yang melaporkan kehilangan/penemuan, melihat kandidat cocok, dan mengajukan klaim.
 */
public class Mahasiswa extends User {

    private static final long serialVersionUID = 1L;

    public Mahasiswa(String nama, String nim) {
        super(nama, nim);
        daftarHakAkses.add(HakAkses.BUAT_LAPORAN);
        daftarHakAkses.add(HakAkses.KLAIM_BARANG);
        daftarHakAkses.add(HakAkses.LIHAT_LAPORAN_SENDIRI);
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Mahasiswa: Laporan Saya, Pelacakan Status, Kandidat Cocok, & Notifikasi.";
    }

    public Mahasiswa(String nama, String nim, String email, String passwordHash, boolean aktif) {
        super(nama, nim, email, passwordHash, aktif);
        daftarHakAkses.add(HakAkses.BUAT_LAPORAN);
        daftarHakAkses.add(HakAkses.KLAIM_BARANG);
        daftarHakAkses.add(HakAkses.LIHAT_LAPORAN_SENDIRI);
    }

    @Override
    public Role getRole() {
        return Role.MAHASISWA;
    }
}
