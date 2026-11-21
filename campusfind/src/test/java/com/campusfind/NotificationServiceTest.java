package com.campusfind;

import com.campusfind.model.Notification;
import com.campusfind.model.NotificationType;
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.FileNotificationRepository;
import com.campusfind.repository.NotificationRepository;
import com.campusfind.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Logika Bisnis NotificationService")
class NotificationServiceTest {

    private Clock clock;
    private ZoneId zone;
    private FileDataStore dataStore;
    private NotificationRepository notificationRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        zone = ZoneId.of("Asia/Jakarta");
        Instant initialInstant = Instant.parse("2026-11-20T10:00:00Z");
        clock = Clock.fixed(initialInstant, zone);

        dataStore = new FileDataStore(tempDir);
        notificationRepository = new FileNotificationRepository(dataStore);
        notificationService = new NotificationService(notificationRepository, clock);
    }

    @Test
    @DisplayName("Kirim notifikasi menetapkan tipe, referensiId, dan tersimpan belum dibaca")
    void testKirimNotifikasi() {
        Notification n = notificationService.kirim(
                "123456789",
                NotificationType.KLAIM_DITERIMA,
                "Klaim Diterima",
                "Pengajuan klaim Anda telah diterima.",
                "CLM-2026-0001"
        );

        assertNotNull(n);
        assertNotNull(n.getId());
        assertEquals("123456789", n.getPenerimaNomorIdentitas());
        assertEquals(NotificationType.KLAIM_DITERIMA, n.getTipe());
        assertEquals("CLM-2026-0001", n.getReferensiId());
        assertFalse(n.isDibaca());

        assertEquals(1, notificationService.countBelumDibaca("123456789"));
    }

    @Test
    @DisplayName("findByPenerima mengembalikan notifikasi dalam urutan terbaru dulu")
    void testFindByPenerimaTerbaruDulu() {
        // Buat 3 notifikasi dengan waktu berbeda
        Clock clock1 = Clock.fixed(Instant.parse("2026-11-20T10:00:00Z"), zone);
        Clock clock2 = Clock.fixed(Instant.parse("2026-11-20T10:30:00Z"), zone);
        Clock clock3 = Clock.fixed(Instant.parse("2026-11-20T11:00:00Z"), zone);

        NotificationService svc1 = new NotificationService(notificationRepository, clock1);
        NotificationService svc2 = new NotificationService(notificationRepository, clock2);
        NotificationService svc3 = new NotificationService(notificationRepository, clock3);

        svc1.kirim("123456789", NotificationType.KLAIM_DITERIMA, "Notif 1", "Pesan 1", "REF-1");
        svc2.kirim("123456789", NotificationType.KANDIDAT_COCOK, "Notif 2", "Pesan 2", "REF-2");
        svc3.kirim("123456789", NotificationType.KLAIM_DISETUJUI, "Notif 3", "Pesan 3", "REF-3");

        List<Notification> list = notificationService.findByPenerima("123456789");
        assertEquals(3, list.size());
        // Terbaru dulu: Notif 3 (11:00) -> Notif 2 (10:30) -> Notif 1 (10:00)
        assertEquals("Notif 3", list.get(0).getJudul());
        assertEquals("Notif 2", list.get(1).getJudul());
        assertEquals("Notif 1", list.get(2).getJudul());
    }

    @Test
    @DisplayName("markAsRead oleh pemilik berhasil mengubah status menjadi dibaca")
    void testMarkAsReadOlehPemilik() {
        Notification n = notificationService.kirim(
                "123456789",
                NotificationType.KLAIM_DITERIMA,
                "Klaim Diterima",
                "Pesan",
                "CLM-1"
        );

        assertEquals(1, notificationService.countBelumDibaca("123456789"));

        notificationService.markAsRead(n.getId(), "123456789");

        assertEquals(0, notificationService.countBelumDibaca("123456789"));
        assertTrue(notificationRepository.findById(n.getId()).orElseThrow().isDibaca());
    }

    @Test
    @DisplayName("markAsRead oleh non-pemilik gagal dengan SecurityException (G6)")
    void testMarkAsReadOlehNonPemilikGagal() {
        Notification n = notificationService.kirim(
                "123456789", // Milik user A
                NotificationType.KLAIM_DITERIMA,
                "Klaim Diterima",
                "Pesan rahasia",
                "CLM-1"
        );

        // User B mencoba menandai dibaca
        assertThrows(SecurityException.class, () ->
                notificationService.markAsRead(n.getId(), "987654321")
        );

        // Status notifikasi tetap belum dibaca
        assertFalse(notificationRepository.findById(n.getId()).orElseThrow().isDibaca());
        assertEquals(1, notificationService.countBelumDibaca("123456789"));
    }

    @Test
    @DisplayName("markAllAsRead menandai seluruh notifikasi milik pengguna yang bersangkutan dan tidak mempengaruhi pengguna lain")
    void testMarkAllAsRead() {
        notificationService.kirim("123456789", NotificationType.KLAIM_DITERIMA, "A1", "P1", "REF-1");
        notificationService.kirim("123456789", NotificationType.KLAIM_DISETUJUI, "A2", "P2", "REF-2");
        notificationService.kirim("987654321", NotificationType.KLAIM_DITERIMA, "B1", "P3", "REF-3");

        assertEquals(2, notificationService.countBelumDibaca("123456789"));
        assertEquals(1, notificationService.countBelumDibaca("987654321"));

        notificationService.markAllAsRead("123456789");

        assertEquals(0, notificationService.countBelumDibaca("123456789"));
        // User B tidak terpengaruh
        assertEquals(1, notificationService.countBelumDibaca("987654321"));
    }
}
