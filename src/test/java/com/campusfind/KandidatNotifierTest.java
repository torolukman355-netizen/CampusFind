package com.campusfind;

import com.campusfind.model.Item;
import com.campusfind.model.ItemFactory;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Notification;
import com.campusfind.model.NotificationType;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.StatusItem;
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.FileItemRepository;
import com.campusfind.repository.FileNotificationRepository;
import com.campusfind.repository.FilePosRepository;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.NotificationRepository;
import com.campusfind.repository.PosRepository;
import com.campusfind.service.CandidateMatchingStrategy;
import com.campusfind.service.ItemService;
import com.campusfind.service.KandidatNotifier;
import com.campusfind.service.NotificationService;
import com.campusfind.service.WeightedAttributeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian KandidatNotifier (Notifikasi Otomatis US-026)")
class KandidatNotifierTest {

    private FileDataStore dataStore;
    private ItemRepository itemRepository;
    private NotificationRepository notificationRepository;
    private NotificationService notificationService;
    private CandidateMatchingStrategy matchingStrategy;
    private KandidatNotifier kandidatNotifier;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        dataStore = new FileDataStore(tempDir);
        itemRepository = new FileItemRepository(dataStore);
        notificationRepository = new FileNotificationRepository(dataStore);
        notificationService = new NotificationService(notificationRepository);
        matchingStrategy = new WeightedAttributeStrategy();

        kandidatNotifier = new KandidatNotifier(
                itemRepository,
                matchingStrategy,
                notificationService,
                notificationRepository
        );
    }

    private Item createLaporanHilang(String id, String nama, String nim) {
        return ItemFactory.create(
                id, JenisLaporan.HILANG, nama, KategoriBarang.ELEKTRONIK,
                "Laptop warna hitam ada stiker", "Perpustakaan",
                LocalDate.of(2026, 11, 20), null, null, "Ciri rahasia",
                StatusItem.DICARI, LocalDateTime.now(), "Pelapor", nim, "Asus", ""
        );
    }

    private Item createBarangTemuan(String id, String nama, String nim, KategoriBarang kategori) {
        return ItemFactory.create(
                id, JenisLaporan.TEMUAN, nama, kategori,
                "Laptop warna hitam ada stiker", "Perpustakaan",
                LocalDate.of(2026, 11, 20), "POS-0001", null, "Rahasia temuan",
                StatusItem.TERSEDIA, LocalDateTime.now(), "Penemu", nim, "Asus", ""
        );
    }

    @Test
    @DisplayName("Laporan temuan baru yang cocok memicu notifikasi KANDIDAT_COCOK ke pelapor hilang")
    void testTemuanBaruMemicuNotifikasi() {
        String nimHilang = "111222333";
        String nimTemuan = "444555666";

        // Simpan laporan hilang yang sudah ada sebelumnya
        Item hilang = createLaporanHilang("ARC-0001", "Laptop Asus ROG Zephyrus", nimHilang);
        itemRepository.save(hilang);

        // Buat barang temuan yang cocok dan panggil listener
        Item temuan = createBarangTemuan("ARC-0002", "Laptop Asus ROG Zephyrus", nimTemuan, KategoriBarang.ELEKTRONIK);
        itemRepository.save(temuan);

        kandidatNotifier.onItemCreated(temuan);

        List<Notification> notifikasi = notificationService.findByPenerima(nimHilang);
        assertEquals(1, notifikasi.size());
        Notification n = notifikasi.get(0);
        assertEquals(NotificationType.KANDIDAT_COCOK, n.getTipe());
        assertEquals("ARC-0001:ARC-0002", n.getReferensiId());
        assertTrue(n.getIsi().contains("Laptop Asus ROG Zephyrus"));
    }

    @Test
    @DisplayName("Laporan temuan dengan beda kategori tidak memicu notifikasi")
    void testTemuanBedaKategoriTidakMemicuNotifikasi() {
        String nimHilang = "111222333";
        String nimTemuan = "444555666";

        Item hilang = createLaporanHilang("ARC-0001", "Laptop Asus ROG Zephyrus", nimHilang);
        itemRepository.save(hilang);

        // Kategori NON_ELEKTRONIK (skor = 0)
        Item temuan = createBarangTemuan("ARC-0002", "Laptop Asus ROG Zephyrus", nimTemuan, KategoriBarang.NON_ELEKTRONIK);
        itemRepository.save(temuan);

        kandidatNotifier.onItemCreated(temuan);

        List<Notification> notifikasi = notificationService.findByPenerima(nimHilang);
        assertTrue(notifikasi.isEmpty(), "Tidak boleh ada notifikasi jika skor < 70");
    }

    @Test
    @DisplayName("Idempotensi: pasangan hilang-temuan yang sama tidak mengirim notifikasi ganda")
    void testIdempotensiDedup() {
        String nimHilang = "111222333";
        String nimTemuan = "444555666";

        Item hilang = createLaporanHilang("ARC-0001", "Laptop Asus ROG Zephyrus", nimHilang);
        itemRepository.save(hilang);

        Item temuan = createBarangTemuan("ARC-0002", "Laptop Asus ROG Zephyrus", nimTemuan, KategoriBarang.ELEKTRONIK);
        itemRepository.save(temuan);

        // Panggil pertama
        kandidatNotifier.onItemCreated(temuan);
        // Panggil kedua kali untuk item yang sama
        kandidatNotifier.onItemCreated(temuan);

        List<Notification> notifikasi = notificationService.findByPenerima(nimHilang);
        assertEquals(1, notifikasi.size(), "Notifikasi harus idempoten dan tidak boleh diduplikasi");
    }

    @Test
    @DisplayName("Kegagalan listener tidak menggagalkan pembuatan laporan barang di ItemService")
    void testKegagalanListenerTidakMenggagalkanLaporan() {
        PosRepository posRepository = new FilePosRepository(dataStore);
        posRepository.save(new PosPenyimpanan("POS-0001", "Pos Satpam", "Gedung A", "08123"));

        ItemService itemService = new ItemService(itemRepository, posRepository, Clock.systemDefaultZone());

        // Daftarkan listener yang sengaja melempar exception
        itemService.addItemCreatedListener(item -> {
            throw new RuntimeException("Simulasi database error pada listener");
        });

        // Pembuatan laporan harus tetap berhasil
        assertDoesNotThrow(() -> {
            itemService.laporkan(
                    JenisLaporan.HILANG,
                    "Kunci Motor Beat",
                    KategoriBarang.NON_ELEKTRONIK,
                    "Kunci motor gantungan dompet",
                    "Parkiran FMIPA",
                    LocalDate.of(2026, 11, 20),
                    null,
                    null,
                    "Ciri rahasia kunci",
                    "Andi",
                    "112233445",
                    "Hitam",
                    "Besi"
            );
        });

        assertEquals(1, itemRepository.findAll().size(), "Item harus tetap tersimpan meski listener gagal");
    }
}
