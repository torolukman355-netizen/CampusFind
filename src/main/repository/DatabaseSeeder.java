package com.campusfind.repository;

import com.campusfind.model.Admin;
import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Notification;
import com.campusfind.model.NotificationType;
import com.campusfind.model.Petugas;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.SesiLayanan;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mengisi data awal (seeding) untuk pos, pengguna, item (beserta gambar di database),
 * klaim, dan notifikasi agar sesuai dengan tampilan Figma CampusFind.
 */
public final class DatabaseSeeder {

    private static final Logger LOG = Logger.getLogger(DatabaseSeeder.class.getName());

    private DatabaseSeeder() {
    }

    public static void seedIfEmpty(PosRepository posRepo,
                                  ItemRepository itemRepo,
                                  KlaimRepository klaimRepo,
                                  NotificationRepository notifRepo,
                                  UserRepository userRepo) {
        seedPos(posRepo);
        seedUsers(userRepo);
        seedItemsAndImages(posRepo, itemRepo, userRepo);
        seedKlaim(itemRepo, klaimRepo, userRepo);
        seedNotifications(notifRepo);
    }

    private static void seedPos(PosRepository posRepo) {
        if (posRepo == null || !posRepo.findAll().isEmpty()) {
            return;
        }
        posRepo.save(new PosPenyimpanan("POS-0001", "Pos Satpam Rektorat", "Pos Kustodi Sentral - Rektorat Sayap Barat Lt. 1", "POS REKTORAT"));
        posRepo.save(new PosPenyimpanan("POS-0002", "Pos Satpam Perpustakaan", "Perpustakaan Lt. 1 Sayap Selatan", "POS PERPUSTAKAAN"));
        posRepo.save(new PosPenyimpanan("POS-0003", "Pos Satpam Gedung B", "Gedung Kuliah Bersama B Lt. 1", "POS GEDUNG B"));
        posRepo.save(new PosPenyimpanan("POS-0004", "Pos Satpam Parkiran Barat", "Pos Jaga Parkir Barat Dekat Gerbang 2", "POS PARKIRAN"));
        LOG.info("Seeder: 4 Pos Penyimpanan berhasil disimpan.");
    }

    private static void seedUsers(UserRepository userRepo) {
        if (userRepo == null) {
            return;
        }
        if (userRepo.findAll().isEmpty()) {
            userRepo.save(new Mahasiswa("Budi Santoso", "123456789", "budi@student.kampus.ac", "password123", true));
            userRepo.save(new Mahasiswa("Dewi Lestari", "987654321", "dewi@student.kampus.ac", "password123", true));
            userRepo.save(new Mahasiswa("Fajar Nugroho", "1122334455", "fajar@student.kampus.ac", "password123", true));
            userRepo.save(new Petugas("Rina Wulandari", "198501012010122001", "rina@kampus.ac", "password123", true));
            userRepo.save(new Petugas("Andi Pratama", "198703152012111002", "andi@kampus.ac", "password123", false));
            userRepo.save(new Admin("Hendra Admin", "admin", "hendra@kampus.ac", "admin123", true));
            LOG.info("Seeder: 6 akun pengguna awal berhasil disimpan.");
        }
    }

