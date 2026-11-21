package com.campusfind.integration;

import com.campusfind.model.StatusKlaim;

import java.util.Objects;

/**
 * Formatter status klaim untuk menjembatani format status string modul Petugas teman
 * yang menggabungkan status dan alasan penolakan dalam satu teks ("DITOLAK (alasan)").
 */
public final class KlaimStatusFormatter {

    private KlaimStatusFormatter() {
        // utility class
    }

    /**
     * Memformat status klaim dan alasan menjadi format string modul Petugas:
     * {@code "DITOLAK (alasan)"} atau status reguler.
     *
     * @param status status klaim
     * @param alasan alasan penolakan (opsional jika bukan ditolak)
     * @return representasi string gabungan
     */
    public static String format(StatusKlaim status, String alasan) {
        Objects.requireNonNull(status, "Status klaim tidak boleh null");
        if (status == StatusKlaim.DITOLAK) {
            String sanitizedAlasan = (alasan != null && !alasan.isBlank()) ? alasan.trim() : "Tidak memenuhi syarat kepemilikan";
            return "DITOLAK (" + sanitizedAlasan + ")";
        }
        return status.name();
    }

    /**
     * Mengekstrak alasan dari status gabungan string teman misal "DITOLAK (Alasan)".
     *
     * @param statusString string dari model teman
     * @return alasan penolakan, atau null jika bukan berstatus ditolak
     */
    public static String extractAlasan(String statusString) {
        if (statusString == null || !statusString.toUpperCase().startsWith("DITOLAK")) {
            return null;
        }
        int start = statusString.indexOf('(');
        int end = statusString.lastIndexOf(')');
        if (start != -1 && end != -1 && end > start + 1) {
            return statusString.substring(start + 1, end).trim();
        }
        return null;
    }

    /**
     * Memetakan string status teman menjadi {@link StatusKlaim}.
     *
     * @param statusString teks status teman
     * @return status klaim yang sesuai
     */
    public static StatusKlaim parseStatus(String statusString) {
        if (statusString == null || statusString.isBlank()) {
            return StatusKlaim.DIAJUKAN;
        }
        String upper = statusString.trim().toUpperCase();
        if (upper.startsWith("DITOLAK")) {
            return StatusKlaim.DITOLAK;
        } else if (upper.startsWith("DISETUJUI")) {
            return StatusKlaim.DISETUJUI;
        } else if (upper.startsWith("DIBATALKAN")) {
            return StatusKlaim.DIBATALKAN;
        } else if (upper.startsWith("SELESAI")) {
            return StatusKlaim.SELESAI;
        }
        return StatusKlaim.DIAJUKAN;
    }
}
