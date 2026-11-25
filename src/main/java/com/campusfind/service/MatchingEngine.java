package com.campusfind.service;

import com.campusfind.model.Item;
import com.campusfind.model.Laporan;
import com.campusfind.model.MatchResult;
import com.campusfind.model.NotificationType;
import com.campusfind.model.StatusLaporan;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Mesin Pencocokan Otomatis / Smart Matching (Dokumen Konsep v2.0 Section 2.5 & 3.4).
 * Memisahkan logika pencocokan dari kelas Item (Single Responsibility Principle)
 * serta menerapkan pra-filter (kategori sama & selisih waktu <= 7 hari) sebagai mitigasi bottleneck.
 */
public class MatchingEngine {

    private final CandidateMatchingStrategy strategiDasar;
    private final NotificationService notificationService;

    public MatchingEngine() {
        this(new WeightedAttributeStrategy(), null);
    }

    public MatchingEngine(NotificationService notificationService) {
        this(new WeightedAttributeStrategy(), notificationService);
    }

    public MatchingEngine(CandidateMatchingStrategy strategiDasar, NotificationService notificationService) {
        this.strategiDasar = Objects.requireNonNull(strategiDasar, "Strategi matching tidak boleh null");
        this.notificationService = notificationService;
    }

    /**
     * Pra-filter mitigasi bottleneck: hanya membandingkan laporan dengan kategori sama
     * dan selisih waktu kejadian maksimal 7 hari.
     */
    public boolean praFilter(Laporan hilang, Laporan temuan) {
        if (hilang == null || temuan == null || hilang.getItem() == null || temuan.getItem() == null) {
            return false;
        }
        if (hilang.getItem().getKategori() == null || temuan.getItem().getKategori() == null) {
            return false;
        }
        boolean kategoriSama = hilang.getItem().getKategori().equals(temuan.getItem().getKategori());
        if (!kategoriSama) {
            return false;
        }
        long selisih = selisihHari(hilang, temuan);
        return selisih <= 7;
    }

    private long selisihHari(Laporan a, Laporan b) {
        if (a.getWaktu() == null || b.getWaktu() == null) {
            return 0;
        }
        return Math.abs(ChronoUnit.DAYS.between(a.getWaktu().toLocalDate(), b.getWaktu().toLocalDate()));
    }

    /**
     * Menghitung skor kemiripan (0 - 100) antara laporan Hilang dan Temuan.
     * Mengembalikan 0 jika gagal melewati praFilter().
     */
    public int hitungSkor(Laporan hilang, Laporan temuan) {
        if (!praFilter(hilang, temuan)) {
            return 0;
        }
        MatchResult result = strategiDasar.evaluate(hilang.getItem(), temuan.getItem());
        return result.getSkor();
    }

    /**
     * Mengevaluasi kandidat beserta alasan kecocokannya.
     */
    public MatchResult evaluasiDetail(Laporan hilang, Laporan temuan) {
        if (!praFilter(hilang, temuan)) {
            return new MatchResult(
                    temuan != null && temuan.getItem() != null ? temuan.getItem().getId() : "",
                    0,
                    Collections.singletonList("Tidak lolos Pra-Filter (Kategori berbeda atau selisih > 7 hari)")
            );
        }
        return strategiDasar.evaluate(hilang.getItem(), temuan.getItem());
    }

    /**
     * Mencari semua rekomendasi temuan yang cocok (skor >= 70) dan mengirim notifikasi ke pelapor hilang.
     */
    public List<MatchResult> cariRekomendasi(Laporan hilang, List<Laporan> daftarTemuan) {
        List<MatchResult> hasil = new ArrayList<>();
        if (hilang == null || daftarTemuan == null) {
            return hasil;
        }

        for (Laporan t : daftarTemuan) {
            if (t.getStatus() == StatusLaporan.DITOLAK || t.getStatus() == StatusLaporan.DIARSIPKAN) {
                continue;
            }
            MatchResult res = evaluasiDetail(hilang, t);
            if (res.getSkor() >= ServicePolicy.MIN_SKOR_KANDIDAT) {
                hasil.add(res);
                if (notificationService != null && hilang.getPelapor() != null) {
                    Item itemTemuan = t.getItem();
                    notificationService.kirim(
                            hilang.getPelapor().getNomorIdentitas(),
                            NotificationType.KANDIDAT_COCOK,
                            "Kandidat Barang Cocok (" + res.getSkor() + "%)",
                            "Barang Anda '" + hilang.getItem().getNama() + "' kemungkinan ditemukan: "
                                    + itemTemuan.getNama() + " di " + t.getLokasi() + ".",
                            itemTemuan.getId()
                    );
                }
            }
        }
        return hasil;
    }
}
