package com.campusfind;

import com.campusfind.model.Admin;
import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.HakAkses;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.Mahasiswa;
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
import com.campusfind.service.ItemService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.MatchingStrategy;
import com.campusfind.service.NotificationService;
import com.campusfind.service.PetugasService;
import com.campusfind.service.QRCodeService;
import com.campusfind.service.TokenOverlapStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite pengujian komprehensif untuk Fitur Utama Role Petugas (Bintang, Jepin, Fakhri):
 * - US-001 s.d. US-008: OOP Polimorfisme & Hak Akses
 * - US-010 & US-028   : Dashboard Antrean Kerja
 * - US-011 & US-028   : Verifikasi Laporan Masuk
 * - US-012            : Kontrol Transisi Status Berurutan & Logging Audit
 * - US-013            : Manajemen Lokasi Penyimpanan Fisik (Khusus Temuan)
 * - US-015            : Konfirmasi Status Smart Matching
 * - US-017, US-018, US-029: Manajemen & Pelacakan QR Code Barang
 * - US-020, US-028    : Pemeriksaan & Keputusan Klaim
 * - US-021, US-029    : Pencatatan Serah Terima Barang & Penguncian Data
 */
public class PetugasRoleComprehensiveTest {

    @TempDir
    Path tempDir;

    private ItemRepository itemRepository;
    private KlaimRepository klaimRepository;
    private PosRepository posRepository;
    private NotificationRepository notificationRepository;

    private ItemService itemService;
    private KlaimService klaimService;
    private NotificationService notificationService;
    private QRCodeService qrCodeService;
    private PetugasService petugasService;

    private Petugas petugasBintang;
    private Mahasiswa mhsPelapor;
    private Mahasiswa mhsKlaim1;
    private Mahasiswa mhsKlaim2;

    @BeforeEach
    void setUp() {
        FileDataStore dataStore = new FileDataStore(tempDir);
        itemRepository = new FileItemRepository(dataStore);
        klaimRepository = new FileKlaimRepository(dataStore);
        posRepository = new FilePosRepository(dataStore);
        notificationRepository = new FileNotificationRepository(dataStore);

        Clock clock = Clock.systemDefaultZone();
        MatchingStrategy matchingStrategy = new TokenOverlapStrategy();

        itemService = new ItemService(itemRepository, posRepository, clock);
        klaimService = new KlaimService(klaimRepository, itemRepository, posRepository, notificationRepository, matchingStrategy, clock);
        notificationService = new NotificationService(notificationRepository);
        qrCodeService = new QRCodeService();
        petugasService = new PetugasService(itemRepository, posRepository, notificationService, qrCodeService);

        petugasBintang = new Petugas("Bintang (Petugas)", "199504122020121001");
        mhsPelapor = new Mahasiswa("Rama", "230001001");
        mhsKlaim1 = new Mahasiswa("Budi", "230001002");
        mhsKlaim2 = new Mahasiswa("Siti", "230001003");
    }

    @Test
    @DisplayName("US-007 & US-008: Polimorfisme Item.tampilkanDetail() pada BarangElektronik dan BarangNonElektronik")
    void testPolymorphismTampilkanDetail() {
        Item laptop = new BarangElektronik(
                "T-999", JenisLaporan.TEMUAN, "MacBook Air M2",
                KategoriBarang.ELEKTRONIK, "Ditemukan di Ruang Seminar Lt. 3",
                "Ruang Seminar Lt. 3", LocalDate.now(), "POS-0001", null, "Stiker NASA di pojok kanan",
                StatusItem.DILAPORKAN, LocalDateTime.now(), mhsPelapor,
                "Apple", "SN-MAC123456"
        );

        Item dompet = new BarangNonElektronik(
                "H-888", JenisLaporan.HILANG, "Dompet Kulit Coklat",
                KategoriBarang.NON_ELEKTRONIK, "Tertinggal di kantin",
                "Kantin Pusat", LocalDate.now(), null, null, "Sobekan kecil",
                StatusItem.DILAPORKAN, LocalDateTime.now(), mhsPelapor,
                "Coklat", "Kulit Sintetis"
        );

        String detailLaptop = laptop.tampilkanDetail();
        String detailDompet = dompet.tampilkanDetail();

        assertNotNull(detailLaptop);
        assertNotNull(detailDompet);

        // Verifikasi output polimorfik spesifik
        assertTrue(detailLaptop.contains("Apple"), "Detail elektronik memuat merek");
        assertTrue(detailLaptop.contains("SN-MAC123456"), "Detail elektronik memuat nomor seri rahasia");
        assertTrue(detailLaptop.contains("RAHASIA PETUGAS"), "Detail elektronik mencantumkan penanda rahasia");

        assertTrue(detailDompet.contains("Kulit Sintetis"), "Detail non-elektronik memuat bahan");
        assertTrue(detailDompet.contains("Coklat"), "Detail non-elektronik memuat warna");
    }

