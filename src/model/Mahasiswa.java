package model;

import model.enums.HakAkses;

/**
 * Model Role Mahasiswa (Pitya dan Nadia).
 * Hak akses terbatas untuk membuat laporan, klaim barang, dan memantau status pribadi.
 */
public class Mahasiswa extends User {
    private String nim;

    public Mahasiswa(String id, String nama, String email, String password, String nim) {
        super(id, nama, email, password);
        this.nim = nim;

        daftarHakAkses.add(HakAkses.BUAT_LAPORAN);
        daftarHakAkses.add(HakAkses.KLAIM_BARANG);
        daftarHakAkses.add(HakAkses.LIHAT_LAPORAN_SENDIRI);
    }

    public String getNim() {
        return nim;
    }

    public void setNim(String nim) {
        this.nim = nim;
    }

    @Override
    public String getRole() {
        return "Mahasiswa";
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Mahasiswa: Laporan Saya, Pelacakan Status, & Notifikasi In-App.";
    }
}
