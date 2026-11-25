package com.campusfind.model;

/**
 * Interface Verifikator (Dokumen Konsep v2.0).
 * Hak verifikasi klaim hanya dimiliki oleh Security sesuai prinsip hak akses minimum (least privilege).
 */
public interface Verifikator {
    void verifikasiKlaim(Klaim klaim, boolean disetujui);
}
