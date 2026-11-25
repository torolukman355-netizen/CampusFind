package com.campusfind.model;


/**
 * Role Admin (Lukman).
 * US-003, US-010, US-022, US-023 & Dokumen Konsep v2.0:
 * Memiliki hak akses pengelolaan master data, manajemen akun pengguna, audit log, pengarsipan barang lama, dan visualisasi statistik.
 */
public class Admin extends User implements Pelapor {

    private static final long serialVersionUID = 1L;

    public Admin(String nama, String idAdmin) {
        super(nama, idAdmin);
        daftarHakAkses.add(HakAkses.BUAT_LAPORAN);
        daftarHakAkses.add(HakAkses.LIHAT_SEMUA_BARANG);
        daftarHakAkses.add(HakAkses.KELOLA_SEMUA_LAPORAN);
        daftarHakAkses.add(HakAkses.MANAJEMEN_PENGGUNA);
        daftarHakAkses.add(HakAkses.LIHAT_STATISTIK);
        daftarHakAkses.add(HakAkses.ARSIP_BARANG);
        daftarHakAkses.add(HakAkses.LIHAT_AUDIT_LOG);
        daftarHakAkses.add(HakAkses.KELOLA_MASTER_DATA);
    }

    @Override
    public void buatLaporan(Laporan laporan) {
        if (laporan != null) {
            laporan.ubahStatus(StatusLaporan.DILAPORKAN);
        }
    }

    public String kelolaMasterData() {
        return "Mengelola Master Data Pos Penyimpanan & Kategori Barang.";
    }

    public String lihatAuditLog() {
        return "Mengakses Riwayat Aktivitas & Audit Log Sistem.";
    }

    public String arsipkanBarangLama() {
        return "Mengarsipkan barang berstatus tak diklaim (> 60 hari) menjadi DIARSIPKAN.";
    }

    public String lihatStatistik() {
        return "Menampilkan Statistik Return Rate & Rata-rata Waktu Penyelesaian.";
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Admin: Ringkasan Statistik Sistem, Return Rate, Audit Log, Arsip & Manajemen Pengguna.";
    }
}
