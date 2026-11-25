package com.campusfind;

import com.campusfind.model.Item;
import com.campusfind.model.ItemFactory;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.MatchResult;
import com.campusfind.model.StatusItem;
import com.campusfind.service.CandidateMatchingStrategy;
import com.campusfind.service.WeightedAttributeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian WeightedAttributeStrategy")
class WeightedAttributeStrategyTest {

    private CandidateMatchingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new WeightedAttributeStrategy();
    }

    private Item createElektronik(String id, JenisLaporan jenis, String nama,
                                  String deskripsi, String lokasi, LocalDate tanggal,
                                  String ciriRahasia, String merek, String nomorSeri) {
        return ItemFactory.create(
                id, jenis, nama, KategoriBarang.ELEKTRONIK, deskripsi, lokasi,
                tanggal, "POS-0001", null, ciriRahasia,
                jenis == JenisLaporan.TEMUAN ? StatusItem.TERSEDIA : StatusItem.DICARI,
                LocalDateTime.now(), "Budi", "123456789", merek, nomorSeri
        );
    }

    private Item createNonElektronik(String id, JenisLaporan jenis, KategoriBarang kategori,
                                     String nama, String deskripsi, String lokasi,
                                     LocalDate tanggal, String ciriRahasia, String warna, String bahan) {
        return ItemFactory.create(
                id, jenis, nama, kategori, deskripsi, lokasi,
                tanggal, "POS-0001", null, ciriRahasia,
                jenis == JenisLaporan.TEMUAN ? StatusItem.TERSEDIA : StatusItem.DICARI,
                LocalDateTime.now(), "Siti", "987654321", warna, bahan
        );
    }

    @Test
    @DisplayName("Pasangan sama menghasilkan skor >= 70 (tepatnya 100)")
    void testPasanganSamaSkorMinimal70() {
        Item hilang = createElektronik(
                "ARC-0001", JenisLaporan.HILANG,
                "Laptop Asus ROG Zephyrus",
                "Laptop warna hitam ada stiker github di belakang layar",
                "Perpustakaan Lantai 2",
                LocalDate.of(2026, 11, 20),
                "Serial di BIOS tertulis G14", "Asus", ""
        );

        Item temuan = createElektronik(
                "ARC-0002", JenisLaporan.TEMUAN,
                "Laptop Asus ROG Zephyrus",
                "Laptop warna hitam ada stiker github di belakang layar",
                "Perpustakaan Lantai 2",
                LocalDate.of(2026, 11, 20),
                "Serial di BIOS tertulis G14", "Asus", ""
        );

        MatchResult result = strategy.evaluate(hilang, temuan);
        assertTrue(result.getSkor() >= 70, "Skor pasangan identik harus >= 70, didapat: " + result.getSkor());
        assertEquals(100, result.getSkor());
        assertFalse(result.getAlasan().isEmpty());
    }

    @Test
    @DisplayName("Kategori berbeda menghasilkan skor 0 dan tanpa alasan")
    void testBedaKategoriSkorNol() {
        Item hilang = createElektronik(
                "ARC-0001", JenisLaporan.HILANG,
                "Smartwatch Apple Watch",
                "Jam tangan pintar warna hitam",
                "Kantin Pusat",
                LocalDate.of(2026, 11, 20),
                "Wallpaper foto kucing", "Apple", "SN12345"
        );

        Item temuan = createNonElektronik(
                "ARC-0002", JenisLaporan.TEMUAN, KategoriBarang.NON_ELEKTRONIK,
                "Smartwatch Apple Watch",
                "Jam tangan pintar warna hitam",
                "Kantin Pusat",
                LocalDate.of(2026, 11, 20),
                "Ada robekan", "Hitam", "Kain"
        );

        MatchResult result = strategy.evaluate(hilang, temuan);
        assertEquals(0, result.getSkor());
        assertTrue(result.getAlasan().isEmpty());
    }

    @Test
    @DisplayName("Nomor seri identik pada BarangElektronik menghasilkan skor override 100")
    void testNomorSeriIdentikOverride100() {
        Item hilang = createElektronik(
                "ARC-0001", JenisLaporan.HILANG,
                "Laptop Kantor ThinkPad",
                "Tertinggal saat rapat",
                "Ruang Sidang Rektorat",
                LocalDate.of(2026, 11, 10),
                "Password bios admin", "Lenovo", "PF-2XYZ99"
        );

        Item temuan = createElektronik(
                "ARC-0002", JenisLaporan.TEMUAN,
                "Laptop Hitam",
                "Ditemukan di bangku",
                "Gedung D",
                LocalDate.of(2026, 11, 15),
                "Password bios admin", "Lenovo", "PF-2XYZ99"
        );

        MatchResult result = strategy.evaluate(hilang, temuan);
        assertEquals(100, result.getSkor());
        assertTrue(result.getAlasan().stream().anyMatch(a -> a.contains("Nomor seri identik")));
        // Pastikan nomor seri utuh TIDAK dicantumkan di alasan
        assertFalse(result.getAlasan().stream().anyMatch(a -> a.contains("PF-2XYZ99")),
                "Nomor seri utuh dilarang dibocorkan ke alasan");
    }

    @Test
    @DisplayName("Temuan sebelum tanggal hilang menghasilkan skor waktu 0")
    void testTemuanSebelumTanggalHilangSkorWaktuNol() {
        Item hilang = createElektronik(
                "ARC-0001", JenisLaporan.HILANG,
                "Mouse Wireless Logitech",
                "Mouse bluetooth hitam",
                "Lab Komputer",
                LocalDate.of(2026, 11, 20),
                "Baterai ABC", "Logitech", ""
        );

        // Ditemukan 2 hari SEBELUM tanggal hilang (tidak masuk akal secara kronologis)
        Item temuan = createElektronik(
                "ARC-0002", JenisLaporan.TEMUAN,
                "Mouse Wireless Logitech",
                "Mouse bluetooth hitam",
                "Lab Komputer",
                LocalDate.of(2026, 11, 18),
                "Baterai ABC", "Logitech", ""
        );

        MatchResult result = strategy.evaluate(hilang, temuan);
        // Total tanpa waktu (30 nama + 20 deskripsi + 20 lokasi + 10 merek = 80)
        assertEquals(80, result.getSkor());
        // Alasan waktu tidak boleh ada
        assertFalse(result.getAlasan().stream().anyMatch(a -> a.contains("hari setelah kehilangan") || a.contains("hari yang sama")));
    }

    @Test
    @DisplayName("KEAMANAN KRITIS: Alasan tidak pernah memuat ciriRahasia barang temuan")
    void testAlasanTidakPernahMemuatCiriRahasia() {
        String rahasiaTemuan = "Ada ukiran rahasia bintang lima di pojok kanan bawah";

        Item hilang = createElektronik(
                "ARC-0001", JenisLaporan.HILANG,
                "Kamera Mirrorless Sony Alpha",
                "Kamera mirrorless hitam lensa kit",
                "Taman Rektorat",
                LocalDate.of(2026, 11, 20),
                "Lensa tergores tipis", "Sony", ""
        );

        Item temuan = createElektronik(
                "ARC-0002", JenisLaporan.TEMUAN,
                "Kamera Mirrorless Sony Alpha",
                "Kamera mirrorless hitam lensa kit",
                "Taman Rektorat",
                LocalDate.of(2026, 11, 20),
                rahasiaTemuan, "Sony", ""
        );

        MatchResult result = strategy.evaluate(hilang, temuan);
        for (String alasan : result.getAlasan()) {
            assertFalse(alasan.toLowerCase().contains("ukiran"), "Alasan membocorkan kata rahasia");
            assertFalse(alasan.toLowerCase().contains("bintang"), "Alasan membocorkan kata rahasia");
            assertFalse(alasan.toLowerCase().contains(rahasiaTemuan.toLowerCase()), "Alasan membocorkan ciri rahasia penuh");
        }
    }
}
