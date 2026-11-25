package com.campusfind.repository;

import com.campusfind.exception.StorageException;
import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Petugas;
import com.campusfind.model.User;
import com.campusfind.model.Notification;
import com.campusfind.model.NotificationType;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.Role;
import com.campusfind.model.SesiLayanan;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Pengujian integrasi repository JDBC terhadap PostgreSQL.
 * <p>
 * Tes otomatis dilewati bila server PostgreSQL tidak tersedia. Database yang
 * dipakai terpisah dari database aplikasi ({@code campusfind_test}).
 */
@DisplayName("Pengujian repository JDBC PostgreSQL")
class JdbcRepositoryTest {

    private static DatabaseManager manager;
    private static boolean tersedia;

    @BeforeAll
    static void siapkanDatabase() {
        Properties config = new Properties();
        config.setProperty("db.url", env("CAMPUSFIND_TEST_DB_URL",
                "jdbc:postgresql://localhost:5432/campusfind_test"));
        config.setProperty("db.user", env("CAMPUSFIND_TEST_DB_USER", "campusfind"));
        config.setProperty("db.password", env("CAMPUSFIND_TEST_DB_PASSWORD", "campusfind"));
        manager = new DatabaseManager(config);

        try (Connection ignored = manager.getConnection()) {
            tersedia = true;
        } catch (StorageException e) {
            tersedia = buatDatabaseBilaBelumAda(config);
        } catch (SQLException e) {
            tersedia = false;
        }

        Assumptions.assumeTrue(tersedia,
                "PostgreSQL tidak tersedia; pengujian repository JDBC dilewati.");
        DatabaseSchema.ensureSchema(manager);
    }

