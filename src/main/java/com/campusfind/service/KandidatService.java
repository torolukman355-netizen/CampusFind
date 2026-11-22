package com.campusfind.service;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KandidatCocokItem;
import com.campusfind.model.MatchResult;
import com.campusfind.model.StatusItem;
import com.campusfind.repository.ItemRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Layanan bisnis untuk mencari dan mengevaluasi kandidat barang temuan yang cocok
 * dengan laporan kehilangan (US-015, US-026).
 */
public class KandidatService {

    private static final Logger LOG = Logger.getLogger(KandidatService.class.getName());

    private final ItemRepository itemRepository;
    private final CandidateMatchingStrategy matchingStrategy;

    public KandidatService(ItemRepository itemRepository, CandidateMatchingStrategy matchingStrategy) {
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository tidak boleh null");
        this.matchingStrategy = Objects.requireNonNull(matchingStrategy, "matchingStrategy tidak boleh null");
    }

    /**
     * Mencari kandidat barang temuan yang cocok untuk suatu laporan kehilangan.
     *
     * @param laporanHilangId        ID laporan kehilangan
     * @param nomorIdentitasPeminta  NIM / NIP peminta (harus pemilik laporan)
     * @return daftar {@link KandidatCocokItem} yang memenuhi skor minimum (&gt;= 70),
     *         diurutkan dari skor tertinggi lalu tanggal temuan terbaru, maksimum 10 item.
     * @throws IllegalArgumentException jika laporan tidak ditemukan atau bukan berjenis HILANG
     * @throws SecurityException        jika peminta bukan pelapor asli laporan kehilangan
     */
    public List<KandidatCocokItem> cariKandidat(String laporanHilangId, String nomorIdentitasPeminta) {
        if (laporanHilangId == null || laporanHilangId.isBlank()) {
            throw new IllegalArgumentException("ID laporan kehilangan wajib diisi.");
        }
        if (nomorIdentitasPeminta == null || nomorIdentitasPeminta.isBlank()) {
            throw new SecurityException("Nomor identitas peminta wajib diisi.");
        }

        Item hilang = itemRepository.findById(laporanHilangId)
                .orElseThrow(() -> new IllegalArgumentException("Laporan kehilangan dengan ID " + laporanHilangId + " tidak ditemukan."));

        if (hilang.getJenis() != JenisLaporan.HILANG) {
            throw new IllegalArgumentException("Laporan harus berupa laporan kehilangan (HILANG).");
        }

        if (hilang.getPelapor() == null || !nomorIdentitasPeminta.equals(hilang.getPelapor().getNomorIdentitas())) {
            throw new SecurityException("Anda tidak berhak melihat kandidat untuk laporan ini.");
        }

        List<Item> temuanList = itemRepository.findByJenis(JenisLaporan.TEMUAN);
        List<KandidatCocokItem> hasil = new ArrayList<>();

        for (Item temuan : temuanList) {
            // Hanya barang berstatus TERSEDIA
            if (temuan.getStatus() != StatusItem.TERSEDIA) {
                continue;
            }

            // Pelapor berbeda
            if (temuan.getPelapor() != null && nomorIdentitasPeminta.equals(temuan.getPelapor().getNomorIdentitas())) {
                continue;
            }

            MatchResult result = matchingStrategy.evaluate(hilang, temuan);
            if (result.getSkor() >= ServicePolicy.MIN_SKOR_KANDIDAT) {
                hasil.add(new KandidatCocokItem(temuan, result));
            }
        }

        // Urutkan skor menurun (tertinggi dulu), lalu tanggal kejadian terbaru
        return hasil.stream()
                .sorted(Comparator.comparingInt(KandidatCocokItem::getSkor).reversed()
                        .thenComparing(k -> k.getItemTemuan().getTanggalKejadian(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(k -> k.getItemTemuan().getDibuatPada(), Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(ServicePolicy.MAX_KANDIDAT_HASIL)
                .toList();
    }
}
