package com.campusfind.model;


/**
 * Role Mahasiswa (Pitya, Nadia).
 * Pengguna yang melaporkan kehilangan/penemuan, melihat kandidat cocok, dan mengajukan klaim.
 * Mengimplementasikan interface Pelapor (Dokumen Konsep v2.0).
 */
public class Mahasiswa extends User implements Pelapor {

    private static final long serialVersionUID = 1L;

    public Mahasiswa(String nama, String nim) {
        super(nama, nim);
        daftarHakAkses.add(HakAkses.BUAT_LAPORAN);
        daftarHakAkses.add(HakAkses.KLAIM_BARANG);
        daftarHakAkses.add(HakAkses.LIHAT_LAPORAN_SENDIRI);
    }

    public String getNim() {
        return getNomorIdentitas();
    }

    @Override
    public void buatLaporan(Laporan laporan) {
        if (laporan != null) {
            laporan.ubahStatus(StatusLaporan.DILAPORKAN);
        }
    }

    public void ajukanKlaim(Klaim klaim) {
        if (klaim != null) {
            klaim.ajukan();
        }
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Mahasiswa: Laporan Saya, Pelacakan Status, Kandidat Cocok, & Notifikasi.";
    }
}
