package com.campusfind;

import com.campusfind.model.Item;
import com.campusfind.model.ItemFactory;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.Notification;
import com.campusfind.model.Petugas;
import com.campusfind.model.SesiLayanan;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.FileItemRepository;
import com.campusfind.repository.FileKlaimRepository;
import com.campusfind.repository.FileNotificationRepository;
import com.campusfind.repository.FilePosRepository;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.repository.NotificationRepository;
import com.campusfind.repository.PosRepository;
import com.campusfind.service.BarangTidakTersediaException;
import com.campusfind.service.KlaimGandaException;
import com.campusfind.service.KlaimService;
import com.campusfind.service.MatchingStrategy;
import com.campusfind.service.TokenOverlapStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Logika Bisnis KlaimService")
class KlaimServiceTest {

    private Clock clock;
    private ZoneId zone;
    private Path tempDirPath;
    private FileDataStore dataStore;
    private ItemRepository itemRepository;
    private KlaimRepository klaimRepository;
    private PosRepository posRepository;
    private NotificationRepository notificationRepository;
    private MatchingStrategy matchingStrategy;
    private KlaimService klaimService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDirPath = tempDir;
        zone = ZoneId.of("Asia/Jakarta");
        // 2026-11-20 10:00:00 (Jumat)
        Instant fixedInstant = LocalDate.of(2026, 11, 20)
                .atTime(10, 0, 0)
                .atZone(zone)
                .toInstant();
        clock = Clock.fixed(fixedInstant, zone);

        dataStore = new FileDataStore(tempDir);
        itemRepository = new FileItemRepository(dataStore);
        klaimRepository = new FileKlaimRepository(dataStore);
        posRepository = new FilePosRepository(dataStore);
        notificationRepository = new FileNotificationRepository(dataStore);
        matchingStrategy = new TokenOverlapStrategy();

