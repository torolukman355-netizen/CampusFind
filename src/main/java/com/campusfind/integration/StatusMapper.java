package com.campusfind.integration;

import com.campusfind.model.JenisLaporan;
import com.campusfind.model.StatusItem;

/**
 * Adapter pemetaan dua arah antara status laporan model teman (String)
 * dengan status enum sistem CampusFind ({@link StatusItem}).
 *
 * <table border="1">
 *   <caption>Tabel Pemetaan Status</caption>
 *   <tr><th>Status Teman (String)</th><th>Jenis Laporan</th><th>StatusItem (CampusFind)</th><th>Keterangan</th></tr>
 *   <tr><td>DILAPORKAN</td><td>HILANG / TEMUAN</td><td>DICARI / TERSEDIA</td><td>Belum diverifikasi petugas. Diabaikan dari kandidat matching hingga diverifikasi.</td></tr>
 *   <tr><td>DIVERIFIKASI</td><td>TEMUAN</td><td>TERSEDIA</td><td>Siap dicocokkan dan diklaim.</td></tr>
 *   <tr><td>DIVERIFIKASI</td><td>HILANG</td><td>DICARI</td><td>Aktif dicari di sistem.</td></tr>
 *   <tr><td>COCOK</td><td>HILANG / TEMUAN</td><td>DICARI / TERSEDIA</td><td>Penanda hasil matching (&ge; 70%), bukan status fisik tersimpan.</td></tr>
 *   <tr><td>DIKLAIM</td><td>TEMUAN</td><td>DALAM_PROSES_KLAIM</td><td>Sedang dalam proses verifikasi klaim.</td></tr>
 *   <tr><td>DIKEMBALIKAN</td><td>TEMUAN</td><td>DISERAHKAN</td><td>Telah diserahkan kepada pemilik sah.</td></tr>
 * </table>
 */
public final class StatusMapper {

    private StatusMapper() {
        // utility class
    }

    /**
     * Memetakan status string dari model teman menjadi {@link StatusItem}.
     *
     * @param statusTeman status string (misal: "DIVERIFIKASI", "DIKLAIM", "DIKEMBALIKAN")
     * @param jenis       jenis laporan (HILANG / TEMUAN) untuk menentukan status default
     * @return {@link StatusItem} padanan
     */
    public static StatusItem toStatusItem(String statusTeman, JenisLaporan jenis) {
        if (statusTeman == null || statusTeman.isBlank()) {
            return (jenis == JenisLaporan.TEMUAN) ? StatusItem.TERSEDIA : StatusItem.DICARI;
        }

        String normalized = statusTeman.trim().toUpperCase();

        if (normalized.startsWith("DIKLAIM")) {
            return StatusItem.DALAM_PROSES_KLAIM;
        } else if (normalized.startsWith("DIKEMBALIKAN")) {
            return StatusItem.DISERAHKAN;
        } else if (normalized.startsWith("DIVERIFIKASI") || normalized.startsWith("COCOK") || normalized.startsWith("DILAPORKAN")) {
            return (jenis == JenisLaporan.TEMUAN) ? StatusItem.TERSEDIA : StatusItem.DICARI;
        }

        return (jenis == JenisLaporan.TEMUAN) ? StatusItem.TERSEDIA : StatusItem.DICARI;
    }

    /**
     * Memetakan {@link StatusItem} menjadi status string untuk modul teman.
     *
     * @param status status item CampusFind
     * @return status string yang dikenali oleh modul Petugas teman
     */
    public static String toStatusTeman(StatusItem status) {
        if (status == null) {
            return "DIVERIFIKASI";
        }
        return switch (status) {
            case DILAPORKAN -> "DILAPORKAN";
            case DIVERIFIKASI, TERSEDIA, DICARI -> "DIVERIFIKASI";
            case COCOK -> "COCOK";
            case DALAM_PROSES_KLAIM, DIKLAIM -> "DIKLAIM";
            case DISERAHKAN, DIKEMBALIKAN -> "DIKEMBALIKAN";
        };
    };
}