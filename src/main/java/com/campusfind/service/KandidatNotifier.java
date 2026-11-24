package com.campusfind.service;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.MatchResult;
import com.campusfind.model.NotificationType;
import com.campusfind.model.StatusItem;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.NotificationRepository;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Notifier otomatis untuk mendeteksi kecocokan saat laporan baru dibuat (US-026, ADR-4).
 * Mengimplementasikan ItemCreatedListener dan bersifat idempoten.
 */
public class KandidatNotifier implements ItemCreatedListener {

    private static final Logger LOG = Logger.getLogger(KandidatNotifier.class.getName());

    private final ItemRepository itemRepository;
    private final CandidateMatchingStrategy matchingStrategy;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public KandidatNotifier(ItemRepository itemRepository,
                            CandidateMatchingStrategy matchingStrategy,
                            NotificationService notificationService,
                            NotificationRepository notificationRepository) {
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository tidak boleh null");
        this.matchingStrategy = Objects.requireNonNull(matchingStrategy, "matchingStrategy tidak boleh null");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService tidak boleh null");
        this.notificationRepository = Objects.requireNonNull(notificationRepository, "notificationRepository tidak boleh null");
    }

    @Override
    public void onItemCreated(Item item) {
        if (item == null) return;

        try {
            if (item.getJenis() == JenisLaporan.HILANG) {
                evaluasiUntukLaporanHilang(item);
            } else if (item.getJenis() == JenisLaporan.TEMUAN) {
                evaluasiUntukBarangTemuan(item);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Terjadi kesalahan saat memproses evaluasi kandidat cocok: " + e.getMessage(), e);
        }
    }

    private void evaluasiUntukLaporanHilang(Item hilang) {
        if (hilang.getPelapor() == null || hilang.getPelapor().getNomorIdentitas() == null) {
            return;
        }
        String peminta = hilang.getPelapor().getNomorIdentitas();

        List<Item> temuanList = itemRepository.findByJenis(JenisLaporan.TEMUAN);
        for (Item temuan : temuanList) {
            if (temuan.getStatus() != StatusItem.TERSEDIA) {
                continue;
            }
            if (temuan.getPelapor() != null && peminta.equals(temuan.getPelapor().getNomorIdentitas())) {
                continue;
            }

            prosesPasangan(hilang, temuan);
        }
    }

    private void evaluasiUntukBarangTemuan(Item temuan) {
        if (temuan.getStatus() != StatusItem.TERSEDIA) {
            return;
        }

        List<Item> hilangList = itemRepository.findByJenis(JenisLaporan.HILANG);
        for (Item hilang : hilangList) {
            if (hilang.getStatus() != StatusItem.DICARI) {
                continue;
            }
            if (hilang.getPelapor() == null || hilang.getPelapor().getNomorIdentitas() == null) {
                continue;
            }
            if (temuan.getPelapor() != null && hilang.getPelapor().getNomorIdentitas().equals(temuan.getPelapor().getNomorIdentitas())) {
                continue;
            }

            prosesPasangan(hilang, temuan);
        }
    }

    private void prosesPasangan(Item hilang, Item temuan) {
        MatchResult result = matchingStrategy.evaluate(hilang, temuan);
        if (result.getSkor() >= ServicePolicy.MIN_SKOR_KANDIDAT) {
            String penerima = hilang.getPelapor().getNomorIdentitas();
            String dedupKey = hilang.getId() + ":" + temuan.getId();

            // Cek idempotensi: apakah notifikasi untuk pasangan ini sudah pernah dikirim ke pelapor hilang
            boolean sudahAda = notificationRepository.findByPenerima(penerima).stream()
                    .anyMatch(n -> n.getTipe() == NotificationType.KANDIDAT_COCOK && dedupKey.equals(n.getReferensiId()));

            if (!sudahAda) {
                notificationService.kirim(
                        penerima,
                        NotificationType.KANDIDAT_COCOK,
                        NotificationMessages.judulKandidatCocok(),
                        NotificationMessages.isiKandidatCocok(hilang, temuan, result.getSkor()),
                        dedupKey
                );
                LOG.info(() -> "Notifikasi kandidat cocok dikirim untuk pasangan: " + dedupKey);
            }
        }
    }
}
