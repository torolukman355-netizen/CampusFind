package com.campusfind.integration;

import com.campusfind.model.JenisLaporan;
import com.campusfind.model.StatusItem;

/**
 * Adapter pemetaan dua arah antara status laporan model teman (String)
 * dengan status enum sistem CampusFind ({@link StatusItem}).
 */
public final class StatusMapper {

    private StatusMapper() {
        // utility class
    }

    public static StatusItem toStatusItem(String statusTeman, JenisLaporan jenis) {
        if (statusTeman == null || statusTeman.isBlank()) {
            return (jenis == JenisLaporan.TEMUAN) ? StatusItem.TERSEDIA : StatusItem.DICARI;
        }

        String normalized = statusTeman.trim().toUpperCase();

        if (normalized.startsWith("DITOLAK")) {
            return StatusItem.DITOLAK;
        } else if (normalized.startsWith("DIKLAIM")) {
            return StatusItem.DALAM_PROSES_KLAIM;
        } else if (normalized.startsWith("DIKEMBALIKAN")) {
            return StatusItem.DISERAHKAN;
        } else if (normalized.startsWith("DIVERIFIKASI") || normalized.startsWith("COCOK") || normalized.startsWith("DILAPORKAN")) {
            return (jenis == JenisLaporan.TEMUAN) ? StatusItem.TERSEDIA : StatusItem.DICARI;
        }

        return (jenis == JenisLaporan.TEMUAN) ? StatusItem.TERSEDIA : StatusItem.DICARI;
    }

    public static String toStatusTeman(StatusItem status) {
        if (status == null) {
            return "DIVERIFIKASI";
        }
        return switch (status) {
            case DILAPORKAN -> "DILAPORKAN";
            case DIVERIFIKASI, TERSEDIA, DICARI -> "DIVERIFIKASI";
            case DITOLAK -> "DITOLAK";
            case COCOK -> "COCOK";
            case DALAM_PROSES_KLAIM, DIKLAIM -> "DIKLAIM";
            case DISERAHKAN, DIKEMBALIKAN -> "DIKEMBALIKAN";
        };
    }
}