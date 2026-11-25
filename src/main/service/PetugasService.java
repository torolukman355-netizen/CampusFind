package com.campusfind.service;

import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.NotificationType;
import com.campusfind.model.Petugas;
import com.campusfind.model.StatusItem;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.PosRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    public void verifikasiLaporan(String itemId, boolean terima, String alasanTolak, Petugas aktor) {
        validasiPetugas(aktor);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Barang tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new IllegalStateException("Data barang sudah terkunci.");
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
                    "Laporan '" + item.getNama() + "' telah diverifikasi oleh " + aktor.getNama() + ".");
        } else {
            if (alasanTolak == null || alasanTolak.trim().isEmpty()) {
                throw new IllegalArgumentException("Alasan penolakan wajib diisi.");
            }
            catatLog(itemId, "DITOLAK: " + alasanTolak.trim(), aktor.getNama());
            item.setStatus(StatusItem.DISERAHKAN);
            itemRepository.save(item);
            kirimNotifPelapor(item, "Laporan Ditolak",
                    "Laporan '" + item.getNama() + "' ditolak. Alasan: " + alasanTolak.trim());
        }
    }

    public void ubahStatusBarang(String itemId, StatusItem statusBaru, Petugas aktor) {
        validasiPetugas(aktor);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Barang tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new IllegalStateException("Barang sudah dikembalikan dan terkunci.");
        }
        if (!item.getStatus().bolehBerubahKe(statusBaru)) {
            throw new IllegalStateException("Transisi status tidak valid dari " + item.getStatus() + " ke " + statusBaru);
        }

        item.setStatus(statusBaru);
        itemRepository.save(item);
        catatLog(itemId, statusBaru.name(), aktor.getNama());
    }

    public void aturLokasiPenyimpanan(String itemId, String lokasiFisik, Petugas aktor) {
        validasiPetugas(aktor);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Barang tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new IllegalStateException("Barang sudah terkunci dan tidak dapat diubah.");
        }
        if (item.getJenis() != JenisLaporan.TEMUAN) {
            throw new IllegalArgumentException("Lokasi penyimpanan hanya berlaku untuk barang berjenis TEMUAN.");
        }
        if (lokasiFisik == null || lokasiFisik.trim().isEmpty()) {
            throw new IllegalArgumentException("Lokasi penyimpanan wajib diisi.");
        }

        lokasiSimpanMap.put(itemId, lokasiFisik.trim());
        catatLog(itemId, "Lokasi simpan diatur ke: " + lokasiFisik.trim(), aktor.getNama());
    }

    public void prosesSerahTerima(String itemId, String namaPenerima, LocalDate tanggalSerah, Petugas aktor) {
        validasiPetugas(aktor);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Barang tidak ditemukan."));

        if (isTerkunci(item)) {
            throw new IllegalStateException("Barang ini sudah diserahterimakan dan terkunci.");
        }
        if (item.getStatus() != StatusItem.DALAM_PROSES_KLAIM && item.getStatus() != StatusItem.DIKLAIM) {
            throw new IllegalStateException("Serah terima hanya dapat dilakukan untuk barang berstatus DIKLAIM.");
        }
        if (namaPenerima == null || namaPenerima.trim().isEmpty()) {
            throw new IllegalArgumentException("Nama penerima wajib diisi.");
        }

        item.setStatus(StatusItem.DIKEMBALIKAN);
        itemRepository.save(item);
        catatLog(itemId, "DIKEMBALIKAN kepada " + namaPenerima.trim() + " pada " + tanggalSerah + " [TERKUNCI]", aktor.getNama());

        kirimNotifPelapor(item, "Barang Dikembalikan",
                "Barang '" + item.getNama() + "' telah diserahterimakan kepada " + namaPenerima.trim() + ".");
    }

    public Item cariBarangByQR(String kodeQr) {
        if (kodeQr == null || kodeQr.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode QR tidak boleh kosong.");
        }
        String target = kodeQr.trim().toUpperCase();
        for (Item item : itemRepository.findAll()) {
            String kodeItem = getKodeQR(item);
            if (kodeItem.equalsIgnoreCase(target) || item.getId().equalsIgnoreCase(target)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Barang tidak ditemukan.");
    }

    public String getKodeQR(Item item) {
        if (item.getJenis() != JenisLaporan.TEMUAN) return "-";
        return qrCodeMap.computeIfAbsent(item.getId(), id -> qrCodeService.generateUniqueCode(id));
    }

    public String getLokasiSimpan(Item item) {
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
        return item.getStatus() == StatusItem.DIKEMBALIKAN || item.getStatus() == StatusItem.DISERAHKAN;
    }

    public String getRiwayatLog(String itemId) {
        List<String> logs = riwayatStatusMap.get(itemId);
        if (logs == null || logs.isEmpty()) {
            return "Belum ada catatan perubahan status oleh petugas.";
        }
        return String.join("\n", logs);
    }

    private void catatLog(String itemId, String aksi, String pelaku) {
        riwayatStatusMap.computeIfAbsent(itemId, k -> new ArrayList<>())
                .add("[" + LocalDateTime.now().format(dtf) + "] " + aksi + " (oleh " + pelaku + ")");
    }

    private void validasiPetugas(Petugas aktor) {
        if (aktor == null) {
            throw new SecurityException("Akses ditolak: hanya Petugas yang berwenang.");
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
}