        klaimService = new KlaimService(
                klaimRepository,
                itemRepository,
                posRepository,
                notificationRepository,
                matchingStrategy,
                clock
        );
    }

    private File createSampleBuktiFile(String filename) {
        try {
            File f = tempDirPath.resolve(filename).toFile();
            Files.write(f.toPath(), new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x01});
            return f;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Item createSampleTemuanItem(String pelaporId) {
        String id = itemRepository.nextId();
        Item item = ItemFactory.create(
                id,
                JenisLaporan.TEMUAN,
                "Kunci Motor Vario",
                KategoriBarang.KUNCI_KENDARAAN,
                "Gantungan warna hitam",
                "Parkir Barat",
                LocalDate.of(2026, 11, 19),
                "POS-0001",
                null,
                "Kunci hitam remote alarm",
                StatusItem.TERSEDIA,
                LocalDateTime.now(clock),
                "Bambang",
                pelaporId,
                "Hitam",
                "Besi"
        );
        itemRepository.save(item);
        return item;
    }

    @Test
    @DisplayName("Pengajuan klaim berhasil: status DIAJUKAN, status item DALAM_PROSES_KLAIM, dan notifikasi dibuat")
    void testSukses() {
        Item item = createSampleTemuanItem("123456789");
        File bukti = createSampleBuktiFile("bukti_siti.jpg");

        Klaim klaim = klaimService.ajukan(
                item.getId(),
                "987654321", // Pengklaim NIM berbeda
                "Siti Aminah",
                "Kunci motor remote hitam ada lecet di tombol alarm",
                bukti,
                LocalDate.of(2026, 11, 23), // Senin depan
                SesiLayanan.PAGI,
                true,
                true
        );

        assertNotNull(klaim);
        assertEquals(StatusKlaim.DIAJUKAN, klaim.getStatus());
        assertEquals("Diajukan", klaim.getStatus().getLabel());

        // Status item harus berubah menjadi DALAM_PROSES_KLAIM
        Item updated = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(StatusItem.DALAM_PROSES_KLAIM, updated.getStatus());

        // Notifikasi untuk pengklaim harus dibuat
        List<Notification> notifList = notificationRepository.findByPenerima("987654321");
        assertFalse(notifList.isEmpty());
        assertTrue(notifList.get(0).getIsi().contains(item.kode()));
    }

    @Test
    @DisplayName("Pengajuan klaim tanpa berkas bukti wajib melempar IllegalArgumentException")
    void testBuktiWajib() {
        Item item = createSampleTemuanItem("123456789");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                klaimService.ajukan(
                        item.getId(),
                        "987654321",
                        "Siti",
                        "Kunci motor ada gantungan",
                        null,
                        LocalDate.of(2026, 11, 23),
                        SesiLayanan.PAGI,
                        true,
                        true
                )
        );
        assertEquals("Bukti kepemilikan wajib diunggah.", ex.getMessage());
    }

    @Test
    @DisplayName("Pengajuan klaim pada barang yang tidak tersedia melempar BarangTidakTersediaException")
    void testItemTidakTersedia() {
        Item item = createSampleTemuanItem("123456789");
        item.setStatus(StatusItem.DALAM_PROSES_KLAIM);
        itemRepository.save(item);
        File bukti = createSampleBuktiFile("bukti.jpg");

        BarangTidakTersediaException ex = assertThrows(BarangTidakTersediaException.class, () ->
                klaimService.ajukan(
                        item.getId(),
                        "987654321",
                        "Siti",
                        "Kunci motor hilang",
                        bukti,
                        LocalDate.of(2026, 11, 23),
                        SesiLayanan.PAGI,
                        true,
                        true
                )
        );
        assertEquals("Barang ini sudah tidak tersedia untuk diklaim.", ex.getMessage());
    }

    @Test
    @DisplayName("Pengajuan klaim ganda pada item yang sama melempar KlaimGandaException")
    void testKlaimGanda() {
        Item item = createSampleTemuanItem("123456789");
        File bukti1 = createSampleBuktiFile("bukti1.jpg");
        File bukti2 = createSampleBuktiFile("bukti2.jpg");

        // Klaim pertama berhasil
        klaimService.ajukan(
                item.getId(),
                "987654321",
                "Siti",
                "Kunci motor ada gantungan",
                bukti1,
                LocalDate.of(2026, 11, 23),
                SesiLayanan.PAGI,
                true,
                true
        );

        // Klaim kedua untuk item yang sama melempar KlaimGandaException atau BarangTidakTersediaException
        assertThrows(IllegalStateException.class, () ->
                klaimService.ajukan(
                        item.getId(),
                        "112233445",
                        "Rudi",
                        "Kunci motor saya",
                        bukti2,
                        LocalDate.of(2026, 11, 23),
                        SesiLayanan.PAGI,
                        true,
                        true
                )
        );
    }

    @Test
    @DisplayName("Uji konkurensi: 25 thread mengajukan klaim untuk barang yang sama secara simultan -> tepat 1 sukses")
    void testKonkurensiKlaimGanda() throws Exception {
        Item item = createSampleTemuanItem("123456789");
        int totalThreads = 25;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(totalThreads);

        AtomicInteger suksesCount = new AtomicInteger(0);
        AtomicInteger gagalCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < totalThreads; i++) {
            final String nim = String.format("98765%04d", i);
            File bukti = createSampleBuktiFile("bukti_conc_" + i + ".jpg");

            futures.add(executor.submit(() -> {
                try {
                    startLatch.await();
                    klaimService.ajukan(
                            item.getId(),
                            nim,
                            "User " + nim,
                            "Ciri khusus barang kunci kendaraan",
                            bukti,
                            LocalDate.of(2026, 11, 23),
                            SesiLayanan.PAGI,
                            true,
                            true
                    );
                    suksesCount.incrementAndGet();
                } catch (KlaimGandaException | BarangTidakTersediaException e) {
                    gagalCount.incrementAndGet();
                } catch (Exception e) {
                    gagalCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            }));
        }

        // Mulai semua thread bersamaan
        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        assertEquals(1, suksesCount.get(), "Tepat 1 thread harus berhasil mengklaim");
        assertEquals(totalThreads - 1, gagalCount.get(), "Semua thread lain harus gagal karena klaim ganda / barang tidak tersedia");
    }

    @Test
    @DisplayName("Regresi G5: kegagalan notifikasi membatalkan penyimpanan klaim tanpa meninggalkan klaim hantu")
    void testRollbackG5KonsistensiPenyimpanan() {
        Item item = createSampleTemuanItem("123456789");
        File bukti = createSampleBuktiFile("bukti_g5.jpg");

        // Mock notification repository yang selalu melempar error saat save
        NotificationRepository failingNotifRepo = new NotificationRepository() {
            @Override
            public void save(Notification notification) {
                throw new RuntimeException("Simulasi database notifikasi down!");
            }

            @Override
            public List<Notification> findByPenerima(String nomorIdentitas) {
                return List.of();
            }

            @Override
            public int countBelumDibaca(String nomorIdentitas) {
                return 0;
            }

            @Override
            public Optional<Notification> findById(String id) {
                return Optional.empty();
            }

            @Override
            public void markAsRead(String id, String nomorIdentitas) {}

            @Override
            public void markAllAsRead(String nomorIdentitas) {}

            @Override
            public void markAsRead(String id) {}
        };

        KlaimService serviceWithFailingNotif = new KlaimService(
                klaimRepository,
                itemRepository,
                posRepository,
                failingNotifRepo,
                matchingStrategy,
                clock
        );

        assertThrows(RuntimeException.class, () ->
                serviceWithFailingNotif.ajukan(
                        item.getId(),
                        "987654321",
                        "Siti",
                        "Kunci motor remote alarm",
                        bukti,
                        LocalDate.of(2026, 11, 23),
                        SesiLayanan.PAGI,
                        true,
                        true
                )
        );

        // Verifikasi: tidak ada klaim hantu yang tersimpan
        assertTrue(klaimRepository.findAll().isEmpty(), "Klaim hantu tidak boleh tertinggal di repository!");
        // Status item harus tetap TERSEDIA
        Item itemAfterRollback = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(StatusItem.TERSEDIA, itemAfterRollback.getStatus(), "Status item harus kembali TERSEDIA!");
    }

    @Test
    @DisplayName("Pelapor barang tidak dapat mengklaim barangnya sendiri")
    void testKlaimOlehPelaporSendiri() {
        Item item = createSampleTemuanItem("123456789");
        File bukti = createSampleBuktiFile("bukti.jpg");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                klaimService.ajukan(
                        item.getId(),
                        "123456789", // Identitas sama dengan pelapor
                        "Bambang",
                        "Ini barang saya yang saya temukan sendiri",
                        bukti,
                        LocalDate.of(2026, 11, 23),
                        SesiLayanan.PAGI,
                        true,
                        true
                )
        );
        assertEquals("Pelapor barang tidak dapat mengklaim barangnya sendiri.", ex.getMessage());
    }

    @Test
    @DisplayName("Batalkan klaim mengubah status klaim ke DIBATALKAN dan memulihkan status item ke TERSEDIA")
    void testBatalkanMemulihkanStatus() {
        Item item = createSampleTemuanItem("123456789");
        File bukti = createSampleBuktiFile("bukti.jpg");

        Klaim klaim = klaimService.ajukan(
                item.getId(),
                "987654321",
                "Siti",
                "Kunci motor saya",
                bukti,
                LocalDate.of(2026, 11, 23),
                SesiLayanan.PAGI,
                true,
                true
        );

        // Batalkan oleh pemilik klaim yang sah
        klaimService.batalkan(klaim.getId(), "987654321");

        Klaim updatedKlaim = klaimRepository.findById(klaim.getId()).orElseThrow();
        assertEquals(StatusKlaim.DIBATALKAN, updatedKlaim.getStatus());

        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(StatusItem.TERSEDIA, updatedItem.getStatus());
    }

    @Test
    @DisplayName("Setujui klaim: status klaim menjadi DISETUJUI, dan klaim lain otomatis DITOLAK")
    void testSetujuiKlaim() {
        Item item = createSampleTemuanItem("123456789");
        File bukti1 = createSampleBuktiFile("bukti_setuju1.jpg");
        Petugas petugas = new Petugas("Pak Satpam", "198001012005011001");

        Klaim klaim1 = klaimService.ajukan(
                item.getId(),
                "987654321",
                "Siti",
                "Kunci motor saya ada gantungan",
                bukti1,
                LocalDate.of(2026, 11, 23),
                SesiLayanan.PAGI,
                true,
                true
        );

        // Simulasikan ada klaim kedua yang sempat masuk
        Klaim klaim2 = new Klaim(
                "CLM-2026-9999",
                item.getId(),
                new Petugas("Pengklaim 2", "198501012010011002"),
                "Kunci motor cadangan",
                "path/bukti2.jpg",
                LocalDate.of(2026, 11, 23),
                SesiLayanan.SIANG,
                "POS-0001",
                true,
                true,
                50,
                StatusKlaim.DIAJUKAN,
                LocalDateTime.now(clock),
                null
        );
        klaimRepository.save(klaim2);

        // Petugas menyetujui klaim1
        klaimService.setujui(klaim1.getId(), petugas);

        Klaim updatedKlaim1 = klaimRepository.findById(klaim1.getId()).orElseThrow();
        assertEquals(StatusKlaim.DISETUJUI, updatedKlaim1.getStatus());

        // Klaim2 otomatis ditolak
        Klaim updatedKlaim2 = klaimRepository.findById(klaim2.getId()).orElseThrow();
        assertEquals(StatusKlaim.DITOLAK, updatedKlaim2.getStatus());
        assertTrue(updatedKlaim2.getAlasanPenolakan().contains("Otomatis:"));

        // Notifikasi diterima oleh Siti dan Pengklaim 2
        assertFalse(notificationRepository.findByPenerima("987654321").isEmpty());
        assertFalse(notificationRepository.findByPenerima("198501012010011002").isEmpty());
    }

    @Test
    @DisplayName("Tolak klaim: alasan wajib (minimal 5 karakter), item kembali TERSEDIA")
    void testTolakKlaim() {
        Item item = createSampleTemuanItem("123456789");
        File bukti = createSampleBuktiFile("bukti_tolak.jpg");
        Petugas petugas = new Petugas("Pak Satpam", "198001012005011001");

        Klaim klaim = klaimService.ajukan(
                item.getId(),
                "987654321",
                "Siti",
                "Kunci motor saya ada gantungan",
                bukti,
                LocalDate.of(2026, 11, 23),
                SesiLayanan.PAGI,
                true,
                true
        );

        // Tolak tanpa alasan atau alasan terlalu pendek gagal
        assertThrows(IllegalArgumentException.class, () ->
                klaimService.tolak(klaim.getId(), "", petugas)
        );
        assertThrows(IllegalArgumentException.class, () ->
                klaimService.tolak(klaim.getId(), "abc", petugas)
        );

        // Tolak dengan alasan sah
        klaimService.tolak(klaim.getId(), "Ciri rahasia remote alarm tidak sesuai", petugas);

        Klaim updatedKlaim = klaimRepository.findById(klaim.getId()).orElseThrow();
        assertEquals(StatusKlaim.DITOLAK, updatedKlaim.getStatus());
        assertEquals("Ciri rahasia remote alarm tidak sesuai", updatedKlaim.getAlasanPenolakan());

        // Item kembali berstatus TERSEDIA
        Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertEquals(StatusItem.TERSEDIA, updatedItem.getStatus());
    }

    @Test
    @DisplayName("Aksi persetujuan dan penolakan oleh aktor non-Petugas melempar SecurityException")
    void testAktorBukanPetugas() {
        Item item = createSampleTemuanItem("123456789");
        File bukti = createSampleBuktiFile("bukti_aktor.jpg");

        Klaim klaim = klaimService.ajukan(
                item.getId(),
                "987654321",
                "Siti",
                "Kunci motor saya",
                bukti,
                LocalDate.of(2026, 11, 23),
                SesiLayanan.PAGI,
                true,
                true
        );

        assertThrows(SecurityException.class, () ->
                klaimService.setujui(klaim.getId(), null)
        );
        assertThrows(SecurityException.class, () ->
                klaimService.tolak(klaim.getId(), "Alasan penolakan valid", null)
        );
    }
}