    private static void seedItemsAndImages(PosRepository posRepo, ItemRepository itemRepo, UserRepository userRepo) {
        if (itemRepo == null) {
            return;
        }

        // Jika item sudah ada, pastikan item yang belum punya foto_bytes diperbarui dari file gambar
        List<Item> existingItems = itemRepo.findAll();
        if (!existingItems.isEmpty()) {
            Map<String, String> imageFileMap = Map.of(
                    "ARC-0001", "0001_laptop_asus.jpg",
                    "ARC-0002", "0002_dompet_kulit.jpg",
                    "ARC-0003", "0003_tas_ransel.jpg",
                    "ARC-0004", "0004_earbuds_putih.jpg",
                    "ARC-0005", "0005_kunci_motor.jpg",
                    "ARC-0006", "0006_botol_minum.jpg",
                    "ARC-0007", "0007_jaket_denim.jpg",
                    "ARC-0008", "0008_kalkulator_casio.jpg"
            );

            for (Item item : existingItems) {
                if (!item.hasFotoBytes() && imageFileMap.containsKey(item.getId())) {
                    byte[] bytes = readImageBytes(imageFileMap.get(item.getId()));
                    if (bytes != null) {
                        item.setFotoBytes(bytes);
                        itemRepo.save(item);
                        LOG.info("Seeder: Foto bytes diperbarui untuk item " + item.getId());
                    }
                }
            }
            return;
        }

        var mhsBudi = userRepo.findById("123456789")
                .orElseGet(() -> new Mahasiswa("Budi Santoso", "123456789", "budi@student.kampus.ac", "password123", true));
        var mhsDewi = userRepo.findById("987654321")
                .orElseGet(() -> new Mahasiswa("Dewi Lestari", "987654321", "dewi@student.kampus.ac", "password123", true));
        var petugasRina = userRepo.findById("198501012010122001")
                .orElseGet(() -> new Petugas("Rina Wulandari", "198501012010122001", "rina@kampus.ac", "password123", true));
        var petugasAndi = userRepo.findById("198703152012111002")
                .orElseGet(() -> new Petugas("Andi Pratama", "198703152012111002", "andi@kampus.ac", "password123", false));

        // 1. Laptop hitam ASUS (Temuan, Cocok)
        BarangElektronik item1 = new BarangElektronik(
                "ARC-0001",
                JenisLaporan.TEMUAN,
                "Laptop hitam ASUS",
                KategoriBarang.ELEKTRONIK,
                "Ditemukan laptop hitam merk ASUS tertinggal di atas meja baca dekat jendela perpustakaan lantai 2.",
                "Perpustakaan Lt. 2",
                LocalDate.of(2025, 5, 12),
                "POS-0001",
                "images/0001_laptop_asus.jpg",
                "Stiker bintang di sisi kiri, wallpaper kucing oranye, nomor seri R9N0CV124821",
                StatusItem.COCOK,
                LocalDateTime.of(2025, 5, 12, 14, 45),
                petugasRina,
                "ASUS",
                "R9N0CV124821"
        );
        item1.setFotoBytes(readImageBytes("0001_laptop_asus.jpg"));
        itemRepo.save(item1);

        // 2. Dompet kulit coklat (Hilang, Diverifikasi)
        BarangNonElektronik item2 = new BarangNonElektronik(
                "ARC-0002",
                JenisLaporan.HILANG,
                "Dompet kulit coklat",
                KategoriBarang.NON_ELEKTRONIK,
                "Dompet lipat pria bahan kulit warna coklat tua, berisi KTM, SIM, dan beberapa lembar uang.",
                "Kantin Pusat",
                LocalDate.of(2025, 5, 10),
                null,
                "images/0002_dompet_kulit.jpg",
                "KTM atas nama Dewi Lestari, ada kartu perpustakaan",
                StatusItem.DIVERIFIKASI,
                LocalDateTime.of(2025, 5, 10, 11, 20),
                mhsDewi,
                "Coklat",
                "Kulit"
        );
        item2.setFotoBytes(readImageBytes("0002_dompet_kulit.jpg"));
        itemRepo.save(item2);

        // 3. Tas ransel Eiger hijau (Hilang, Dilaporkan)
        BarangNonElektronik item3 = new BarangNonElektronik(
                "ARC-0003",
                JenisLaporan.HILANG,
                "Tas ransel Eiger hijau",
                KategoriBarang.NON_ELEKTRONIK,
                "Tas ransel Eiger warna hijau lumut tertinggal di bawah kursi setelah perkuliahan.",
                "Gedung B, R. 204",
                LocalDate.of(2025, 5, 10),
                null,
                "images/0003_tas_ransel.jpg",
                "Ada gantungan alpaka kecil di resleting saku depan",
                StatusItem.DILAPORKAN,
                LocalDateTime.of(2025, 5, 10, 15, 0),
                mhsDewi,
                "Hijau",
                "Kanvas"
        );
        item3.setFotoBytes(readImageBytes("0003_tas_ransel.jpg"));
        itemRepo.save(item3);

        // 4. Earbuds putih + case (Temuan, Diverifikasi)
        BarangElektronik item4 = new BarangElektronik(
                "ARC-0004",
                JenisLaporan.TEMUAN,
                "Earbuds putih + case",
                KategoriBarang.ELEKTRONIK,
                "Ditemukan earbuds TWS warna putih beserta charging case di atas meja dosen.",
                "Gedung A, R. 101",
                LocalDate.of(2025, 5, 9),
                "POS-0001",
                "images/0004_earbuds_putih.jpg",
                "Ada goresan kecil di bagian engsel case",
                StatusItem.DIVERIFIKASI,
                LocalDateTime.of(2025, 5, 9, 16, 10),
                petugasAndi,
                "Samsung",
                "SM-R177"
        );
        item4.setFotoBytes(readImageBytes("0004_earbuds_putih.jpg"));
        itemRepo.save(item4);

        // 5. Kunci motor Honda (Temuan, Dilaporkan)
        BarangNonElektronik item5 = new BarangNonElektronik(
                "ARC-0005",
                JenisLaporan.TEMUAN,
                "Kunci motor Honda",
                KategoriBarang.NON_ELEKTRONIK,
                "Ditemukan kunci motor Honda dengan gantungan kain merah di area parkiran barat.",
                "Parkiran Barat",
                LocalDate.of(2025, 5, 9),
                "POS-0004",
                "images/0005_kunci_motor.jpg",
                "Ada gantungan kain merah bersulam tulisan Honda dan remote alarm",
                StatusItem.DILAPORKAN,
                LocalDateTime.of(2025, 5, 9, 8, 30),
                mhsBudi,
                "Hitam & Merah",
                "Logam & Kain"
        );
        item5.setFotoBytes(readImageBytes("0005_kunci_motor.jpg"));
        itemRepo.save(item5);

        // 6. Botol minum biru (Hilang, Dikembalikan)
        BarangNonElektronik item6 = new BarangNonElektronik(
                "ARC-0006",
                JenisLaporan.HILANG,
                "Botol minum biru",
                KategoriBarang.NON_ELEKTRONIK,
                "Tumbler stainless warna biru merk Tupperware tertinggal di pinggir lapangan basket.",
                "Lapangan Olahraga",
                LocalDate.of(2025, 4, 28),
                null,
                "images/0006_botol_minum.jpg",
                "Merk Tupperware, ada goresan melingkar di tutup botol",
                StatusItem.DIKEMBALIKAN,
                LocalDateTime.of(2025, 4, 28, 17, 15),
                mhsBudi,
                "Biru",
                "Stainless Steel"
        );
        item6.setFotoBytes(readImageBytes("0006_botol_minum.jpg"));
        itemRepo.save(item6);

        // 7. Jaket denim biru (Temuan, Diklaim)
        BarangNonElektronik item7 = new BarangNonElektronik(
                "ARC-0007",
                JenisLaporan.TEMUAN,
                "Jaket denim biru",
                KategoriBarang.NON_ELEKTRONIK,
                "Jaket jeans denim warna biru pudar tertinggal di kursi aula setelah acara seminar.",
                "Aula Utama",
                LocalDate.of(2025, 5, 6),
                "POS-0001",
                "images/0007_jaket_denim.jpg",
                "Merk Levi's ukuran L, ada pin kecil di kerah kiri",
                StatusItem.DIKLAIM,
                LocalDateTime.of(2025, 5, 6, 13, 0),
                petugasRina,
                "Biru Denim",
                "Denim"
        );
        item7.setFotoBytes(readImageBytes("0007_jaket_denim.jpg"));
        itemRepo.save(item7);

        // 8. Kalkulator Casio fx-991 (Hilang, Diverifikasi)
        BarangElektronik item8 = new BarangElektronik(
                "ARC-0008",
                JenisLaporan.HILANG,
                "Kalkulator Casio fx-991",
                KategoriBarang.ELEKTRONIK,
                "Kalkulator scientific Casio fx-991EX warna hitam tertinggal setelah ujian kalkulus.",
                "Gedung C, R. 301",
                LocalDate.of(2025, 5, 5),
                null,
                "images/0008_kalkulator_casio.jpg",
                "Di balik tutup ada coretan spidol kecil inisial BS",
                StatusItem.DIVERIFIKASI,
                LocalDateTime.of(2025, 5, 5, 12, 10),
                mhsBudi,
                "Casio",
                "FX-991EX"
        );
        item8.setFotoBytes(readImageBytes("0008_kalkulator_casio.jpg"));
        itemRepo.save(item8);

        // 9. Laporan kehilangan Laptop ASUS Vivobook oleh Budi (Laporan Mahasiswa di dashboard)
        BarangElektronik item9 = new BarangElektronik(
                "ARC-0009",
                JenisLaporan.HILANG,
                "Laptop ASUS Vivobook",
                KategoriBarang.ELEKTRONIK,
                "Laptop hitam dengan stiker bintang di sisi kiri, tertinggal di meja dekat jendela perpustakaan.",
                "Perpustakaan Lt. 2",
                LocalDate.of(2025, 5, 12),
                null,
                "images/0001_laptop_asus.jpg",
                "Stiker bintang di sisi kiri, wallpaper kucing oranye, nomor seri R9N0CV124821",
                StatusItem.COCOK,
                LocalDateTime.of(2025, 5, 12, 14, 30),
                mhsBudi,
                "ASUS",
                "R9N0CV124821"
        );
        item9.setFotoBytes(readImageBytes("0001_laptop_asus.jpg"));
        itemRepo.save(item9);

        LOG.info("Seeder: 9 item dan foto berhasil disimpan ke database.");
    }

