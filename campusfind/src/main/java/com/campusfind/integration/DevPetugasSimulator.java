package com.campusfind.integration;

import com.campusfind.model.Petugas;
import com.campusfind.service.KlaimService;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Simulator aksi petugas untuk pengujian dan demo end-to-end tanpa memerlukan UI petugas teman (US-019, US-027).
 * Aktif dan digunakan saat properti sistem {@code -Dcampusfind.dev=true} disetel,
 * atau melalui pengujian dan simulasi skenario manual.
 */
public class DevPetugasSimulator {

    private static final Logger LOG = Logger.getLogger(DevPetugasSimulator.class.getName());

    private final KlaimService klaimService;
    private final Petugas defaultPetugas;

    public DevPetugasSimulator(KlaimService klaimService, Petugas defaultPetugas) {
        this.klaimService = Objects.requireNonNull(klaimService, "klaimService tidak boleh null");
        this.defaultPetugas = (defaultPetugas != null)
                ? defaultPetugas
                : new Petugas("Pak Budi (Simulator Satpam)", "198001012005011001");
    }

    public DevPetugasSimulator(KlaimService klaimService) {
        this(klaimService, null);
    }

    public static boolean isDevMode() {
        return Boolean.getBoolean("campusfind.dev");
    }

    /**
     * Menyimulasikan persetujuan klaim oleh petugas.
     */
    public void simulasikanSetujui(String klaimId) {
        LOG.info(() -> "[DevSimulator] Menyetujui klaim: " + klaimId);
        klaimService.setujui(klaimId, defaultPetugas);
    }

    /**
     * Menyimulasikan penolakan klaim oleh petugas disertai alasan.
     */
    public void simulasikanTolak(String klaimId, String alasan) {
        LOG.info(() -> "[DevSimulator] Menolak klaim: " + klaimId + " dengan alasan: " + alasan);
        klaimService.tolak(klaimId, alasan, defaultPetugas);
    }

    public Petugas getDefaultPetugas() {
        return defaultPetugas;
    }
}