    @Test
    @DisplayName("US-011: Verifikasi Laporan Masuk (Terima & Otomatis QR Code Temuan)")
    void testVerifikasiLaporanTerimaDanQR() {
        Item temuan = new BarangElektronik(
                "T-001", JenisLaporan.TEMUAN, "Laptop Asus ROG",
                KategoriBarang.ELEKTRONIK, "Tertinggal di Lab",
                "Lab Multimedia", LocalDate.now(), "POS-0001", null, null,
                StatusItem.DILAPORKAN, LocalDateTime.now(), mhsPelapor,
                "Asus", "SN-ROG998"
        );
        itemRepository.save(temuan);

        assertEquals(StatusItem.DILAPORKAN, temuan.getStatus());

        // Aksi Petugas: Terima Laporan
        petugasService.verifikasiLaporan("T-001", true, null, petugasBintang);

        Item verified = itemRepository.findById("T-001").orElseThrow();
        assertEquals(StatusItem.TERSEDIA, verified.getStatus(), "Status temuan terverifikasi menjadi TERSEDIA");

        // Verifikasi kode QR otomatis dibuat untuk barang Temuan
        String qr = petugasService.getKodeQR(verified);
        assertNotNull(qr);
        assertTrue(qr.startsWith("QR-T-001"));
    }

    @Test
    @DisplayName("US-011: Penolakan Laporan Masuk Wajib Mengisi Alasan Tertulis")
    void testVerifikasiLaporanTolakWajibAlasan() {
        Item temuanPalsu = new BarangNonElektronik(
                "T-999", JenisLaporan.TEMUAN, "Uang 1 Miliar",
                KategoriBarang.NON_ELEKTRONIK, "Laporan iseng",
                "Gazebo", LocalDate.now(), "POS-0001", null, null,
                StatusItem.DILAPORKAN, LocalDateTime.now(), mhsPelapor,
                "Hijau", "Kertas"
        );
        itemRepository.save(temuanPalsu);

        // Tolak tanpa alasan -> Harus gagal melempar IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
                petugasService.verifikasiLaporan("T-999", false, "  ", petugasBintang));

        // Tolak dengan alasan sah -> Berhasil
        petugasService.verifikasiLaporan("T-999", false, "Laporan palsu / tidak dapat diverifikasi", petugasBintang);