    private static void seedKlaim(ItemRepository itemRepo, KlaimRepository klaimRepo, UserRepository userRepo) {
        if (klaimRepo == null || !klaimRepo.findAll().isEmpty()) {
            return;
        }

        var mhsBudi = userRepo.findById("123456789")
                .orElseGet(() -> new Mahasiswa("Budi Santoso", "123456789", "budi@student.kampus.ac", "password123", true));
        var mhsFajar = userRepo.findById("1122334455")
                .orElseGet(() -> new Mahasiswa("Fajar Nugroho", "1122334455", "fajar@student.kampus.ac", "password123", true));

        // Klaim 1 oleh Budi untuk Laptop hitam ASUS (skor 92)
        Klaim klaim1 = new Klaim(
                "CLM-0001",
                "ARC-0001",
                mhsBudi,
                "Stiker bintang di sisi kiri, wallpaper kucing oranye, no. seri berakhiran 4821. Terlampir foto nota pembelian.",
                "nota_asus.jpg",
                LocalDate.of(2025, 5, 13),
                SesiLayanan.PAGI,
                "POS-0001",
                true,
                true,
                92,
                StatusKlaim.DIAJUKAN,
                LocalDateTime.of(2025, 5, 12, 16, 30),
                null
        );
        klaimRepo.save(klaim1);

        LOG.info("Seeder: Klaim awal berhasil disimpan ke database.");
    }

