package com.campusfind.service;

import com.campusfind.exception.AksesDitolakException;
import com.campusfind.exception.NotFoundException;
import com.campusfind.exception.ValidationException;
import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.HakAkses;
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
import com.campusfind.repository.PetugasAuditRepository;
import com.campusfind.repository.PosRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service pusat untuk seluruh fungsionalitas Role Petugas.
 */
public class PetugasService {

    private final ItemRepository itemRepository;
    private final PosRepository posRepository;
    private final NotificationService notificationService;
    private final QRCodeService qrCodeService;
    private final PetugasAuditRepository auditRepository;

    // Fallback memori hanya dipakai jika konstruktor lama (unit test) dipanggil tanpa auditRepository
    private final Map<String, List<String>> fallbackLogMap = new ConcurrentHashMap<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PetugasService(ItemRepository itemRepository,
                          PosRepository posRepository,
                          NotificationService notificationService,
                          QRCodeService qrCodeService) {
        this(itemRepository, posRepository, notificationService, qrCodeService, null);
    }

    public PetugasService(ItemRepository itemRepository,
                          PosRepository posRepository,
                          NotificationService notificationService,
                          QRCodeService qrCodeService,
                          PetugasAuditRepository auditRepository) {
        this.itemRepository = itemRepository;
        this.posRepository = posRepository;
        this.notificationService = notificationService;
        this.qrCodeService = qrCodeService;
        this.auditRepository = auditRepository;
    }

    /**
     * US-011 & US-028: Verifikasi Laporan Masuk dari Mahasiswa.
     */
    public void verifikasiLaporan(String itemId, boolean terima, String alasanTolak, Petugas aktor) {
        validasiHakAkses(aktor, HakAkses.VERIFIKASI_LAPORAN);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Barang dengan ID " + itemId + " tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new ValidationException("Data barang [" + itemId + "] sudah dikembalikan dan terkunci permanen.");
        }
        if (item.getStatus() != StatusItem.DILAPORKAN) {
            throw new ValidationException("Hanya laporan berstatus DILAPORKAN yang dapat diverifikasi.");
        }

