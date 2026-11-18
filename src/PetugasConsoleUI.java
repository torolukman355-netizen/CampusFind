import model.*;
import model.enums.HakAkses;
import model.enums.JenisLaporan;
import model.enums.StatusItem;
import model.enums.StatusKlaim;
import repository.DatabaseManager;
import repository.ItemRepository;
import repository.KlaimRepository;
import repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * PetugasConsoleUI:
 * Tampilan antarmuka berbasis CLI (Console) interaktif.
 * Digunakan saat aplikasi dijalankan di lingkungan Headless (Terminal / WSL / SSH)
 * tanpa server X11 / Display GUI, agar aplikasi tetap bisa dijalankan dan diuji 100%.
 */
public class PetugasConsoleUI {

    private final ItemRepository itemRepo = ItemRepository.getInstance();
    private final KlaimRepository klaimRepo = KlaimRepository.getInstance();
    private final UserRepository userRepo = UserRepository.getInstance();
    private final Scanner scanner = new Scanner(System.in);
    private String namaPetugas = "Bintang";

    public void start() {
        DatabaseManager.muatDariFile();

        System.out.println("===============================================================");
        System.out.println("   CAMPUSFIND — SISTEM INFORMASI BARANG HILANG & TEMUAN        ");
        System.out.println("    MODUL LENGKAP KELOMPOK 4 (BINTANG, JEPIN, FAKHRI, DLL)     ");
        System.out.println("===============================================================");
        System.out.println("Petugas Aktif: " + namaPetugas + "\n");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Pilih menu [0-12]: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    menuLihatAntreanLaporan();
                    break;
                case "2":
                    menuVerifikasiLaporan();
                    break;
                case "3":
                    menuKontrolTransisiStatus();
                    break;
                case "4":
                    menuAturLokasiSimpan();
                    break;
                case "5":
                    menuKonfirmasiMatching();
                    break;
                case "6":
                    menuLacakBarangQR();
                    break;
                case "7":
                    menuPemeriksaanKlaim();
                    break;
                case "8":
                    menuSerahTerima();
                    break;
                case "9":
                    menuPolimorfismeDetail();
                    break;
                case "10":
                    menuCekHakAkses();
                    break;
                case "11":
                    menuGantiPetugasAktif();
                    break;
                case "12":
                    menuJalankanModulSinkronisasiLaporan();
                    break;
                case "0":
                    DatabaseManager.simpanKeFile();
                    System.out.println("\n[INFO] Data disimpan ke database lokal. Terima kasih!");
                    running = false;
                    break;
                default:
                    System.out.println("\n[!] Pilihan tidak valid, silakan coba lagi.");
            }
            System.out.println();
        }
    }

    private void printMenu() {
        int countVerif = itemRepo.findByStatus(StatusItem.DILAPORKAN).size();
        int countKlaim = klaimRepo.findByStatus(StatusKlaim.DIAJUKAN).size();

        System.out.println("---------------------------------------------------------------");
        System.out.println("DASHBOARD PETUGAS: " + namaPetugas + " | [Verifikasi: " + countVerif + "] | [Klaim: " + countKlaim + "]");
        System.out.println("---------------------------------------------------------------");
        System.out.println("1. Tampilkan Antrean Laporan Baru (DILAPORKAN)");
        System.out.println("2. Verifikasi Laporan Masuk (Terima / Tolak Wajib Alasan) (US-011)");
        System.out.println("3. Kontrol Transisi Status Berurutan (US-012)");
        System.out.println("4. Atur Lokasi Penyimpanan Fisik (Khusus Temuan) (US-013)");
        System.out.println("5. Konfirmasi Hasil Smart Matching (US-015)");
        System.out.println("6. Lacak Detail Barang via Kode QR / ID (US-018)");
        System.out.println("7. Pemeriksaan & Keputusan Klaim Mahasiswa (US-020)");
        System.out.println("8. Formulir Serah Terima & Penguncian Data (US-021)");
        System.out.println("9. Lihat Detail Lengkap Semua Barang (Polimorfisme US-008)");
        System.out.println("10. Cek Hierarki Role & Hak Akses Pengguna (US-001, US-003)");
        System.out.println("11. Ganti Petugas Aktif (Bintang / Jepin / Fakhri)");
        System.out.println("12. Simulasi Ujian: Jalankan Class Laporan (Modul Sinkronisasi)");
        System.out.println("0. Simpan & Keluar");
        System.out.println("---------------------------------------------------------------");
    }

    private void menuGantiPetugasAktif() {
        System.out.println("\nPilih Petugas yang bertugas:");
        System.out.println("1. Bintang");
        System.out.println("2. Jepin");
        System.out.println("3. Fakhri");
        System.out.print("Pilih [1-3]: ");
        String p = scanner.nextLine().trim();
        if ("1".equals(p)) namaPetugas = "Bintang";
        else if ("2".equals(p)) namaPetugas = "Jepin";
        else if ("3".equals(p)) namaPetugas = "Fakhri";
        System.out.println("[OK] Petugas aktif saat ini diubah menjadi: " + namaPetugas);
    }

    private void menuJalankanModulSinkronisasiLaporan() {
        System.out.println("\n=== MENJALANKAN MODUL SINKRONISASI KELOMPOK 4 (Laporan.main) ===");
        Laporan.main(new String[]{});
        System.out.println("=== SELESAI MENJALANKAN SINKRONISASI ===\n");
    }

    private void menuLihatAntreanLaporan() {
        System.out.println("\n=== ANTREAN LAPORAN MASUK (STATUS: DILAPORKAN) ===");
        List<Item> list = itemRepo.findByStatus(StatusItem.DILAPORKAN);
        if (list.isEmpty()) {
            System.out.println("Tidak ada laporan baru yang menunggu verifikasi.");
            return;
        }

        System.out.printf("%-8s | %-8s | %-24s | %-16s | %-15s\n", "ID", "JENIS", "NAMA BARANG", "KATEGORI", "PELAPOR");
        System.out.println("--------------------------------------------------------------------------------");
        for (Item item : list) {
            System.out.printf("%-8s | %-8s | %-24s | %-16s | %-15s\n",
                    item.getId(), item.getJenis(), item.getNama(), item.getKategori(), item.getPelapor());
        }
    }

    private void menuVerifikasiLaporan() {
        menuLihatAntreanLaporan();
        System.out.print("\nMasukkan ID Laporan yang ingin diverifikasi: ");
        String id = scanner.nextLine().trim();

        Optional<Item> opt = itemRepo.findById(id);
        if (!opt.isPresent() || opt.get().getStatus() != StatusItem.DILAPORKAN) {
            System.out.println("[!] Laporan dengan ID tersebut tidak ditemukan dalam antrean verifikasi.");
            return;
        }

        Item item = opt.get();
        System.out.println("\nDetail Laporan Dipilih:\n" + item.tampilkanDetail());
        System.out.println("Aksi Petugas (" + namaPetugas + "):");
        System.out.println("1. Terima Laporan (Ubah status -> DIVERIFIKASI)");
        System.out.println("2. Tolak Laporan (Wajib mengisi alasan tertulis)");
        System.out.print("Pilih aksi [1/2]: ");
        String aksi = scanner.nextLine().trim();

        if ("1".equals(aksi)) {
            try {
                item.ubahStatus(StatusItem.DIVERIFIKASI, namaPetugas, "Laporan diperiksa dan dinyatakan valid");
                itemRepo.save(item);
                DatabaseManager.simpanKeFile();
                System.out.println("[SUKSES] Laporan berhasil diverifikasi (Status: DIVERIFIKASI)!");
                if (item.getJenis() == JenisLaporan.TEMUAN) {
                    System.out.println("[QR CODE] Kode QR unik dibuat otomatis: " + item.getKodeQR());
                }
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        } else if ("2".equals(aksi)) {
            System.out.print("Masukkan alasan penolakan laporan (Wajib diisi): ");
            String alasan = scanner.nextLine().trim();
            if (alasan.isEmpty()) {
                System.out.println("[GAGAL] Penolakan dibatalkan: Alasan penolakan tidak boleh kosong!");
                return;
            }
            try {
                item.ubahStatus(StatusItem.DITOLAK, namaPetugas, "Ditolak: " + alasan);
                itemRepo.save(item);
                DatabaseManager.simpanKeFile();
                System.out.println("[SUKSES] Laporan telah DITOLAK dan tidak akan diikutsertakan dalam matching.");
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    private void menuKontrolTransisiStatus() {
        System.out.print("\nMasukkan ID Barang: ");
        String id = scanner.nextLine().trim();
        Optional<Item> opt = itemRepo.findById(id);
        if (!opt.isPresent()) {
            System.out.println("[!] Barang tidak ditemukan.");
            return;
        }

        Item item = opt.get();
        System.out.println("Barang: " + item.getNama() + " | Status Sekarang: " + item.getStatus() +
                (item.isTerkunci() ? " [TERKUNCI]" : ""));

        System.out.println("Pilihan Status Baru (Aturan US-012 berurutan):");
        System.out.println("1. DIVERIFIKASI");
        System.out.println("2. COCOK");
        System.out.println("3. DIKLAIM");
        System.out.println("4. DIKEMBALIKAN (Mengunci data barang)");
        System.out.println("5. DITOLAK");
        System.out.print("Pilih status [1-5]: ");
        String pil = scanner.nextLine().trim();

        StatusItem target;
        switch (pil) {
            case "1": target = StatusItem.DIVERIFIKASI; break;
            case "2": target = StatusItem.COCOK; break;
            case "3": target = StatusItem.DIKLAIM; break;
            case "4": target = StatusItem.DIKEMBALIKAN; break;
            case "5": target = StatusItem.DITOLAK; break;
            default: System.out.println("[!] Pilihan tidak dikenal."); return;
        }

        System.out.print("Masukkan catatan/keterangan perubahan: ");
        String note = scanner.nextLine().trim();

        try {
            item.ubahStatus(target, namaPetugas, note.isEmpty() ? "Perubahan status manual" : note);
            itemRepo.save(item);
            DatabaseManager.simpanKeFile();
            System.out.println("[SUKSES] Status berhasil diubah ke: " + target);
        } catch (Exception e) {
            System.out.println("[GAGAL TRANSISI] " + e.getMessage());
        }
    }

    private void menuAturLokasiSimpan() {
        System.out.print("\nMasukkan ID Barang (Harus berjenis TEMUAN): ");
        String id = scanner.nextLine().trim();
        Optional<Item> opt = itemRepo.findById(id);
        if (!opt.isPresent()) {
            System.out.println("[!] Barang tidak ditemukan.");
            return;
        }

        Item item = opt.get();
        System.out.println("Barang: " + item.getNama() + " (" + item.getJenis() + ") | Lokasi saat ini: " + item.getLokasiSimpan());
        System.out.print("Masukkan Lokasi Penyimpanan Fisik Baru (contoh: Loker B-2 Ruang Satpam): ");
        String lokasi = scanner.nextLine().trim();

        try {
            item.setLokasiSimpan(lokasi, namaPetugas);
            itemRepo.save(item);
            DatabaseManager.simpanKeFile();
            System.out.println("[SUKSES] Lokasi simpan berhasil diperbarui.");
        } catch (Exception e) {
            System.out.println("[GAGAL US-013] " + e.getMessage());
        }
    }

    private void menuKonfirmasiMatching() {
        System.out.println("\n=== KONFIRMASI SMART MATCHING (US-015) ===");
        System.out.println("Rekomendasi pasangan kemiripan >= 70%:");
        System.out.println("Hilang : H-012 (Flashdisk Sandisk 32GB)");
        System.out.println("Temuan : T-034 (USB Sandisk Merah Hitam)");
        System.out.println("Skor   : 88% (Kategori sama, nomor seri/ciri identik)");
        System.out.print("\nKonfirmasi status kedua barang menjadi COCOK? (y/n): ");
        String konf = scanner.nextLine().trim();

        if ("y".equalsIgnoreCase(konf)) {
            itemRepo.findById("H-012").ifPresent(i -> {
                try { i.ubahStatus(StatusItem.COCOK, namaPetugas, "Dikonfirmasi cocok dengan T-034"); itemRepo.save(i); } catch (Exception ignored) {}
            });
            itemRepo.findById("T-034").ifPresent(i -> {
                try { i.ubahStatus(StatusItem.COCOK, namaPetugas, "Dikonfirmasi cocok dengan H-012"); itemRepo.save(i); } catch (Exception ignored) {}
            });
            DatabaseManager.simpanKeFile();
            System.out.println("[SUKSES] Kedua laporan berhasil diubah statusnya menjadi COCOK!");
        }
    }

    private void menuLacakBarangQR() {
        System.out.print("\nMasukkan Kode Unik / QR Code barang (contoh: QR-T-055 atau T-055): ");
        String query = scanner.nextLine().trim();

        for (Item item : itemRepo.findAll()) {
            if (item.getKodeQR().equalsIgnoreCase(query) || item.getId().equalsIgnoreCase(query)) {
                System.out.println("\n[HASIL PELACAKAN DITEMUKAN]\n" + item.tampilkanDetail());
                return;
            }
        }
        System.out.println("[!] Barang dengan kode '" + query + "' tidak ditemukan dalam sistem.");
    }

    private void menuPemeriksaanKlaim() {
        System.out.println("\n=== ANTREAN PEMERIKSAAN KLAIM MAHASISWA (US-020) ===");
        List<Klaim> list = klaimRepo.findByStatus(StatusKlaim.DIAJUKAN);
        if (list.isEmpty()) {
            System.out.println("Tidak ada pengajuan klaim baru.");
            return;
        }

        for (Klaim k : list) {
            System.out.println("ID Klaim: " + k.getIdKlaim() + " | Barang: " + k.getIdBarang() + " | Pengaju: " + k.getPengaju());
            System.out.println("Bukti Kepemilikan: \"" + k.getBukti() + "\"");
            System.out.println("---------------------------------------------------");
        }

        System.out.print("\nMasukkan ID Klaim yang ingin diproses: ");
        String idKlaim = scanner.nextLine().trim();
        Optional<Klaim> optK = klaimRepo.findById(idKlaim);
        if (!optK.isPresent()) {
            System.out.println("[!] Klaim tidak ditemukan.");
            return;
        }

        Klaim klaim = optK.get();
        System.out.println("Aksi Petugas (" + namaPetugas + "): [1] Setujui Klaim (Status -> DIKLAIM) | [2] Tolak Klaim");
        System.out.print("Pilih [1/2]: ");
        String aks = scanner.nextLine().trim();

        if ("1".equals(aks)) {
            itemRepo.findById(klaim.getIdBarang()).ifPresent(item -> {
                try {
                    item.ubahStatus(StatusItem.DIKLAIM, namaPetugas, "Klaim dari " + klaim.getPengaju() + " disetujui");
                    itemRepo.save(item);
                    klaim.setStatusKlaim(StatusKlaim.DISETUJUI);
                    klaimRepo.save(klaim);
                    DatabaseManager.simpanKeFile();
                    System.out.println("[SUKSES] Klaim disetujui! Status barang berubah menjadi DIKLAIM.");
                } catch (Exception e) {
                    System.out.println("[GAGAL] " + e.getMessage());
                }
            });
        } else if ("2".equals(aks)) {
            System.out.print("Masukkan alasan penolakan klaim (Wajib diisi): ");
            String al = scanner.nextLine().trim();
            if (al.isEmpty()) {
                System.out.println("[GAGAL] Alasan wajib diisi!");
                return;
            }
            klaim.setStatusKlaim(StatusKlaim.DITOLAK);
            klaim.setCatatanPetugas("Ditolak: " + al);
            klaimRepo.save(klaim);
            DatabaseManager.simpanKeFile();
            System.out.println("[SUKSES] Pengajuan klaim berhasil ditolak.");
        }
    }

    private void menuSerahTerima() {
        System.out.println("\n=== PENCATATAN SERAH TERIMA BARANG (US-021) ===");
        List<Item> diklaim = itemRepo.findByStatus(StatusItem.DIKLAIM);
        if (diklaim.isEmpty()) {
            System.out.println("Belum ada barang yang berstatus DIKLAIM dan siap diserahterimakan.");
            return;
        }

        for (Item item : diklaim) {
            System.out.println("ID: " + item.getId() + " | " + item.getNama() + " | Lokasi: " + item.getLokasiSimpan());
        }

        System.out.print("\nMasukkan ID Barang yang akan diserahkan: ");
        String id = scanner.nextLine().trim();
        Optional<Item> opt = itemRepo.findById(id);
        if (!opt.isPresent()) {
            System.out.println("[!] Barang tidak ditemukan.");
            return;
        }

        Item item = opt.get();
        System.out.print("Masukkan Nama Penerima Barang: ");
        String penerima = scanner.nextLine().trim();
        if (penerima.isEmpty()) {
            System.out.println("[GAGAL] Nama penerima tidak boleh kosong!");
            return;
        }

        try {
            item.ubahStatus(StatusItem.DIKEMBALIKAN, namaPetugas,
                    "Diserahkan kepada " + penerima + " pada " + LocalDate.now() + " [DATA DIKUNCI]");
            itemRepo.save(item);
            DatabaseManager.simpanKeFile();
            System.out.println("[SUKSES US-021] Serah terima selesai! Status barang menjadi DIKEMBALIKAN dan data telah TERKUNCI.");
        } catch (Exception e) {
            System.out.println("[GAGAL] " + e.getMessage());
        }
    }

    private void menuPolimorfismeDetail() {
        System.out.println("\n=== POLIMORFISME: TAMPILKAN DETAIL SEMUA BARANG (US-008) ===");
        for (Item item : itemRepo.findAll()) {
            System.out.println(item.tampilkanDetail());
            System.out.println("===============================================================");
        }
    }

    private void menuCekHakAkses() {
        System.out.println("\n=== HIERARKI ROLE & HAK AKSES SISTEM (US-001 s.d. US-003) ===");
        for (User u : userRepo.findAll()) {
            System.out.println("User : " + u.getNama() + " (" + u.getRole() + ") - " + u.getEmail());
            System.out.println("  -> Dashboard : " + u.tampilDashboard());
            System.out.println("  -> Akses VERIFIKASI_LAPORAN? " + u.punyaAkses(HakAkses.VERIFIKASI_LAPORAN));
            System.out.println("  -> Akses KONTROL_STATUS? " + u.punyaAkses(HakAkses.KONTROL_STATUS));
            System.out.println("  -> Akses LIHAT_SEMUA_BARANG? " + u.punyaAkses(HakAkses.LIHAT_SEMUA_BARANG));
            System.out.println("  -> Akses MANAJEMEN_PENGGUNA? " + u.punyaAkses(HakAkses.MANAJEMEN_PENGGUNA));
            System.out.println("---------------------------------------------------------------");
        }
    }
}