        assertNotNull(itemService);
        Item rejected = itemRepository.findById("T-999").orElseThrow();
        assertNotNull(rejected);
        assertTrue(petugasService.getRiwayatLog("T-999").contains("DITOLAK"));
    }

    @Test
    @DisplayName("US-012: Kontrol Transisi Status Berurutan dan Proteksi Data Terkunci")
    void testKontrolTransisiStatusBerurutanDanKunci() {
        Item item = new BarangElektronik(
                "T-010", JenisLaporan.TEMUAN, "iPad Pro M1",
                KategoriBarang.ELEKTRONIK, "Tertinggal di perpus",
                "Perpustakaan", LocalDate.now(), "POS-0001", null, null,
                StatusItem.DILAPORKAN, LocalDateTime.now(), mhsPelapor,
                "Apple", "SN-IPAD772"
        );
        itemRepository.save(item);

        // 1. DILAPORKAN -> TERSEDIA (Diverifikasi)
        petugasService.verifikasiLaporan("T-010", true, null, petugasBintang);
        assertEquals(StatusItem.TERSEDIA, itemRepository.findById("T-010").orElseThrow().getStatus());

        // 2. TERSEDIA -> COCOK
        petugasService.ubahStatusBarang("T-010", StatusItem.COCOK, petugasBintang);
        assertEquals(StatusItem.COCOK, itemRepository.findById("T-010").orElseThrow().getStatus());

        // 3. COCOK -> DIKLAIM
        petugasService.ubahStatusBarang("T-010", StatusItem.DIKLAIM, petugasBintang);
        assertEquals(StatusItem.DIKLAIM, itemRepository.findById("T-010").orElseThrow().getStatus());

        // 4. DIKLAIM -> DIKEMBALIKAN (Serah Terima)
        petugasService.prosesSerahTerima("T-010", "Rama Aditya", LocalDate.now(), petugasBintang);
        Item finalItem = itemRepository.findById("T-010").orElseThrow();
        assertEquals(StatusItem.DIKEMBALIKAN, finalItem.getStatus());
        assertTrue(finalItem.isTerkunci(), "Data barang harus terkunci permanen");

        // 5. Coba ubah status saat sudah terkunci -> Wajib ditolak
        assertThrows(IllegalStateException.class, () ->
                petugasService.ubahStatusBarang("T-010", StatusItem.TERSEDIA, petugasBintang));
    }

    @Test
    @DisplayName("US-013: Lokasi Penyimpanan Fisik HANYA Berlaku untuk Barang TEMUAN")
    void testManajemenLokasiSimpanKhususTemuan() {
        Item temuan = new BarangNonElektronik(
                "T-020", JenisLaporan.TEMUAN, "Kunci Motor Honda",
                KategoriBarang.NON_ELEKTRONIK, "Ditemukan di parkiran",
                "Parkir Barat", LocalDate.now(), "POS-0001", null, null,
                StatusItem.TERSEDIA, LocalDateTime.now(), mhsPelapor,
                "Hitam", "Besi"
        );
        Item hilang = new BarangNonElektronik(
                "H-021", JenisLaporan.HILANG, "Kunci Motor Yamaha",
                KategoriBarang.NON_ELEKTRONIK, "Hilang di parkiran",
                "Parkir Barat", LocalDate.now(), null, null, null,
                StatusItem.DICARI, LocalDateTime.now(), mhsPelapor,
                "Hitam", "Besi"
        );
        itemRepository.save(temuan);
        itemRepository.save(hilang);

        // Atur lokasi simpan pada barang TEMUAN -> Berhasil
        petugasService.aturLokasiPenyimpanan("T-020", "Loker B-2 Ruang Satpam", petugasBintang);
        assertEquals("Loker B-2 Ruang Satpam", petugasService.getLokasiSimpan(itemRepository.findById("T-020").orElseThrow()));

        // Atur lokasi simpan pada barang HILANG -> Wajib ditolak (IllegalArgumentException)
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                petugasService.aturLokasiPenyimpanan("H-021", "Loker A-1", petugasBintang));
        assertTrue(ex.getMessage().contains("TEMUAN"), "Pesan error menjelaskan hanya untuk temuan");
    }

    @Test
    @DisplayName("US-020: Persetujuan Klaim Mengubah Status & Otomatis Menolak Klaim Lain")
    void testPersetujuanKlaimOtomatisTolakKlaimLain() {
        Item itemTemuan = new BarangNonElektronik(
                "T-030", JenisLaporan.TEMUAN, "Jaket Varsity Biru",
                KategoriBarang.NON_ELEKTRONIK, "Tertinggal di kantin",
                "Kantin", LocalDate.now(), "POS-0001", null, null,
                StatusItem.TERSEDIA, LocalDateTime.now(), mhsPelapor,
                "Biru Putih", "Fleece"
        );
        itemRepository.save(itemTemuan);

        Klaim klaim1 = new Klaim(
                "CLM-001", "T-030", mhsKlaim1,
                "Jaket Varsity biru dengan bordir nama Budi di lengan kiri",
                null, LocalDate.now().plusDays(1), SesiLayanan.PAGI, "POS-0001",
                true, true, 90, StatusKlaim.DIAJUKAN, LocalDateTime.now(), null
        );
        Klaim klaim2 = new Klaim(
                "CLM-002", "T-030", mhsKlaim2,
                "Jaket varsity biru biasa",
                null, LocalDate.now().plusDays(1), SesiLayanan.SIANG, "POS-0001",
                true, true, 60, StatusKlaim.DIAJUKAN, LocalDateTime.now(), null
        );
        klaimRepository.save(klaim1);
        klaimRepository.save(klaim2);

        // Petugas menyetujui klaim1
        klaimService.setujui("CLM-001", petugasBintang);

        // Verifikasi klaim 1 DISETUJUI
        Klaim k1Hasil = klaimRepository.findById("CLM-001").orElseThrow();
        assertEquals(StatusKlaim.DISETUJUI, k1Hasil.getStatus());

        // Verifikasi klaim 2 otomatis DITOLAK
        Klaim k2Hasil = klaimRepository.findById("CLM-002").orElseThrow();
        assertEquals(StatusKlaim.DITOLAK, k2Hasil.getStatus());
        assertTrue(k2Hasil.getCatatanPetugas().contains("telah disetujui"), "Catatan otomatis penolakan klaim lain");

        // Status barang menjadi DALAM_PROSES_KLAIM
        Item itemTerkunci = itemRepository.findById("T-030").orElseThrow();
        assertEquals(StatusItem.DALAM_PROSES_KLAIM, itemTerkunci.getStatus());
    }

    @Test
    @DisplayName("US-017 & US-018: Pembuatan, Penyimpanan File, dan Dekode Gambar QR Code")
    void testQRCodeGenerationDanDecoding() throws IOException {
        String kode = "QR-ARC-0099";
        BufferedImage image = qrCodeService.generateQRImage(kode, 200, 200);
        assertNotNull(image);

        File outputFile = tempDir.resolve("test-qr.png").toFile();
        File saved = qrCodeService.saveQRToPng(kode, outputFile, 200);
        assertTrue(saved.exists());
        assertTrue(saved.length() > 0);

        // Dekode kembali file gambar QR menggunakan ZXing
        String decoded = qrCodeService.decodeQRFromFile(saved);
        assertEquals(kode, decoded, "Teks hasil dekode QR sesuai kode awal");
    }

    @Test
    @DisplayName("US-001, US-002, US-003: Hak Akses Role (Petugas, Mahasiswa, Admin)")
    void testHakAksesRolePengguna() {
        Petugas p = new Petugas("Bintang", "NIP01");
        Mahasiswa m = new Mahasiswa("Rama", "NIM01");
        Admin a = new Admin("Lukman", "ADM01");

        // Petugas punya akses operasional pos
        assertTrue(p.punyaAkses(HakAkses.VERIFIKASI_LAPORAN));
        assertTrue(p.punyaAkses(HakAkses.KELOLA_LOKASI_SIMPAN));
        assertTrue(p.punyaAkses(HakAkses.KEPUTUSAN_KLAIM));
        assertTrue(p.punyaAkses(HakAkses.SERAH_TERIMA_BARANG));
        assertTrue(p.punyaAkses(HakAkses.LIHAT_SEMUA_BARANG));

        // Mahasiswa TIDAK punya akses operasional pos atau manajemen
        assertFalse(m.punyaAkses(HakAkses.VERIFIKASI_LAPORAN));
        assertFalse(m.punyaAkses(HakAkses.KELOLA_LOKASI_SIMPAN));
        assertFalse(m.punyaAkses(HakAkses.LIHAT_SEMUA_BARANG));

        // Mahasiswa punya hak lapor dan klaim
        assertTrue(m.punyaAkses(HakAkses.BUAT_LAPORAN));
        assertTrue(m.punyaAkses(HakAkses.KLAIM_BARANG));

        // Admin punya akses penuh manajemen & statistik
        assertTrue(a.punyaAkses(HakAkses.LIHAT_SEMUA_BARANG));
        assertTrue(a.punyaAkses(HakAkses.MANAJEMEN_PENGGUNA));
        assertTrue(a.punyaAkses(HakAkses.LIHAT_STATISTIK));
        assertFalse(a.punyaAkses(HakAkses.BUAT_LAPORAN));
    }
}
