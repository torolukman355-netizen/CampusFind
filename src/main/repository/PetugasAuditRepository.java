package com.campusfind.repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Kontrak penyimpanan log aktivitas petugas dan catatan serah terima barang.
 */
public interface PetugasAuditRepository {

    void simpanLog(String itemId, String aksi, String pelaku);

    List<String> ambilLog(String itemId);

    void simpanSerahTerima(String itemId, String namaPenerima, LocalDate tanggal, String nipPetugas);
}