package com.campusfind.service;

import com.campusfind.model.HakAkses;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.StatusItem;
import com.campusfind.model.User;
import com.campusfind.model.UserAccount;
import com.campusfind.model.UserAccount.RoleType;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.PosRepository;
import com.campusfind.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Layanan bisnis untuk Role Admin (Lukman):
 * - Autentikasi akun (menolak akun nonaktif)
 * - US-022: Manajemen Akun Pengguna (CRUD, validasi email unik, sandi min 8 karakter, proteksi diri)
 * - US-023 & US-031: Layanan & Visualisasi Statistik (Return Rate, Rata-rata Waktu Penyelesaian, Grafik Kategori/Bulan)
 * - Master Data Pos Penyimpanan
 */
public class AdminService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PosRepository posRepository;

    public record StatistikRingkasan(
            int totalLaporan,
            int totalHilang,
            int totalTemuan,
            int totalHilangTerverifikasi,
            int totalDikembalikan,
            int totalDiarsipkan,
            double returnRatePersen,
            double rataRataJamPenyelesaian,
            Map<String, Integer> perKategori,
            Map<String, Integer> perBulan,
            Map<String, Integer> perJenis
    ) {}

    public AdminService(UserRepository userRepository,
                        ItemRepository itemRepository,
                        PosRepository posRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.posRepository = posRepository;
        seedAkunAwalJikaKosong();
    }

    public void seedAkunAwalJikaKosong() {
        if (!userRepository.findAll().isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        // Anggota Kelompok 4 + Akun Demo Mahasiswa
        userRepository.save(new UserAccount("USR-001", "Bintang Pamungkas", "251402082",
                "bintang@campusfind.ac.id", "bintang123", RoleType.SECURITY, true, now));
        userRepository.save(new UserAccount("USR-002", "Muhammad Fakhry Adrian", "251402053",
                "fakhry@campusfind.ac.id", "fakhry123", RoleType.SECURITY, true, now));
        userRepository.save(new UserAccount("USR-003", "Jevine Jeje", "251402085",
                "jevine@campusfind.ac.id", "jevine123", RoleType.CLEANING_SERVICE, true, now));
        userRepository.save(new UserAccount("USR-004", "Muhammad Lukman Toro", "251402105",
                "lukman@campusfind.ac.id", "lukman123", RoleType.ADMIN, true, now));
        userRepository.save(new UserAccount("USR-005", "Nadia Stevany", "251402073",
                "nadia@students.usu.ac.id", "nadia1234", RoleType.MAHASISWA, true, now));
        userRepository.save(new UserAccount("USR-006", "Fitya Shofi Ahmad", "251402132",
                "fitya@students.usu.ac.id", "fitya1234", RoleType.MAHASISWA, true, now));
        userRepository.save(new UserAccount("USR-007", "Budi Santoso", "230001003",
                "budi.santoso@students.usu.ac.id", "budi12345", RoleType.MAHASISWA, true, now));
    }

    public UserAccount authenticate(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email dan kata sandi wajib diisi.");
        }
        UserAccount acc = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityException("Email tidak terdaftar dalam sistem."));
        if (!acc.isAktif()) {
            throw new SecurityException("Akun Anda telah dinonaktifkan oleh Admin. Hubungi administrator.");
        }
        if (!acc.getPassword().equals(password)) {
            throw new SecurityException("Kata sandi tidak sesuai.");
        }
        return acc;
    }

    public List<UserAccount> getAllUsers(User aktor) {
        validasiManajemenUser(aktor);
        return userRepository.findAll();
    }

    public List<UserAccount> getAllDemoAccounts() {
        return userRepository.findAll();
    }

    public UserAccount tambahPengguna(String nama, String nomorIdentitas, String email,
                                      String password, RoleType role, User aktor) {
        validasiManajemenUser(aktor);
        if (nama == null || nama.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama pengguna wajib diisi.");
        }
        if (nomorIdentitas == null || nomorIdentitas.trim().isEmpty()) {
            throw new IllegalArgumentException("NIM / NIP wajib diisi.");
        }
        if (email == null || !email.contains("@") || email.trim().length() < 5) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }
        if (userRepository.existsByEmail(email.trim())) {
            throw new IllegalArgumentException("Email '" + email.trim() + "' sudah terdaftar. Gunakan email unik.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Kata sandi minimal harus 8 karakter.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role pengguna wajib dipilih.");
        }

        UserAccount acc = new UserAccount(
                userRepository.nextId(),
                nama.trim(),
                nomorIdentitas.trim(),
                email.trim().toLowerCase(),
                password,
                role,
                true,
                LocalDateTime.now()
        );
        userRepository.save(acc);
        return acc;
    }

    public UserAccount ubahRolePengguna(String userId, RoleType roleBaru, User aktor) {
        validasiManajemenUser(aktor);
        if (roleBaru == null) {
            throw new IllegalArgumentException("Role baru tidak boleh kosong.");
        }
        UserAccount acc = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Akun pengguna tidak ditemukan."));
        acc.setRole(roleBaru);
        userRepository.save(acc);
        return acc;
    }

    public UserAccount toggleAktifPengguna(String userId, boolean aktifBaru, User aktor) {
        validasiManajemenUser(aktor);
        UserAccount acc = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Akun pengguna tidak ditemukan."));
        if (!aktifBaru && aktor != null && acc.getNomorIdentitas().equals(aktor.getNomorIdentitas())) {
            throw new IllegalStateException("Proteksi Diri: Admin tidak diperbolehkan menonaktifkan akunnya sendiri!");
        }
        acc.setAktif(aktifBaru);
        userRepository.save(acc);
        return acc;
    }

    public PosPenyimpanan tambahPos(String nama, String lokasiPenyerahan, String jamLayanan, User aktor) {
        if (aktor == null || !aktor.punyaAkses(HakAkses.KELOLA_MASTER_DATA)) {
            throw new SecurityException("Akses Ditolak: Hanya Admin yang dapat mengelola master data pos.");
        }
        if (nama == null || nama.isBlank() || lokasiPenyerahan == null || lokasiPenyerahan.isBlank()) {
            throw new IllegalArgumentException("Nama pos dan lokasi penyerahan wajib diisi.");
        }
        if (posRepository.existsByNama(nama.trim())) {
            throw new IllegalArgumentException("Pos dengan nama tersebut sudah terdaftar.");
        }
        PosPenyimpanan pos = new PosPenyimpanan(
                posRepository.nextId(),
                nama.trim(),
                lokasiPenyerahan.trim(),
                jamLayanan != null && !jamLayanan.isBlank() ? jamLayanan.trim() : "08:00 - 17:00"
        );
        posRepository.save(pos);
        return pos;
    }

    /**
     * US-023 & US-031: Menghitung seluruh metrik statistik sistem, Return Rate, dan durasi rata-rata penyelesaian.
     * Menangani data kosong dengan aman (tidak error / division by zero).
     */
    public StatistikRingkasan hitungStatistik() {
        List<Item> all = itemRepository.findAll();
        int totalLaporan = all.size();
        int totalHilang = 0;
        int totalTemuan = 0;
        int totalHilangTerverifikasi = 0;
        int totalDikembalikan = 0;
        int totalDiarsipkan = 0;

        long totalJamSelesai = 0;
        int countDurasiSelesai = 0;

        Map<String, Integer> perKategori = new LinkedHashMap<>();
        for (KategoriBarang k : KategoriBarang.values()) {
            perKategori.put(k.getLabel(), 0);
        }

        Map<String, Integer> perBulan = new LinkedHashMap<>();
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Item item : all) {
            if (item.getJenis() == JenisLaporan.HILANG) {
                totalHilang++;
                if (item.getStatus() != StatusItem.DILAPORKAN && item.getStatus() != StatusItem.DITOLAK) {
                    totalHilangTerverifikasi++;
                }
            } else {
                totalTemuan++;
            }

            if (item.getStatus() == StatusItem.DIKEMBALIKAN || item.getStatus() == StatusItem.DISERAHKAN) {
                totalDikembalikan++;
                if (item.getDibuatPada() != null) {
                    long jam = Math.max(1, Duration.between(item.getDibuatPada(), LocalDateTime.now()).toHours());
                    totalJamSelesai += jam;
                    countDurasiSelesai++;
                }
            }
            if (item.getStatus() == StatusItem.DIARSIPKAN) {
                totalDiarsipkan++;
            }

            if (item.getKategori() != null) {
                perKategori.merge(item.getKategori().getLabel(), 1, Integer::sum);
            }
            if (item.getTanggalKejadian() != null) {
                String bln = item.getTanggalKejadian().format(monthFmt);
                perBulan.merge(bln, 1, Integer::sum);
            }
        }

        double returnRate = (totalHilangTerverifikasi > 0)
                ? Math.min(100.0, (totalDikembalikan * 100.0) / totalHilangTerverifikasi)
                : 0.0;
        double avgJam = (countDurasiSelesai > 0)
                ? (totalJamSelesai * 1.0) / countDurasiSelesai
                : 0.0;

        Map<String, Integer> perJenis = new LinkedHashMap<>();
        perJenis.put("Barang Hilang", totalHilang);
        perJenis.put("Barang Temuan", totalTemuan);

        return new StatistikRingkasan(
                totalLaporan, totalHilang, totalTemuan,
                totalHilangTerverifikasi, totalDikembalikan, totalDiarsipkan,
                Math.round(returnRate * 10.0) / 10.0,
                Math.round(avgJam * 10.0) / 10.0,
                perKategori, perBulan, perJenis
        );
    }

    private void validasiManajemenUser(User aktor) {
        if (aktor == null || !aktor.punyaAkses(HakAkses.MANAJEMEN_PENGGUNA)) {
            throw new SecurityException("Akses Ditolak: Fitur manajemen akun pengguna hanya diperbolehkan untuk role Admin.");
        }
    }
}