        if (terima) {
            StatusItem statusBaru = (item.getJenis() == JenisLaporan.TEMUAN)
                    ? StatusItem.TERSEDIA
                    : StatusItem.DICARI;
            item.setStatus(statusBaru);
            itemRepository.save(item);
            catatLog(itemId, "DIVERIFIKASI (" + statusBaru.getLabel() + ")", aktor.getNama());

            kirimNotifPelapor(item, NotificationType.LAPORAN_DIVERIFIKASI, "Laporan Diverifikasi",
                    "Laporan '" + item.getNama() + "' telah diverifikasi oleh Petugas " + aktor.getNama() + ".");
        } else {
            if (alasanTolak == null || alasanTolak.trim().isEmpty()) {
                throw new ValidationException("alasanTolak", "Alasan penolakan laporan wajib diisi!");
            }
            String alasanBersih = alasanTolak.trim();
            item.setStatus(StatusItem.DITOLAK);
            itemRepository.save(item);
            catatLog(itemId, "DITOLAK: " + alasanBersih, aktor.getNama());

            kirimNotifPelapor(item, NotificationType.LAPORAN_DITOLAK, "Laporan Ditolak",
                    "Laporan '" + item.getNama() + "' ditolak. Alasan: " + alasanBersih);
        }
    }

    /**
     * US-012: Kontrol Transisi Status Berurutan.
     */
    public void ubahStatusBarang(String itemId, StatusItem statusBaru, Petugas aktor) {
        validasiHakAkses(aktor, HakAkses.KELOLA_STATUS_ITEM);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Barang dengan ID " + itemId + " tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new ValidationException("Barang [" + itemId + "] sudah berstatus akhir (DIKEMBALIKAN) dan datanya TERKUNCI.");
        }
        if (!item.getStatus().bolehBerubahKe(statusBaru)) {
            throw new ValidationException("Transisi status tidak valid: dari " + item.getStatus() + " ke " + statusBaru);
        }

        item.setStatus(statusBaru);
        itemRepository.save(item);
        catatLog(itemId, "Status berubah menjadi: " + statusBaru.name(), aktor.getNama());
    }

    /**
     * US-013: Atur atau perbarui Lokasi Penyimpanan Fisik Barang (tersimpan permanen di item).
     */
    public void aturLokasiPenyimpanan(String itemId, String lokasiFisik, Petugas aktor) {
        validasiHakAkses(aktor, HakAkses.KELOLA_LOKASI_SIMPAN);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Barang tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new ValidationException("Data barang terkunci dan tidak dapat diubah lagi.");
        }
        if (item.getJenis() != JenisLaporan.TEMUAN) {
            throw new ValidationException("Lokasi penyimpanan fisik HANYA dapat diatur untuk barang berjenis TEMUAN!");
        }
        if (lokasiFisik == null || lokasiFisik.trim().isEmpty()) {
            throw new ValidationException("lokasiFisik", "Lokasi penyimpanan fisik tidak boleh kosong.");
        }

        String lokasiBersih = lokasiFisik.trim();
        item.setTempatPenitipan(lokasiBersih);
        itemRepository.save(item);
        catatLog(itemId, "Lokasi simpan fisik diatur ke: " + lokasiBersih, aktor.getNama());
    }

    /**
     * US-021 & US-029: Pencatatan Serah Terima Barang & Penguncian Data.
     */
    public void prosesSerahTerima(String itemId, String namaPenerima, LocalDate tanggalSerah, Petugas aktor) {
        validasiHakAkses(aktor, HakAkses.SERAH_TERIMA);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Barang tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new ValidationException("Barang ini sudah diserahterimakan dan terkunci permanen.");
        }
        if (item.getStatus() != StatusItem.DALAM_PROSES_KLAIM && item.getStatus() != StatusItem.DIKLAIM) {
            throw new ValidationException("Serah terima hanya dapat diproses untuk barang berstatus DIKLAIM / DALAM PROSES KLAIM.");
        }
        if (namaPenerima == null || namaPenerima.trim().isEmpty()) {
            throw new ValidationException("namaPenerima", "Nama penerima barang wajib diisi.");
        }

        LocalDate tglEfektif = (tanggalSerah != null) ? tanggalSerah : LocalDate.now();
        String penerimaBersih = namaPenerima.trim();

        item.setStatus(StatusItem.DIKEMBALIKAN);
        itemRepository.save(item);

        if (auditRepository != null) {
            auditRepository.simpanSerahTerima(itemId, penerimaBersih, tglEfektif, aktor.getNomorIdentitas());
        }
        catatLog(itemId, "DIKEMBALIKAN kepada " + penerimaBersih + " pada " + tglEfektif + " [DATA TERKUNCI]", aktor.getNama());

        kirimNotifPelapor(item, NotificationType.BARANG_DIKEMBALIKAN, "Barang Dikembalikan",
                "Barang '" + item.getNama() + "' telah selesai diserahterimakan kepada " + penerimaBersih + ".");
    }

    /**
     * US-018 & US-029: Pencarian & Pelacakan Barang via QR Code.
     */
    public Item cariBarangByQR(String kodeQr) {
        if (kodeQr == null || kodeQr.trim().isEmpty()) {
            throw new ValidationException("Kode QR tidak boleh kosong.");
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
        throw new NotFoundException("Barang dengan kode QR / ID '" + kodeQr + "' tidak ditemukan dalam sistem.");
    }

    public String getKodeQR(Item item) {
        if (item == null || item.getJenis() != JenisLaporan.TEMUAN) return "-";
        return qrCodeService.generateUniqueCode(item.getId());
    }

    public String getLokasiSimpan(Item item) {
        if (item == null) return "-";
        if (item.getTempatPenitipan() != null && !item.getTempatPenitipan().isBlank()) {
            return item.getTempatPenitipan();
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
        List<String> logs = (auditRepository != null)
                ? auditRepository.ambilLog(itemId)
                : fallbackLogMap.get(itemId);

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
        if (auditRepository != null) {
            auditRepository.simpanLog(itemId, aksi, pelaku);
        } else {
            fallbackLogMap.computeIfAbsent(itemId, k -> new ArrayList<>())
                    .add("[" + LocalDateTime.now().format(dtf) + "] " + aksi + " (oleh " + pelaku + ")");
        }
    }

    private void validasiHakAkses(Petugas aktor, HakAkses hakDibutuhkan) {
        if (aktor == null || !aktor.punyaHak(hakDibutuhkan)) {
            throw new AksesDitolakException("Akses Ditolak: Anda tidak memiliki hak akses " + hakDibutuhkan + ".");
        }
    }

    private void kirimNotifPelapor(Item item, NotificationType tipe, String judul, String pesan) {
        if (notificationService != null && item.getPelapor() != null && item.getPelapor().getNomorIdentitas() != null) {
            notificationService.kirim(
                    item.getPelapor().getNomorIdentitas(),
                    tipe,
                    judul,
                    pesan,
                    item.getId()
            );
        }
    }

    public void pastikanDataAwalLengkap(KlaimRepository klaimRepo) {
        if (itemRepository.findAll().size() >= 3) {
            return;
        }

        User mhsRama = new Mahasiswa("Rama Aditya", "230001001");
        User mhsAgnes = new Mahasiswa("Agnes Monica", "230001002");
        User mhsBudi = new Mahasiswa("Budi Santoso", "230001003");
        User mhsSiti = new Mahasiswa("Siti Nurhaliza", "230001004");

        Item t101 = new BarangElektronik(
                "T-101", JenisLaporan.TEMUAN, "iPhone 13 Midnight 128GB",
                KategoriBarang.ELEKTRONIK, "Ditemukan di bangku panjang Gedung Kuliah Bersama Lt. 2",
                "Gedung A Lt. 2", LocalDate.now(), "POS-0001", null, "Case bening sudut retak",
                StatusItem.DILAPORKAN, LocalDateTime.now().minusHours(3), mhsBudi,
                "Apple", "IMEI-3599201992"
        );
        itemRepository.save(t101);
        catatLog(t101.getId(), "DILAPORKAN oleh Mahasiswa", "Sistem");

        Item h102 = new BarangNonElektronik(
                "H-102", JenisLaporan.HILANG, "Dompet Kulit Braun Buffel Coklat",
                KategoriBarang.NON_ELEKTRONIK, "Tertinggal di meja kasir setelah bayar makan siang",
                "Kantin Pusat", LocalDate.now(), null, null, "Ada sobekan kecil di saku koin",
                StatusItem.DILAPORKAN, LocalDateTime.now().minusHours(2), mhsSiti,
                "Coklat Gelap", "Kulit Sapi"
        );
        itemRepository.save(h102);
        catatLog(h102.getId(), "DILAPORKAN oleh Mahasiswa", "Sistem");

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
        t034.setTempatPenitipan("Loker B-04 Ruang Satpam");
        itemRepository.save(h012);
        itemRepository.save(t034);
        catatLog(h012.getId(), "DIVERIFIKASI (Dicari)", "Rina");
        catatLog(t034.getId(), "DIVERIFIKASI (Tersedia)", "Rina");

        Item t055 = new BarangNonElektronik(
                "T-055", JenisLaporan.TEMUAN, "Tumbler Corkcicle Canteen Cobalt 750ml",
                KategoriBarang.NON_ELEKTRONIK, "Tumbler biru metalik tertinggal di meja baca lantai 2 perpustakaan",
                "Perpustakaan Pusat Lt. 2", LocalDate.now().minusDays(2), "POS-0001", null, "Penyok halus di bagian bawah",
                StatusItem.TERSEDIA, LocalDateTime.now().minusDays(2), mhsAgnes,
                "Cobalt Blue", "Stainless Steel"
        );
        t055.setTempatPenitipan("Loker A-12 Pos Satpam Rektorat");
        itemRepository.save(t055);
        catatLog(t055.getId(), "DIVERIFIKASI (Tersedia)", "Rina");

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

        Item t080 = new BarangElektronik(
                "T-080", JenisLaporan.TEMUAN, "Kalkulator Ilmiah Casio FX-991EX",
                KategoriBarang.ELEKTRONIK, "Tertinggal di Ruang Seminar B",
                "Ruang Seminar B", LocalDate.now().minusDays(3), "POS-0001", null, "Tulisan 'Agnes' di penutup",
                StatusItem.DIKLAIM, LocalDateTime.now().minusDays(3), mhsBudi,
                "Casio", "SN-FX991-002"
        );
        t080.setTempatPenitipan("Loker B-01 Ruang Satpam");
        itemRepository.save(t080);
        catatLog(t080.getId(), "DIVERIFIKASI (Tersedia)", "Rina");
        catatLog(t080.getId(), "Status berubah menjadi: DIKLAIM (Klaim disetujui)", "Rina");
    }
}