    @BeforeEach
    void bersihkanTabel() {
        Assumptions.assumeTrue(tersedia, "PostgreSQL tidak tersedia.");
        try (Connection connection = manager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(
                    "TRUNCATE notifications, klaim, items, pos, app_sequence CASCADE");
        } catch (SQLException e) {
            fail("Gagal membersihkan tabel pengujian: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Pos dan item (elektronik/non-elektronik) tersimpan dan terbaca kembali")
    void testPosDanItem() {
        JdbcPosRepository posRepository = new JdbcPosRepository(manager);
        String posId = posRepository.nextId();
        assertEquals("POS-0001", posId);
        posRepository.save(new PosPenyimpanan(posId, "Pos Rektorat", "Gedung A Lt. 1", "POS A"));
        assertTrue(posRepository.existsByNama("pos rektorat"));
        assertEquals("Gedung A Lt. 1",
                posRepository.findById(posId).orElseThrow().getLokasiPenyerahan());
        assertEquals(1, posRepository.findAll().size());

        JdbcItemRepository itemRepository = new JdbcItemRepository(manager);

        String idElektronik = itemRepository.nextId();
        assertEquals("ARC-0001", idElektronik);
        BarangElektronik be = new BarangElektronik(
                idElektronik, JenisLaporan.TEMUAN, "Laptop Asus", KategoriBarang.ELEKTRONIK,
                "Laptop hitam", "Lab 1", LocalDate.of(2026, 11, 20), posId,
                "data/images/laptop.jpg", "stiker merah", StatusItem.TERSEDIA,
                LocalDateTime.of(2026, 11, 20, 10, 0), new Mahasiswa("Budi", "123456789"),
                "Asus", "SN-123");
        be.setTempatPenitipan("Meja Resepsionis Gedung Utama");
        itemRepository.save(be);

        Item elektronik = itemRepository.findById(idElektronik).orElseThrow();
        assertInstanceOf(BarangElektronik.class, elektronik);
        assertEquals("Asus", ((BarangElektronik) elektronik).getMerek());
        assertEquals("SN-123", ((BarangElektronik) elektronik).getNomorSeri());
        assertEquals("Budi", elektronik.getPelapor().getNama());
        assertEquals(Role.MAHASISWA, elektronik.getPelapor().getRole());
        assertEquals(LocalDate.of(2026, 11, 20), elektronik.getTanggalKejadian());
        assertEquals("Meja Resepsionis Gedung Utama", elektronik.getTempatPenitipan());

        String idNonElektronik = itemRepository.nextId();
        assertEquals("ARC-0002", idNonElektronik);
        itemRepository.save(new BarangNonElektronik(
                idNonElektronik, JenisLaporan.HILANG, "Jaket Hoodie", KategoriBarang.NON_ELEKTRONIK,
                "Jaket abu-abu", "Kantin", LocalDate.of(2026, 11, 21), null,
                null, "ada bordir", StatusItem.DILAPORKAN,
                LocalDateTime.of(2026, 11, 21, 9, 0), new Mahasiswa("Sari", "987654321"),
                "Abu-abu", "Katun"));

        Item nonElektronik = itemRepository.findById(idNonElektronik).orElseThrow();
        assertInstanceOf(BarangNonElektronik.class, nonElektronik);
        assertEquals("Katun", ((BarangNonElektronik) nonElektronik).getBahan());

        itemRepository.updateStatus(idElektronik, StatusItem.DALAM_PROSES_KLAIM);
        assertEquals(1, itemRepository.findByStatus(StatusItem.DALAM_PROSES_KLAIM).size());
        assertEquals(1, itemRepository.findByJenis(JenisLaporan.TEMUAN).size());
        assertEquals(1, itemRepository.search("laptop").size());
        assertEquals(2, itemRepository.findAll().size());
    }

    @Test
    @DisplayName("Klaim tersimpan, status aktif terlacak, dan ID tidak terpakai ulang")
    void testKlaim() {
        String posId = simpanPos();
        String itemId = simpanItem(posId);

        JdbcKlaimRepository klaimRepository = new JdbcKlaimRepository(manager);
        String klaimId = klaimRepository.nextId(2026);
        assertEquals("CLM-2026-0001", klaimId);

        klaimRepository.save(new Klaim(
                klaimId, itemId, new Mahasiswa("Sari", "987654321"), "Ada lecet di sudut",
                "data/bukti/klaim.jpg", LocalDate.of(2026, 11, 24), SesiLayanan.PAGI,
                posId, true, true, 85, StatusKlaim.DIAJUKAN,
                LocalDateTime.of(2026, 11, 24, 9, 0), null));

        Klaim tersimpan = klaimRepository.findById(klaimId).orElseThrow();
        assertEquals(85, tersimpan.getSkorKecocokan());
        assertEquals(StatusKlaim.DIAJUKAN, tersimpan.getStatus());
        assertEquals("Sari", tersimpan.getPengklaim().getNama());
        assertEquals(1, klaimRepository.findAll().size());
        assertEquals(1, klaimRepository.findByPengklaim("987654321").size());
        assertEquals(klaimId,
                klaimRepository.findAktifByItemId(itemId).orElseThrow().getId());

        klaimRepository.updateStatus(klaimId, StatusKlaim.DISETUJUI);
        assertTrue(klaimRepository.findAktifByItemId(itemId).isPresent());
        klaimRepository.updateStatus(klaimId, StatusKlaim.SELESAI);
        assertTrue(klaimRepository.findAktifByItemId(itemId).isEmpty());

        klaimRepository.delete(klaimId);
        assertTrue(klaimRepository.findById(klaimId).isEmpty());
        assertEquals("CLM-2026-0002", klaimRepository.nextId(2026),
                "Nomor klaim yang sudah dipakai tidak boleh dipakai ulang");
        assertEquals("CLM-2027-0001", klaimRepository.nextId(2027));
    }

    @Test
    @DisplayName("Notifikasi tersimpan, dihitung, dan ditandai dibaca sesuai pemilik")
    void testNotifikasi() {
        JdbcNotificationRepository repository = new JdbcNotificationRepository(manager);

        Notification pertama = new Notification(
                "NTF-0001", "123456789", NotificationType.KANDIDAT_COCOK,
                "Kandidat cocok", "Ada barang yang mirip laporanmu", "ARC-0001",
                LocalDateTime.of(2026, 11, 24, 8, 0), false);
        Notification kedua = new Notification(
                "NTF-0002", "123456789", NotificationType.KLAIM_DISETUJUI,
                "Klaim disetujui", "Silakan ambil di pos", "CLM-2026-0001",
                LocalDateTime.of(2026, 11, 24, 9, 0), false);
        Notification orangLain = new Notification(
                "NTF-0003", "555555555", NotificationType.KLAIM_DITOLAK,
                "Klaim ditolak", "Ciri khusus tidak cocok", "CLM-2026-0002",
                LocalDateTime.of(2026, 11, 24, 10, 0), false);

        repository.save(pertama);
        repository.save(kedua);
        repository.save(orangLain);

        assertEquals(2, repository.findByPenerima("123456789").size());
        assertEquals(2, repository.countBelumDibaca("123456789"));
        assertEquals(NotificationType.KANDIDAT_COCOK,
                repository.findById("NTF-0001").orElseThrow().getTipe());

        assertThrows(SecurityException.class,
                () -> repository.markAsRead("NTF-0001", "999999999"));

        repository.markAsRead("NTF-0001", "123456789");
        assertEquals(1, repository.countBelumDibaca("123456789"));

        repository.markAllAsRead("123456789");
        assertEquals(0, repository.countBelumDibaca("123456789"));
        assertEquals(1, repository.countBelumDibaca("555555555"));

        // Menyimpan ulang ID yang sama harus memperbarui, bukan menggandakan.
        repository.save(pertama);
        assertEquals(2, repository.findByPenerima("123456789").size());
    }

    @Test
    @DisplayName("Penyimpanan dan pencarian akun pengguna di PostgreSQL")
    void testPengguna() {
        Assumptions.assumeTrue(tersedia, "PostgreSQL tidak tersedia, tes dilewati.");

        JdbcUserRepository repository = new JdbcUserRepository(manager);
        Mahasiswa mhs = new Mahasiswa("Budi Santoso", "123456789", "budi@student.kampus.ac", "rahasia123", true);
        repository.save(mhs);

        User ditemukan = repository.findById("123456789").orElseThrow();
        assertEquals("Budi Santoso", ditemukan.getNama());
        assertEquals("budi@student.kampus.ac", ditemukan.getEmail());
        assertEquals(Role.MAHASISWA, ditemukan.getRole());
        assertTrue(ditemukan.isAktif());

        User dariEmail = repository.findByEmail("budi@student.kampus.ac").orElseThrow();
        assertEquals("123456789", dariEmail.getNomorIdentitas());

        // Menyimpan user lain dengan email yang sama harus gagal
        Petugas duplikat = new Petugas("Andi", "198501012010122001", "budi@student.kampus.ac", "pwd", true);
        assertThrows(StorageException.class, () -> repository.save(duplikat));

        // Hapus
        assertTrue(repository.deleteById("123456789"));
        assertTrue(repository.findById("123456789").isEmpty());
    }

    @Test
    @DisplayName("Penyimpanan bytea gambar item dan seeder database")
    void testItemFotoBytesDanSeeder() {
        JdbcPosRepository posRepo = new JdbcPosRepository(manager);
        JdbcItemRepository itemRepo = new JdbcItemRepository(manager);
        JdbcKlaimRepository klaimRepo = new JdbcKlaimRepository(manager);
        JdbcNotificationRepository notifRepo = new JdbcNotificationRepository(manager);
        JdbcUserRepository userRepo = new JdbcUserRepository(manager);

        DatabaseSeeder.seedIfEmpty(posRepo, itemRepo, klaimRepo, notifRepo, userRepo);

        List<Item> items = itemRepo.findAll();
        assertTrue(items.size() >= 8, "Harus ada minimal 8 item hasil seeder");

        Item laptop = itemRepo.findById("ARC-0001").orElseThrow();
        assertTrue(laptop.hasFotoBytes(), "Item ARC-0001 harus memiliki foto_bytes dari database");
        assertTrue(laptop.getFotoBytes().length > 0);

        List<Notification> notifs = notifRepo.findByPenerima("123456789");
        assertFalse(notifs.isEmpty(), "Notifikasi untuk Budi Santoso harus terisi");
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private static String simpanPos() {
        JdbcPosRepository repository = new JdbcPosRepository(manager);
        String posId = repository.nextId();
        repository.save(new PosPenyimpanan(posId, "Pos Rektorat", "Gedung A", "POS A"));
        return posId;
    }

    private static String simpanItem(String posId) {
        JdbcItemRepository repository = new JdbcItemRepository(manager);
        String itemId = repository.nextId();
        repository.save(new BarangElektronik(
                itemId, JenisLaporan.TEMUAN, "HP Samsung", KategoriBarang.ELEKTRONIK,
                "HP biru", "Perpustakaan", LocalDate.of(2026, 11, 22), posId,
                null, "ada retak", StatusItem.TERSEDIA,
                LocalDateTime.of(2026, 11, 22, 11, 0), new Mahasiswa("Budi", "123456789"),
                "Samsung", "SN-999"));
        return itemId;
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static boolean buatDatabaseBilaBelumAda(Properties config) {
        String url = config.getProperty("db.url", "");
        if (!url.startsWith("jdbc:postgresql://")) {
            return false;
        }
        try {
            URI uri = URI.create(url.substring("jdbc:".length()));
            String path = uri.getPath();
            if (path == null || path.length() < 2) {
                return false;
            }
            String namaDatabase = path.substring(1);
            if (!namaDatabase.matches("[A-Za-z0-9_]+")) {
                return false;
            }
            String adminUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + "/postgres";
            Properties adminConfig = new Properties();
            adminConfig.setProperty("user", config.getProperty("db.user"));
            adminConfig.setProperty("password", config.getProperty("db.password"));
            try (Connection connection = DriverManager.getConnection(adminUrl, adminConfig);
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE \"" + namaDatabase + "\"");
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
