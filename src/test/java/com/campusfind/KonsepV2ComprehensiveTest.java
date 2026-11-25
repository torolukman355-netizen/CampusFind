package com.campusfind;

import com.campusfind.model.Admin;
import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.CleaningService;
import com.campusfind.model.HakAkses;
import com.campusfind.model.Item;
import com.campusfind.model.JenisBukti;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.Laporan;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.MatchResult;
import com.campusfind.model.Pelapor;
import com.campusfind.model.SesiLayanan;
import com.campusfind.model.Security;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.model.StatusLaporan;
import com.campusfind.model.UserAccount;
import com.campusfind.model.Verifikator;
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.FileItemRepository;
import com.campusfind.repository.FileKlaimRepository;
import com.campusfind.repository.FileNotificationRepository;
import com.campusfind.repository.FilePosRepository;
import com.campusfind.repository.FileRepositoryProvider;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.repository.NotificationRepository;
import com.campusfind.repository.PosRepository;
import com.campusfind.repository.RepositoryProvider;
import com.campusfind.service.AdminService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.MatchingEngine;
import com.campusfind.service.NotificationService;
import com.campusfind.service.PetugasService;
import com.campusfind.service.QRCodeService;
import com.campusfind.service.TokenOverlapStrategy;
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

@DisplayName("Pengujian Lengkap Arsitektur Dokumen Konsep Versi 2.0")
class KonsepV2ComprehensiveTest {

    @TempDir
    Path tempDir;

    private ItemRepository itemRepository;
    private KlaimRepository klaimRepository;
    private PosRepository posRepository;
    private NotificationRepository notificationRepository;
    private NotificationService notificationService;
    private PetugasService petugasService;
    private KlaimService klaimService;

    private Security securityBintang;
    private CleaningService csJevine;
    private Admin adminLukman;
    private Mahasiswa mhsNadia;

    @BeforeEach
    void setUp() {
        FileDataStore dataStore = new FileDataStore(tempDir);
        itemRepository = new FileItemRepository(dataStore);
        klaimRepository = new FileKlaimRepository(dataStore);
        posRepository = new FilePosRepository(dataStore);
        notificationRepository = new FileNotificationRepository(dataStore);

        notificationService = new NotificationService(notificationRepository);
        QRCodeService qrCodeService = new QRCodeService();
        petugasService = new PetugasService(itemRepository, posRepository, notificationService, qrCodeService);
        klaimService = new KlaimService(
                klaimRepository, itemRepository, posRepository,
                notificationRepository, new TokenOverlapStrategy(), Clock.systemDefaultZone()
        );

        securityBintang = new Security("Bintang Pamungkas", "251402082");
        csJevine = new CleaningService("Jevine Jeje", "251402085");
        adminLukman = new Admin("Muhammad Lukman Toro", "251402105");
        mhsNadia = new Mahasiswa("Nadia Stevany", "251402073");
    }

