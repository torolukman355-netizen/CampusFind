package com.campusfind.model;

/**
 * Subclass Security (Dokumen Konsep v2.0).
 * Bertugas menerima barang di pos, memverifikasi klaim (via Verifikator),
 * mengubah status barang, mencetak QR Code, dan mencatat serah terima.
 */
public class Security extends Petugas implements Verifikator {

    private static final long serialVersionUID = 1L;

    public Security(String nama, String nip) {
        super(nama, nip, JenisPetugas.SECURITY);
    }

    @Override
    public void verifikasiKlaim(Klaim klaim, boolean disetujui) {
        if (klaim == null) {
            throw new IllegalArgumentException("Klaim tidak boleh null.");
        }
        klaim.ubahStatusKlaim(disetujui ? StatusKlaim.DISETUJUI : StatusKlaim.DITOLAK);
        klaim.setCatatanPetugas((disetujui ? "Disetujui" : "Ditolak") + " oleh Security " + getNama());
    }

    public void updateStatus(Laporan laporan, StatusLaporan statusBaru) {
        if (laporan != null && statusBaru != null) {
            laporan.ubahStatus(statusBaru);
        }
    }

    public String generateQR(Laporan laporan) {
        if (laporan == null) return "";
        String qr = "FOUND-" + laporan.getId();
        if (laporan.getJenis() == JenisLaporan.TEMUAN) {
            laporan.setKodeQR(qr);
        }
        return qr;
    }

    @Override
    public String tampilDashboard() {
        return "Dashboard Security (Verifikator): Verifikasi Klaim, OTP, QR Code, & Serah Terima.";
    }
}