    private static void seedNotifications(NotificationRepository notifRepo) {
        if (notifRepo == null || !notifRepo.findByPenerima("123456789").isEmpty()) {
            return;
        }

        notifRepo.save(new Notification(
                "NTF-0001",
                "123456789",
                NotificationType.KANDIDAT_COCOK,
                "Kandidat cocok ditemukan untuk Laptop ASUS.",
                "Sistem mendeteksi kecocokan 92% antara laporan kehilangan Anda dengan temuan di Perpustakaan Lt. 2.",
                "ARC-0001",
                LocalDateTime.now().minusMinutes(5),
                false
        ));

        notifRepo.save(new Notification(
                "NTF-0002",
                "123456789",
                NotificationType.KLAIM_DITERIMA,
                "Laporan Dompet kulit telah diverifikasi.",
                "Laporan kehilangan dompet kulit coklat Anda telah diverifikasi oleh petugas Rina Wulandari.",
                "ARC-0002",
                LocalDateTime.now().minusHours(2),
                false
        ));

        notifRepo.save(new Notification(
                "NTF-0003",
                "123456789",
                NotificationType.KLAIM_DISETUJUI,
                "Barang Botol minum berhasil dikembalikan.",
                "Serah terima botol minum biru telah selesai dan dicatat dalam sistem.",
                "ARC-0006",
                LocalDateTime.now().minusDays(1),
                false
        ));

        notifRepo.save(new Notification(
                "NTF-0004",
                "123456789",
                NotificationType.KLAIM_DITERIMA,
                "Laporan Kunci motor diterima.",
                "Laporan temuan kunci motor Honda Anda di Parkiran Barat telah diterima oleh sistem.",
                "ARC-0005",
                LocalDateTime.now().minusDays(3),
                true
        ));

        LOG.info("Seeder: Notifikasi awal untuk Budi Santoso berhasil disimpan ke database.");
    }

    private static byte[] readImageBytes(String filename) {
        File[] candidates = new File[]{
                new File("data/images/" + filename),
                new File("data/" + filename),
                new File(filename)
        };
        for (File candidate : candidates) {
            if (candidate.exists() && candidate.isFile()) {
                try {
                    return Files.readAllBytes(candidate.toPath());
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Gagal membaca file gambar " + candidate.getAbsolutePath(), e);
                }
            }
        }
        return null;
    }
}