    @Test
    @DisplayName("v2.0 Section 2.2 & 3.1: Hierarki User, Interface Pelapor vs Verifikator & Least Privilege")
    void testHierarkiUserDanLeastPrivilege() {
        // Semua warga kampus mengimplementasikan Pelapor
        assertTrue(mhsNadia instanceof Pelapor);
        assertTrue(securityBintang instanceof Pelapor);
        assertTrue(csJevine instanceof Pelapor);
        assertTrue(adminLukman instanceof Pelapor);

        // Hanya Security mengimplementasikan Verifikator
        assertTrue(securityBintang instanceof Verifikator);
        assertFalse((Object) csJevine instanceof Verifikator);
        assertFalse((Object) mhsNadia instanceof Verifikator);

        // Hak akses CS hanya lapor & serahkan ke pos, bukan verifikasi klaim
        assertTrue(csJevine.punyaAkses(HakAkses.BUAT_LAPORAN));
        assertTrue(csJevine.punyaAkses(HakAkses.SERAHKAN_BARANG_KE_POS));
        assertFalse(csJevine.punyaAkses(HakAkses.VERIFIKASI_LAPORAN));
        assertFalse(csJevine.punyaAkses(HakAkses.KEPUTUSAN_KLAIM));

        Item temuanCS = new BarangNonElektronik(
                "T-CS-01", JenisLaporan.TEMUAN, "Dompet Hitam",
                KategoriBarang.TAS_DOMPET, "Ditemukan di ruang kelas",
                "Gedung A", LocalDate.now(), "POS-0001", null, "Stiker biru",
                StatusItem.DILAPORKAN, LocalDateTime.now(), csJevine, "Hitam", "Kulit"
        );
        petugasService.laporkanTemuanLapangan(temuanCS, csJevine);
        petugasService.serahkanBarangKePos("T-CS-01", csJevine, "Loker Pos Utama");

        // CS harus ditolak jika mencoba memverifikasi laporan (SecurityException)
        assertThrows(SecurityException.class, () ->
                petugasService.verifikasiLaporan("T-CS-01", true, null, csJevine));

        // Security berhasil memverifikasi laporan
        petugasService.verifikasiLaporan("T-CS-01", true, null, securityBintang);
        assertEquals(StatusItem.TERSEDIA, itemRepository.findById("T-CS-01").orElseThrow().getStatus());
    }

    @Test
    @DisplayName("v2.0 Section 3.2: Polimorfisme getSyaratBuktiUtama() Elektronik vs Non-Elektronik")
    void testPolimorfismeSyaratBuktiKepemilikan() {
        Item laptop = new BarangElektronik(
                "E-01", JenisLaporan.TEMUAN, "Laptop Asus",
                KategoriBarang.ELEKTRONIK, "Tertinggal di lab",
                "Lab 1", LocalDate.now(), "POS-0001", null, "Goresan sudut",
                StatusItem.TERSEDIA, LocalDateTime.now(), securityBintang,
                "Asus", "SN-123", "Vivobook", "nadia@students.usu.ac.id"
        );
        Item dompet = new BarangNonElektronik(
                "N-01", JenisLaporan.TEMUAN, "Dompet Kulit",
                KategoriBarang.TAS_DOMPET, "Tertinggal di kantin",
                "Kantin", LocalDate.now(), "POS-0001", null, "Berisi KTM",
                StatusItem.TERSEDIA, LocalDateTime.now(), csJevine,
                "Hitam", "Kulit"
        );

        JenisBukti[] syaratLaptop = laptop.getSyaratBuktiUtama();
        assertEquals(3, syaratLaptop.length);
        assertEquals(JenisBukti.OTP_AKUN_TERDAFTAR, syaratLaptop[0]);
        assertTrue(laptop.tampilkanDetail().contains("na***@students.usu.ac.id"));

        JenisBukti[] syaratDompet = dompet.getSyaratBuktiUtama();
        assertEquals(1, syaratDompet.length);
        assertEquals(JenisBukti.CIRI_FISIK, syaratDompet[0]);
    }

    @Test
    @DisplayName("v2.0 Section 2.6 & 3.3: Komposisi Laporan & Perlindungan Privasi tampilkanKePublik()")
    void testLaporanKomposisiDanPrivasiPublik() {
        Item item = new BarangNonElektronik(
                "T-PRIV-1", JenisLaporan.TEMUAN, "Tas Ransel Eiger",
                KategoriBarang.TAS_DOMPET, "Tertinggal di musholla",
                "Musholla Kampus", LocalDate.now(), "POS-0001", null, "Gantungan kunci merah",
                StatusItem.DILAPORKAN, LocalDateTime.now(), mhsNadia, "Hitam", "Kanvas"
        );
        Laporan laporan = Laporan.dariItem(item);
        assertEquals(StatusLaporan.DILAPORKAN, laporan.getStatus());
        assertEquals(mhsNadia, laporan.getPelapor());

        String publik = laporan.tampilkanKePublik();
        assertFalse(publik.contains("Nadia Stevany"), "Nama pelapor harus disembunyikan pada tampilan publik!");
        assertTrue(publik.contains("Tas Ransel Eiger"));
        assertTrue(publik.contains("Musholla Kampus"));
    }

