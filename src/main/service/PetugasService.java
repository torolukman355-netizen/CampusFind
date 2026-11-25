package com.campusfind.service;

import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.NotificationType;
import com.campusfind.model.Petugas;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.model.User;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.repository.PosRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service pusat untuk seluruh fungsionalitas Role Petugas (Bintang, Jepin, Fakhri):
 * - US-010, US-028: Dashboard Antrean Kerja Petugas
 * - US-011, US-028: Verifikasi Laporan Masuk
 * - US-012: Kontrol Transisi Status Berurutan & Audit Logging
 * - US-013: Manajemen Lokasi Penyimpanan Fisik (Khusus Temuan)
 * - US-015: Konfirmasi Status Kecocokan Smart Matching
 * - US-017, US-018, US-029: Manajemen & Pelacakan QR Code Barang
 * - US-020, US-028: Pemeriksaan & Keputusan Klaim
 * - US-021, US-029: Pencatatan Serah Terima Barang & Penguncian Data
 */
public class PetugasService {

    private final ItemRepository itemRepository;
    private final PosRepository posRepository;
    private final NotificationService notificationService;
    private final QRCodeService qrCodeService;

    private final Map<String, String> qrCodeMap = new ConcurrentHashMap<>();
    private final Map<String, String> lokasiSimpanMap = new ConcurrentHashMap<>();
    private final Map<String, List<String>> riwayatStatusMap = new ConcurrentHashMap<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PetugasService(ItemRepository itemRepository,
                          PosRepository posRepository,
                          NotificationService notificationService,
                          QRCodeService qrCodeService) {
        this.itemRepository = itemRepository;
        this.posRepository = posRepository;
        this.notificationService = notificationService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * US-011 & US-028: Verifikasi Laporan Masuk dari Mahasiswa.
     * Terima: status menjadi DIVERIFIKASI (TERSEDIA/DICARI), generate QR Code jika Temuan.
     * Tolak: status menjadi DITOLAK/DISERAHKAN, wajib menyertakan alasan tertulis.
     */
    public void verifikasiLaporan(String itemId, boolean terima, String alasanTolak, Petugas aktor) {
        validasiPetugas(aktor);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Barang dengan ID " + itemId + " tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new IllegalStateException("Data barang [" + itemId + "] sudah dikembalikan dan terkunci permanen.");
        }

        if (terima) {
            StatusItem statusBaru = (item.getJenis() == JenisLaporan.TEMUAN)
                    ? StatusItem.TERSEDIA
                    : StatusItem.DICARI;
            item.setStatus(statusBaru);
            itemRepository.save(item);
            catatLog(itemId, "DIVERIFIKASI (" + statusBaru.getLabel() + ")", aktor.getNama());

            if (item.getJenis() == JenisLaporan.TEMUAN) {
                String kodeQr = qrCodeService.generateUniqueCode(item.getId());
                qrCodeMap.put(item.getId(), kodeQr);
            }

            kirimNotifPelapor(item, "Laporan Diverifikasi",
                    "Laporan '" + item.getNama() + "' telah diverifikasi oleh Petugas " + aktor.getNama() + ".");
        } else {
            if (alasanTolak == null || alasanTolak.trim().isEmpty()) {
                throw new IllegalArgumentException("Alasan penolakan laporan wajib diisi!");
            }
            catatLog(itemId, "DITOLAK: " + alasanTolak.trim(), aktor.getNama());
            item.setStatus(StatusItem.DISERAHKAN);
            itemRepository.save(item);
            kirimNotifPelapor(item, "Laporan Ditolak",
                    "Laporan '" + item.getNama() + "' ditolak. Alasan: " + alasanTolak.trim());
        }
    }

    /**
     * US-012: Kontrol Transisi Status Berurutan.
     * DILAPORKAN -> DIVERIFIKASI -> COCOK -> DIKLAIM -> DIKEMBALIKAN
     */
    public void ubahStatusBarang(String itemId, StatusItem statusBaru, Petugas aktor) {
        validasiPetugas(aktor);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Barang dengan ID " + itemId + " tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new IllegalStateException("Barang [" + itemId + "] sudah berstatus akhir (DIKEMBALIKAN) dan datanya TERKUNCI.");
        }
        if (!item.getStatus().bolehBerubahKe(statusBaru)) {
            throw new IllegalStateException("Transisi status tidak valid: dari " + item.getStatus() + " ke " + statusBaru);
        }

