package com.campusfind;

import com.campusfind.model.BarangElektronik;
import com.campusfind.model.Item;
import com.campusfind.model.ItemFactory;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KandidatCocokItem;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.MatchResult;
import com.campusfind.model.StatusItem;
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.FileItemRepository;
import com.campusfind.repository.ItemRepository;
import com.campusfind.service.CandidateMatchingStrategy;
import com.campusfind.service.KandidatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Logika Bisnis KandidatService")
class KandidatServiceTest {

    private ItemRepository itemRepository;
    private KandidatService kandidatService;
    private CandidateMatchingStrategy matchingStrategy;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        FileDataStore dataStore = new FileDataStore(tempDir);
        itemRepository = new FileItemRepository(dataStore);
        // Default stub / strategy mockable via lambda
        matchingStrategy = (hilang, temuan) -> {
            int skor = 75;
            if (temuan.getNama().contains("69")) skor = 69;
            if (temuan.getNama().contains("70")) skor = 70;
            if (temuan.getNama().contains("90")) skor = 90;
            if (temuan.getNama().contains("85")) skor = 85;
            return new MatchResult(temuan.getId(), skor, List.of("Kategori sama"));
        };
        kandidatService = new KandidatService(itemRepository, matchingStrategy);
    }

    private Item createLaporanHilang(String id, String nimPelapor) {
        Item item = ItemFactory.create(
                id, JenisLaporan.HILANG, "Laptop Mahasiswa", KategoriBarang.ELEKTRONIK,
                "Laptop hilang", "Kantin", LocalDate.of(2026, 11, 20),
                null, null, "Ciri rahasia",
                StatusItem.DICARI, LocalDateTime.now(), "Pelapor A", nimPelapor, "Asus", ""
        );
        itemRepository.save(item);
        return item;
    }

    private Item createBarangTemuan(String id, String nama, StatusItem status, String nimPelapor, LocalDate tanggal) {
        Item item = ItemFactory.create(
                id, JenisLaporan.TEMUAN, nama, KategoriBarang.ELEKTRONIK,
                "Deskripsi barang temuan", "Kantin", tanggal,
                "POS-0001", null, "Rahasia temuan",
                status, LocalDateTime.now(), "Penemu", nimPelapor, "Asus", ""
        );
        itemRepository.save(item);
        return item;
    }

    @Test
    @DisplayName("Hanya barang temuan dengan status TERSEDIA yang diikutsertakan")
    void testHanyaTemuanTersedia() {
        String nim = "123456789";
        Item hilang = createLaporanHilang("ARC-0001", nim);

        createBarangTemuan("ARC-0002", "Laptop Tersedia", StatusItem.TERSEDIA, "999999999", LocalDate.of(2026, 11, 20));
        createBarangTemuan("ARC-0003", "Laptop Dalam Proses", StatusItem.DALAM_PROSES_KLAIM, "999999999", LocalDate.of(2026, 11, 20));
        createBarangTemuan("ARC-0004", "Laptop Diserahkan", StatusItem.DISERAHKAN, "999999999", LocalDate.of(2026, 11, 20));

        List<KandidatCocokItem> hasil = kandidatService.cariKandidat(hilang.getId(), nim);
        assertEquals(1, hasil.size());
        assertEquals("ARC-0002", hasil.get(0).getItemTemuan().getId());
    }

    @Test
    @DisplayName("Ambang batas skor 70 inklusif (skor 69 ditolak, skor 70 lolos)")
    void testAmbangSkorTujuhPuluhInklusif() {
        String nim = "123456789";
        Item hilang = createLaporanHilang("ARC-0001", nim);

        createBarangTemuan("ARC-0002", "Laptop Skor 69", StatusItem.TERSEDIA, "999999999", LocalDate.of(2026, 11, 20));
        createBarangTemuan("ARC-0003", "Laptop Skor 70", StatusItem.TERSEDIA, "999999999", LocalDate.of(2026, 11, 20));

        List<KandidatCocokItem> hasil = kandidatService.cariKandidat(hilang.getId(), nim);
        assertEquals(1, hasil.size());
        assertEquals("ARC-0003", hasil.get(0).getItemTemuan().getId());
        assertEquals(70, hasil.get(0).getSkor());
    }

    @Test
    @DisplayName("Urutan kandidat: skor menurun lalu tanggal terbaru")
    void testUrutanSkorMenurunLaluTerbaru() {
        String nim = "123456789";
        Item hilang = createLaporanHilang("ARC-0001", nim);

        createBarangTemuan("ARC-0002", "Laptop Biasa", StatusItem.TERSEDIA, "999999999", LocalDate.of(2026, 11, 15)); // skor 75
        createBarangTemuan("ARC-0003", "Laptop Skor 90", StatusItem.TERSEDIA, "999999999", LocalDate.of(2026, 11, 18)); // skor 90
        createBarangTemuan("ARC-0004", "Laptop Skor 85", StatusItem.TERSEDIA, "999999999", LocalDate.of(2026, 11, 19)); // skor 85

        List<KandidatCocokItem> hasil = kandidatService.cariKandidat(hilang.getId(), nim);
        assertEquals(3, hasil.size());
        assertEquals("ARC-0003", hasil.get(0).getItemTemuan().getId(), "Skor 90 harus pertama");
        assertEquals("ARC-0004", hasil.get(1).getItemTemuan().getId(), "Skor 85 harus kedua");
        assertEquals("ARC-0002", hasil.get(2).getItemTemuan().getId(), "Skor 75 harus ketiga");
    }

    @Test
    @DisplayName("Kandidat dibatasi maksimal 10 hasil")
    void testMaksimumSepuluhKandidat() {
        String nim = "123456789";
        Item hilang = createLaporanHilang("ARC-0001", nim);

        for (int i = 2; i <= 16; i++) {
            createBarangTemuan("ARC-" + String.format("%04d", i), "Laptop Item " + i, StatusItem.TERSEDIA, "999999999", LocalDate.of(2026, 11, 20));
        }

        List<KandidatCocokItem> hasil = kandidatService.cariKandidat(hilang.getId(), nim);
        assertEquals(10, hasil.size(), "Hasil kandidat harus dibatasi maksimal 10");
    }

    @Test
    @DisplayName("Peminta bukan pemilik laporan kehilangan ditolak dengan SecurityException")
    void testPemintaBukanPelaporDitolak() {
        String nimAsli = "123456789";
        String nimPenyusup = "999888777";
        Item hilang = createLaporanHilang("ARC-0001", nimAsli);

        assertThrows(SecurityException.class, () ->
                kandidatService.cariKandidat(hilang.getId(), nimPenyusup)
        );
    }

    @Test
    @DisplayName("Barang temuan yang dilaporkan oleh pelapor yang sama dikecualikan dari kandidat")
    void testPelaporSamaDikecualikan() {
        String nim = "123456789";
        Item hilang = createLaporanHilang("ARC-0001", nim);

        // Barang temuan dilaporkan oleh orang yang sama (NIM sama)
        createBarangTemuan("ARC-0002", "Laptop Temuan Sendiri", StatusItem.TERSEDIA, nim, LocalDate.of(2026, 11, 20));

        List<KandidatCocokItem> hasil = kandidatService.cariKandidat(hilang.getId(), nim);
        assertTrue(hasil.isEmpty(), "Barang yang dilaporkan sendiri tidak boleh jadi kandidat");
    }

    @Test
    @DisplayName("Pencarian pada laporan berjenis bukan HILANG ditolak dengan IllegalArgumentException")
    void testBukanLaporanHilangDitolak() {
        String nim = "123456789";
        Item temuan = createBarangTemuan("ARC-0001", "Laptop", StatusItem.TERSEDIA, nim, LocalDate.of(2026, 11, 20));

        assertThrows(IllegalArgumentException.class, () ->
                kandidatService.cariKandidat(temuan.getId(), nim)
        );
    }
}
