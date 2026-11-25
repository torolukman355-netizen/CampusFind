package com.campusfind.model;


/**
 * Role Petugas Kampus (Dokumen Konsep v2.0: Security & Cleaning Service).
 * Mengimplementasikan Pelapor agar seluruh petugas dapat melaporkan temuan dari lapangan.
 * Subclass Security mengimplementasikan Verifikator untuk memutuskan klaim.
 */
public class Petugas extends User implements Pelapor {

    private static final long serialVersionUID = 1L;

    protected final JenisPetugas jenis;

    public Petugas(String nama, String nip) {
        this(nama, nip, JenisPetugas.SECURITY);
    }

    public Petugas(String nama, String nip, JenisPetugas jenis) {
        super(nama, nip);
        this.jenis = jenis != null ? jenis : JenisPetugas.SECURITY;
        daftarHakAkses.add(HakAkses.BUAT_LAPORAN);
        daftarHakAkses.add(HakAkses.LIHAT_SEMUA_BARANG);

        if (this.jenis == JenisPetugas.SECURITY) {
            daftarHakAkses.add(HakAkses.VERIFIKASI_LAPORAN);
            daftarHakAkses.add(HakAkses.KONTROL_STATUS);
            daftarHakAkses.add(HakAkses.KELOLA_LOKASI_SIMPAN);
            daftarHakAkses.add(HakAkses.KONFIRMASI_MATCHING);
            daftarHakAkses.add(HakAkses.KELOLA_QR_CODE);
            daftarHakAkses.add(HakAkses.KEPUTUSAN_KLAIM);
            daftarHakAkses.add(HakAkses.SERAH_TERIMA_BARANG);
            daftarHakAkses.add(HakAkses.LIHAT_AUDIT_LOG);
        } else {
            // Cleaning Service: pelapor lapangan & penyerah ke pos (Prinsip Least Privilege)
            daftarHakAkses.add(HakAkses.SERAHKAN_BARANG_KE_POS);
        }
    }

    public JenisPetugas getJenis() {
        return jenis;
    }

    @Override
    public void buatLaporan(Laporan laporan) {
        if (laporan != null) {
            laporan.ubahStatus(StatusLaporan.DILAPORKAN);
        }
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Petugas (" + jenis.getLabel() + "): Operasional Pos, Verifikasi, & Pelacakan.";
    }
}