        item.setStatus(statusBaru);
        itemRepository.save(item);
        catatLog(itemId, "Status berubah menjadi: " + statusBaru.name(), aktor.getNama());
    }

    /**
     * US-013: Atur atau perbarui Lokasi Penyimpanan Fisik Barang.
     * HANYA berlaku untuk barang berjenis TEMUAN. Sistem menolak keras jika diaplikasikan pada barang Hilang.
     */
    public void aturLokasiPenyimpanan(String itemId, String lokasiFisik, Petugas aktor) {
        validasiPetugas(aktor);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Barang tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new IllegalStateException("Data barang terkunci dan tidak dapat diubah lagi.");
        }
        if (item.getJenis() != JenisLaporan.TEMUAN) {
            throw new IllegalArgumentException("Lokasi penyimpanan fisik HANYA dapat diatur untuk barang berjenis TEMUAN!");
        }
        if (lokasiFisik == null || lokasiFisik.trim().isEmpty()) {
            throw new IllegalArgumentException("Lokasi penyimpanan fisik tidak boleh kosong.");
        }

        lokasiSimpanMap.put(itemId, lokasiFisik.trim());
        catatLog(itemId, "Lokasi simpan fisik diatur ke: " + lokasiFisik.trim(), aktor.getNama());
    }

    /**
     * US-021 & US-029: Pencatatan Serah Terima Barang & Penguncian Data.
     * Mengubah status menjadi DIKEMBALIKAN dan mengunci data secara permanen.
     */
    public void prosesSerahTerima(String itemId, String namaPenerima, LocalDate tanggalSerah, Petugas aktor) {
        validasiPetugas(aktor);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Barang tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new IllegalStateException("Barang ini sudah diserahterimakan dan terkunci permanen.");
        }
        if (item.getStatus() != StatusItem.DALAM_PROSES_KLAIM && item.getStatus() != StatusItem.DIKLAIM) {
            throw new IllegalStateException("Serah terima hanya dapat diproses untuk barang berstatus DIKLAIM / DALAM PROSES KLAIM.");
        }
        if (namaPenerima == null || namaPenerima.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama penerima barang wajib diisi.");
        }

        item.setStatus(StatusItem.DIKEMBALIKAN);
        itemRepository.save(item);
        catatLog(itemId, "DIKEMBALIKAN kepada " + namaPenerima.trim() + " pada " + tanggalSerah + " [DATA TERKUNCI]", aktor.getNama());

        kirimNotifPelapor(item, "Barang Dikembalikan",
                "Barang '" + item.getNama() + "' telah selesai diserahterimakan kepada " + namaPenerima.trim() + ".");
    }

    /**
     * US-018 & US-029: Pencarian & Pelacakan Barang via QR Code.
     * Menerima kode QR unik (misal: "QR-ARC-0001") atau ID Barang langsung.
     */
    public Item cariBarangByQR(String kodeQr) {
        if (kodeQr == null || kodeQr.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode QR tidak boleh kosong.");
        }
        String target = kodeQr.trim();
        String normalizedTarget = target.startsWith("QR-") ? target.substring(3).trim() : target;

        for (Item item : itemRepository.findAll()) {
            String kodeItem = getKodeQR(item);
            if (kodeItem.equalsIgnoreCase(target)
                    || item.getId().equalsIgnoreCase(target)
                    || item.getId().equalsIgnoreCase(normalizedTarget)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Barang dengan kode QR / ID '" + kodeQr + "' tidak ditemukan dalam sistem.");
    }

    public String getKodeQR(Item item) {
        if (item == null || item.getJenis() != JenisLaporan.TEMUAN) return "-";
        return qrCodeMap.computeIfAbsent(item.getId(), id -> qrCodeService.generateUniqueCode(id));
    }

    public String getLokasiSimpan(Item item) {
        if (item == null) return "-";
        if (lokasiSimpanMap.containsKey(item.getId())) {
            return lokasiSimpanMap.get(item.getId());
        }
        if (item.getPosId() != null) {
            return posRepository.findById(item.getPosId())
                    .map(pos -> pos.getNama() + " (" + pos.getLokasiPenyerahan() + ")")
                    .orElse(item.getPosId());
        }
        return "-";
    }

    public boolean isTerkunci(Item item) {
        if (item == null) return false;
        return item.isTerkunci();
    }

    public String getRiwayatLog(String itemId) {
        List<String> logs = riwayatStatusMap.get(itemId);
        if (logs == null || logs.isEmpty()) {
            return "• Belum ada catatan riwayat perubahan status oleh petugas.";
        }
        StringBuilder sb = new StringBuilder();
        for (String log : logs) {
            sb.append("• ").append(log).append("\n");
        }
        return sb.toString().trim();
    }

    private void catatLog(String itemId, String aksi, String pelaku) {
        riwayatStatusMap.computeIfAbsent(itemId, k -> new ArrayList<>())
                .add("[" + LocalDateTime.now().format(dtf) + "] " + aksi + " (oleh " + pelaku + ")");
    }

    private void validasiPetugas(Petugas aktor) {
        if (aktor == null) {
            throw new SecurityException("Akses Ditolak: Aksi ini hanya dapat dilakukan oleh Petugas berwenang.");
        }
    }

    private void kirimNotifPelapor(Item item, String judul, String pesan) {
        if (notificationService != null && item.getPelapor() != null && item.getPelapor().getNomorIdentitas() != null) {
            notificationService.kirim(
                    item.getPelapor().getNomorIdentitas(),
                    NotificationType.KLAIM_DISETUJUI,
                    judul,
                    pesan,
                    item.getId()
            );
        }
    }

    /**
     * Memastikan data simulasi lengkap tersedia saat dashboard dibuka pertama kali.
     * Mencakup item Hilang/Temuan dengan status DILAPORKAN, DIVERIFIKASI, COCOK, DIKLAIM, & DIKEMBALIKAN.
     */
    public void pastikanDataAwalLengkap(KlaimRepository klaimRepo) {
        if (itemRepository.findAll().size() >= 3) {
            return;
        }

        User mhsRama = new Mahasiswa("Rama Aditya", "230001001");
        User mhsAgnes = new Mahasiswa("Agnes Monica", "230001002");
        User mhsBudi = new Mahasiswa("Budi Santoso", "230001003");
        User mhsSiti = new Mahasiswa("Siti Nurhaliza", "230001004");

        // 1. Laporan Temuan Baru (DILAPORKAN) -> Siap diverifikasi di Tab 1
        Item t101 = new BarangElektronik(
                "T-101", JenisLaporan.TEMUAN, "iPhone 13 Midnight 128GB",
                KategoriBarang.ELEKTRONIK, "Ditemukan di bangku panjang Gedung Kuliah Bersama Lt. 2",
                "Gedung A Lt. 2", LocalDate.now(), "POS-0001", null, "Case bening sudut retak",
                StatusItem.DILAPORKAN, LocalDateTime.now().minusHours(3), mhsBudi,
                "Apple", "IMEI-3599201992"
        );
        itemRepository.save(t101);
        catatLog(t101.getId(), "DILAPORKAN oleh Mahasiswa", "Sistem");

        // 2. Laporan Hilang Baru (DILAPORKAN) -> Siap diverifikasi di Tab 1
        Item h102 = new BarangNonElektronik(
                "H-102", JenisLaporan.HILANG, "Dompet Kulit Braun Buffel Coklat",
                KategoriBarang.NON_ELEKTRONIK, "Tertinggal di meja kasir setelah bayar makan siang",
                "Kantin Pusat", LocalDate.now(), null, null, "Ada sobekan kecil di saku koin",
                StatusItem.DILAPORKAN, LocalDateTime.now().minusHours(2), mhsSiti,
                "Coklat Gelap", "Kulit Sapi"
        );
        itemRepository.save(h102);
        catatLog(h102.getId(), "DILAPORKAN oleh Mahasiswa", "Sistem");

        // 3. Pasangan Kandidat Smart Matching (DIVERIFIKASI / DICARI & TERSEDIA)
        Item h012 = new BarangElektronik(
                "H-012", JenisLaporan.HILANG, "Flashdisk SanDisk Cruzer Blade 64GB",
                KategoriBarang.ELEKTRONIK, "Flashdisk hitam merah tertinggal di PC-12 Lab Komputer",
                "Lab Komputer 1", LocalDate.now().minusDays(1), null, null, "Ada ukiran inisial RA",
                StatusItem.DICARI, LocalDateTime.now().minusDays(1), mhsRama,
                "SanDisk", "SN-SD64GB992"
        );
        Item t034 = new BarangElektronik(
                "T-034", JenisLaporan.TEMUAN, "USB Flashdisk SanDisk Merah Hitam",
                KategoriBarang.ELEKTRONIK, "Ditemukan tertancap di CPU Lab Komputer 1 setelah kelas",
                "Lab Komputer 1", LocalDate.now().minusDays(1), "POS-0001", null, "Ada goresan dekat port",
                StatusItem.TERSEDIA, LocalDateTime.now().minusDays(1), mhsAgnes,
                "SanDisk", "SN-SD64GB992"
        );
        itemRepository.save(h012);
        itemRepository.save(t034);
        lokasiSimpanMap.put(t034.getId(), "Loker B-04 Ruang Satpam");
        qrCodeMap.put(t034.getId(), "QR-T-034");
        catatLog(h012.getId(), "DIVERIFIKASI (Dicari)", "Bintang");
        catatLog(t034.getId(), "DIVERIFIKASI (Tersedia)", "Bintang");

        // 4. Barang Temuan dengan Antrean Klaim (TERSEDIA) -> Tab 2
        Item t055 = new BarangNonElektronik(
                "T-055", JenisLaporan.TEMUAN, "Tumbler Corkcicle Canteen Cobalt 750ml",
                KategoriBarang.NON_ELEKTRONIK, "Tumbler biru metalik tertinggal di meja baca lantai 2 perpustakaan",
                "Perpustakaan Pusat Lt. 2", LocalDate.now().minusDays(2), "POS-0001", null, "Penyok halus di bagian bawah",
                StatusItem.TERSEDIA, LocalDateTime.now().minusDays(2), mhsAgnes,
                "Cobalt Blue", "Stainless Steel"
        );
        itemRepository.save(t055);
        lokasiSimpanMap.put(t055.getId(), "Loker A-12 Pos Satpam Rektorat");
        qrCodeMap.put(t055.getId(), "QR-T-055");
        catatLog(t055.getId(), "DIVERIFIKASI (Tersedia)", "Bintang");

        if (klaimRepo != null) {
            Klaim k201 = new Klaim(
                    "CLM-2026-0001",
                    t055.getId(),
                    mhsRama,
                    "Tumbler Corkcicle warna Cobalt Blue, ada bekas stiker nama di bagian bawah botol.",
                    null,
                    LocalDate.now().plusDays(2),
                    com.campusfind.model.SesiLayanan.PAGI,
                    "POS-0001",
                    true,
                    true,
                    88,
                    StatusKlaim.DIAJUKAN,
                    LocalDateTime.now().minusDays(1),
                    null
            );
            klaimRepo.save(k201);
        }

        // 5. Barang Siap Serah Terima (DIKLAIM) -> Tab 5
        Item t080 = new BarangElektronik(
                "T-080", JenisLaporan.TEMUAN, "Kalkulator Ilmiah Casio FX-991EX",
                KategoriBarang.ELEKTRONIK, "Tertinggal di Ruang Seminar B",
                "Ruang Seminar B", LocalDate.now().minusDays(3), "POS-0001", null, "Tulisan 'Agnes' di penutup",
                StatusItem.DIKLAIM, LocalDateTime.now().minusDays(3), mhsBudi,
                "Casio", "SN-FX991-002"
        );
        itemRepository.save(t080);
        lokasiSimpanMap.put(t080.getId(), "Loker B-01 Ruang Satpam");
        qrCodeMap.put(t080.getId(), "QR-T-080");
        catatLog(t080.getId(), "DIVERIFIKASI (Tersedia)", "Bintang");
        catatLog(t080.getId(), "Status berubah menjadi: DIKLAIM (Klaim disetujui)", "Bintang");
    }
}