    @Test
    @DisplayName("v2.0 Section 3.4: MatchingEngine Pra-Filter <= 7 Hari & Scoring")
    void testMatchingEnginePraFilterDanSkoring() {
        MatchingEngine engine = new MatchingEngine(notificationService);

        Item hilang = new BarangElektronik(
                "H-10", JenisLaporan.HILANG, "Laptop Asus Vivobook Hitam",
                KategoriBarang.ELEKTRONIK, "Hilang di Ruang Seminar A",
                "Gedung Fasilkom", LocalDate.now(), null, null, "Stiker biru",
                StatusItem.DICARI, LocalDateTime.now(), mhsNadia, "Asus", "SN-XYZ"
        );
        Item temuanCocok = new BarangElektronik(
                "T-10", JenisLaporan.TEMUAN, "Laptop Asus Vivobook Hitam",
                KategoriBarang.ELEKTRONIK, "Ditemukan di Ruang Seminar A",
                "Gedung Fasilkom", LocalDate.now().minusDays(2), "POS-0001", null, "Stiker biru",
                StatusItem.TERSEDIA, LocalDateTime.now(), csJevine, "Asus", "SN-XYZ"
        );
        Item temuanTerlaluLama = new BarangElektronik(
                "T-11", JenisLaporan.TEMUAN, "Laptop Asus Vivobook Hitam",
                KategoriBarang.ELEKTRONIK, "Ditemukan di Ruang Seminar A",
                "Gedung Fasilkom", LocalDate.now().minusDays(15), "POS-0001", null, "Stiker biru",
                StatusItem.TERSEDIA, LocalDateTime.now(), csJevine, "Asus", "SN-OLD"
        );

        Laporan lapH = Laporan.dariItem(hilang);
        Laporan lapTCocok = Laporan.dariItem(temuanCocok);
        Laporan lapTLama = Laporan.dariItem(temuanTerlaluLama);

        // Lolos Pra-filter (<= 7 hari, kategori sama)
        assertTrue(engine.praFilter(lapH, lapTCocok));
        assertTrue(engine.hitungSkor(lapH, lapTCocok) >= 70);

        // Gagal Pra-filter (> 7 hari) -> Skor otomatis 0
        assertFalse(engine.praFilter(lapH, lapTLama));
        assertEquals(0, engine.hitungSkor(lapH, lapTLama));

        List<MatchResult> hasil = engine.cariRekomendasi(lapH, List.of(lapTCocok, lapTLama));
        assertEquals(1, hasil.size());
        assertEquals("T-10", hasil.get(0).getItemTemuanId());
    }

