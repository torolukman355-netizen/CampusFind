package com.campusfind.service;

import com.campusfind.model.Notification;
import com.campusfind.model.NotificationType;
import com.campusfind.repository.NotificationRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Logika bisnis untuk pengelolaan notifikasi in-app.
 */
public class NotificationService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    public NotificationService(NotificationRepository notificationRepository, Clock clock) {
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    public NotificationService(NotificationRepository notificationRepository) {
        this(notificationRepository, Clock.systemDefaultZone());
    }

    /**
     * Pintu tunggal untuk membuat dan mengirimkan notifikasi baru ke pengguna.
     *
     * @param penerimaNomorIdentitas NIM / NIP penerima
     * @param tipe                    tipe klasifikasi notifikasi
     * @param judul                   judul notifikasi
     * @param isi                     pesan notifikasi
     * @param referensiId             ID rujukan objek terkait (klaimId / hilangId:temuanId)
     * @return entitas {@link Notification} yang tersimpan
     */
    public Notification kirim(String penerimaNomorIdentitas,
                              NotificationType tipe,
                              String judul,
                              String isi,
                              String referensiId) {
        if (penerimaNomorIdentitas == null || penerimaNomorIdentitas.isBlank()) {
            throw new IllegalArgumentException("Nomor identitas penerima wajib diisi.");
        }

        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                penerimaNomorIdentitas,
                tipe,
                judul,
                isi,
                referensiId,
                LocalDateTime.now(clock),
                false
        );

        notificationRepository.save(notification);
        LOG.info(() -> "Notifikasi terkirim ke " + penerimaNomorIdentitas + " [" + tipe + "]: " + judul);
        return notification;
    }

    /**
     * Mencari semua notifikasi untuk penerima tertentu, diurutkan dari yang terbaru.
     */
    public List<Notification> findByPenerima(String nomorIdentitas) {
        return notificationRepository.findByPenerima(nomorIdentitas).stream()
                .sorted(Comparator.comparing(Notification::getDibuatPada).reversed())
                .toList();
    }

    /**
     * Menghitung jumlah notifikasi yang belum dibaca untuk penerima tertentu.
     */
    public int countBelumDibaca(String nomorIdentitas) {
        return notificationRepository.countBelumDibaca(nomorIdentitas);
    }

    /**
     * Menandai notifikasi sebagai sudah dibaca dengan verifikasi kepemilikan (G6).
     *
     * @param id             ID notifikasi
     * @param nomorIdentitas nomor identitas pemilik yang meminta
     */
    public void markAsRead(String id, String nomorIdentitas) {
        notificationRepository.markAsRead(id, nomorIdentitas);
        LOG.fine(() -> "Notifikasi " + id + " ditandai sudah dibaca oleh " + nomorIdentitas);
    }

    /**
     * Menandai seluruh notifikasi milik penerima tertentu sebagai sudah dibaca.
     *
     * @param nomorIdentitas nomor identitas penerima
     */
    public void markAllAsRead(String nomorIdentitas) {
        notificationRepository.markAllAsRead(nomorIdentitas);
        LOG.fine(() -> "Seluruh notifikasi ditandai dibaca untuk: " + nomorIdentitas);
    }

    /**
     * @deprecated Gunakan {@link #markAsRead(String, String)} yang memverifikasi kepemilikan.
     */
    @Deprecated
    public void markAsRead(String id) {
        notificationRepository.markAsRead(id);
    }
}
