package model;

import model.enums.HakAkses;

/**
 * Model Role Petugas (Bintang, Jepin, Fakhri).
 * Bertanggung jawab atas antrean verifikasi, kontrol transisi status, lokasi, QR, klaim, dan serah terima.
 */
public class Petugas extends User {
    private String nip;

    public Petugas(String id, String nama, String email, String password, String nip) {
        super(id, nama, email, password);
        this.nip = nip;

        // Inisialisasi Hak Akses Role Petugas
        daftarHakAkses.add(HakAkses.VERIFIKASI_LAPORAN);
        daftarHakAkses.add(HakAkses.KONTROL_STATUS);
        daftarHakAkses.add(HakAkses.KELOLA_LOKASI_SIMPAN);
        daftarHakAkses.add(HakAkses.KONFIRMASI_MATCHING);
        daftarHakAkses.add(HakAkses.KELOLA_QR_CODE);
        daftarHakAkses.add(HakAkses.KEPUTUSAN_KLAIM);
        daftarHakAkses.add(HakAkses.SERAH_TERIMA_BARANG);
        daftarHakAkses.add(HakAkses.LIHAT_SEMUA_BARANG);
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    @Override
    public String getRole() {
        return "Petugas";
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Petugas: Antrean Kerja, Verifikasi Laporan Masuk, & Kontrol Status Barang.";
    }
}