    @Test
    @DisplayName("v2.0 Section 3.5: Challenge-Response OTP Verifikasi Akun Terdaftar & Pengarsipan > 60 Hari")
    void testOtpChallengeResponseDanPengarsipanBarangLama() {
        Item laptop = new BarangElektronik(
                "T-EL-99", JenisLaporan.TEMUAN, "MacBook Air M2",
                KategoriBarang.ELEKTRONIK, "Tertinggal di perpustakaan",
                "Perpustakaan", LocalDate.now(), "POS-0001", null, "Goresan belakang",
                StatusItem.TERSEDIA, LocalDateTime.now(), csJevine,
                "Apple", "SN-MBA-09", "M2", "nadia@students.usu.ac.id"
        );
        itemRepository.save(laptop);

        Klaim klaim = new Klaim(
                "CLM-V2-001", "T-EL-99", mhsNadia,
                "MacBook Air M2 akun terdaftar nadia@students.usu.ac.id",
                null, LocalDate.now().plusDays(1), SesiLayanan.PAGI, "POS-0001",
                true, true, 92, StatusKlaim.DIAJUKAN, LocalDateTime.now(), null
        );
        klaimRepository.save(klaim);

        // 1. Kirim tantangan OTP (hanya Security yang boleh)
        assertThrows(SecurityException.class, () -> klaimService.kirimTantanganOTP("CLM-V2-001", csJevine));
        String otpDikirim = klaimService.kirimTantanganOTP("CLM-V2-001", securityBintang);
        assertNotNull(otpDikirim);
        assertEquals(6, otpDikirim.length());

        // 2. Uji OTP salah vs OTP benar
        assertFalse(klaimService.verifikasiTantanganOTP("CLM-V2-001", "000000"));
        assertTrue(klaimService.verifikasiTantanganOTP("CLM-V2-001", otpDikirim));
        assertTrue(klaimRepository.findById("CLM-V2-001").orElseThrow().isOtpTerverifikasi());

        // 3. Pengarsipan barang lama (> 60 hari) oleh Admin / Security
        Item barangLama = new BarangNonElektronik(
                "T-OLD-01", JenisLaporan.TEMUAN, "Payung Lipat",
                KategoriBarang.LAINNYA, "Tertinggal 70 hari lalu",
                "Halte Kampus", LocalDate.now().minusDays(70), "POS-0001", null, "Polos",
                StatusItem.TERSEDIA, LocalDateTime.now().minusDays(70), csJevine, "Biru", "Nilon"
        );
        itemRepository.save(barangLama);

        int jmlDiarsipkan = petugasService.arsipkanBarangLama(60, adminLukman);
        assertEquals(1, jmlDiarsipkan);
        assertEquals(StatusItem.DIARSIPKAN, itemRepository.findById("T-OLD-01").orElseThrow().getStatus());
        assertTrue(itemRepository.findById("T-OLD-01").orElseThrow().isTerkunci());
    }

    @Test
    @DisplayName("US-022, US-023 & SQL-Ready RepositoryProvider: Manajemen Akun, Proteksi Diri & Statistik Kosong")
    void testAdminServiceManajemenAkunDanStatistik() {
        RepositoryProvider provider = new FileRepositoryProvider(tempDir);
        AdminService adminService = new AdminService(provider.users(), provider.items(), provider.pos());

        // Data kosong -> returnRate & rata-rata aman (0.0 tanpa exception / division by zero)
        AdminService.StatistikRingkasan emptyStats = adminService.hitungStatistik();
        assertEquals(0, emptyStats.totalLaporan());
        assertEquals(0.0, emptyStats.returnRatePersen());

        // Blokir non-admin mengakses manajemen user
        assertThrows(SecurityException.class, () -> adminService.getAllUsers(mhsNadia));
        assertThrows(SecurityException.class, () -> adminService.getAllUsers(securityBintang));

        // Validasi email unik & sandi minimal 8 karakter
        assertThrows(IllegalArgumentException.class, () ->
                adminService.tambahPengguna("Test", "111", "baru@usu.ac.id", "123", UserAccount.RoleType.MAHASISWA, adminLukman));
        assertThrows(IllegalArgumentException.class, () ->
                adminService.tambahPengguna("Test", "111", "bintang@campusfind.ac.id", "password123", UserAccount.RoleType.SECURITY, adminLukman));

        // Tambah akun baru sukses & bisa login
        UserAccount created = adminService.tambahPengguna("Petugas Baru", "999888", "baru@campusfind.ac.id", "rahasia123", UserAccount.RoleType.SECURITY, adminLukman);
        assertNotNull(adminService.authenticate("baru@campusfind.ac.id", "rahasia123"));

        // Nonaktifkan akun -> ditolak saat login
        adminService.toggleAktifPengguna(created.getId(), false, adminLukman);
        assertThrows(SecurityException.class, () -> adminService.authenticate("baru@campusfind.ac.id", "rahasia123"));

        // Proteksi diri: Admin tidak boleh menonaktifkan akunnya sendiri
        UserAccount selfLukman = provider.users().findByNomorIdentitas(adminLukman.getNomorIdentitas()).orElseThrow();
        assertThrows(IllegalStateException.class, () -> adminService.toggleAktifPengguna(selfLukman.getId(), false, adminLukman));
    }
}
