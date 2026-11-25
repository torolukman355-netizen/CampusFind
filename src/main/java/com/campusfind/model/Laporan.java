package com.campusfind.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representasi kejadian laporan barang Hilang/Temuan berbasis komposisi (Dokumen Konsep v2.0).
 * Menghubungkan User pelapor, objek Item barang, lokasi kejadian, waktu, status, dan privasi tampilan publik.
 */
public class Laporan implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final User pelapor;
    private final Item item;
    private final JenisLaporan jenis;
    private final String lokasi;
    private final LocalDateTime waktu;
    private StatusLaporan status;
    private String kodeQR;
    private boolean diserahkanKePos;

    public Laporan(String id, User pelapor, Item item, JenisLaporan jenis, String lokasi, LocalDateTime waktu) {
        this.id = Objects.requireNonNull(id, "ID laporan tidak boleh null");
        this.pelapor = Objects.requireNonNull(pelapor, "Pelapor tidak boleh null");
        this.item = Objects.requireNonNull(item, "Item barang tidak boleh null");
        this.jenis = Objects.requireNonNull(jenis, "Jenis laporan tidak boleh null");
        this.lokasi = lokasi != null ? lokasi : "";
        this.waktu = waktu != null ? waktu : LocalDateTime.now();
        this.status = StatusLaporan.DILAPORKAN;
        this.diserahkanKePos = false;
    }

    /**
     * Membuat objek Laporan komposisi secara otomatis dari objek Item yang sudah ada.
     */
    public static Laporan dariItem(Item item) {
        Objects.requireNonNull(item, "Item tidak boleh null");
        User pelaporAman = item.getPelapor() != null
                ? item.getPelapor()
                : new Mahasiswa("Anonim", "000000");
        LocalDateTime waktuKejadian = item.getTanggalKejadian() != null
                ? item.getTanggalKejadian().atStartOfDay()
                : item.getDibuatPada();
        Laporan laporan = new Laporan(
                "LAP-" + item.getId(),
                pelaporAman,
                item,
                item.getJenis(),
                item.getLokasiKejadian(),
                waktuKejadian
        );
        laporan.ubahStatus(petakanStatusItem(item.getStatus()));
        return laporan;
    }

    private static StatusLaporan petakanStatusItem(StatusItem statusItem) {
        if (statusItem == null) return StatusLaporan.DILAPORKAN;
        return switch (statusItem) {
            case DILAPORKAN -> StatusLaporan.DILAPORKAN;
            case DIVERIFIKASI, TERSEDIA, DICARI -> StatusLaporan.DIVERIFIKASI;
            case COCOK -> StatusLaporan.COCOK;
            case DALAM_PROSES_KLAIM, DIKLAIM -> StatusLaporan.DALAM_PROSES_KLAIM;
            case DIKEMBALIKAN, DISERAHKAN -> StatusLaporan.DIKEMBALIKAN;
            case DITOLAK -> StatusLaporan.DITOLAK;
            case DIARSIPKAN -> StatusLaporan.DIARSIPKAN;
        };
    }

    public void ubahStatus(StatusLaporan statusBaru) {
        this.status = Objects.requireNonNull(statusBaru, "Status baru tidak boleh null");
    }

    /**
     * Tampilan publik yang melindungi privasi pelapor (Dokumen Konsep v2.0 Section 2.6 & 3.3).
     * Nama dan identitas lengkap pelapor disembunyikan dari pengguna lain.
     */
    public String tampilkanKePublik() {
        String infoSpesifik = item.getKategori() != null ? item.getKategori().getLabel() : "-";
        if (item instanceof BarangNonElektronik nonEl && nonEl.getWarna() != null && !nonEl.getWarna().isBlank()) {
            infoSpesifik = nonEl.getWarna();
        } else if (item instanceof BarangElektronik el && el.getMerek() != null && !el.getMerek().isBlank()) {
            infoSpesifik = el.getMerek();
        }
        return "[" + jenis.name() + "] " + item.getNama() + " (" + infoSpesifik + ") di " + lokasi
                + " | Status: " + status.getLabel() + " | Pelapor: [Disembunyikan demi Privasi]";
    }

    public String getId() {
        return id;
    }

    public User getPelapor() {
        return pelapor;
    }

    public Item getItem() {
        return item;
    }

    public JenisLaporan getJenis() {
        return jenis;
    }

    public String getLokasi() {
        return lokasi;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public StatusLaporan getStatus() {
        return status;
    }

    public String getKodeQR() {
        return kodeQR;
    }

    public void setKodeQR(String kodeQR) {
        if (this.jenis != JenisLaporan.TEMUAN && kodeQR != null) {
            throw new IllegalStateException("Kode QR hanya dapat diisi untuk laporan TEMUAN.");
        }
        this.kodeQR = kodeQR;
    }

    public boolean isDiserahkanKePos() {
        return diserahkanKePos;
    }

    public void setDiserahkanKePos(boolean diserahkanKePos) {
        this.diserahkanKePos = diserahkanKePos;
    }
}
