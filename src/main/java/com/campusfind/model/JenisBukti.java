package com.campusfind.model;

/**
 * Jenis bukti kepemilikan yang digunakan saat pengajuan dan verifikasi klaim (Dokumen Konsep v2.0).
 * Barang Elektronik memprioritaskan verifikasi berbasis akun terdaftar.
 */
public enum JenisBukti {
    OTP_AKUN_TERDAFTAR("OTP ke Akun Terdaftar (Email/No. HP)"),
    UNLOCK_PERANGKAT("Membuka Kunci Layar Perangkat"),
    RECOVERY_AKUN("Verifikasi Recovery Akun Perangkat"),
    NOMOR_SERI("Nomor Seri / IMEI Perangkat (Cadangan)"),
    DUS_PEMBELIAN("Dus / Nota Pembelian Resmi (Cadangan)"),
    CIRI_FISIK("Penjelasan Ciri Fisik & Isi Barang");

    private final String label;

    JenisBukti(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
