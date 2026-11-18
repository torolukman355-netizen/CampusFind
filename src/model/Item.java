package model;

import model.enums.JenisLaporan;
import model.enums.KategoriItem;
import model.enums.StatusItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * US-007, US-008, US-011, US-012, US-013:
 * Abstract class Item sebagai cetak biru barang (Hilang / Temuan).
 */
public abstract class Item {
    protected String id;
    protected JenisLaporan jenis;
    protected String nama;
    protected KategoriItem kategori;
    protected String lokasiKejadian;
    protected String keteranganLokasi;
    protected LocalDateTime waktuKejadian;
    protected String deskripsi;
    protected String fotoPath;
    protected String pelapor;
    protected StatusItem status;
    protected String lokasiSimpan; // US-013 (Khusus Temuan)
    protected String kodeQR;       // US-017 (Khusus Temuan terverifikasi)
    protected boolean terkunci;    // US-021 (Terkunci jika DIKEMBALIKAN)
    protected List<LogStatus> riwayatLog = new ArrayList<>();

    public Item(String id, JenisLaporan jenis, String nama, KategoriItem kategori,
                String lokasiKejadian, String pelapor, String petugasAwal) {
        this.id = id;
        this.jenis = jenis;
        this.nama = nama;
        this.kategori = kategori;
        this.lokasiKejadian = lokasiKejadian;
        this.keteranganLokasi = "-";
        this.lokasiSimpan = "-";
        this.pelapor = pelapor;
        this.status = StatusItem.DILAPORKAN;
        this.kodeQR = "-";
        this.terkunci = false;
        this.waktuKejadian = LocalDateTime.now();
        this.deskripsi = "-";
        this.fotoPath = "-";

        tambahLog(StatusItem.DILAPORKAN, petugasAwal, "Laporan pertama kali didaftarkan ke sistem");
    }

    /**
     * US-012: Kontrol Transisi Status Berurutan:
     * DILAPORKAN -> DIVERIFIKASI -> COCOK -> DIKLAIM -> DIKEMBALIKAN
     * Dan DILAPORKAN -> DITOLAK
     */
    public synchronized boolean ubahStatus(StatusItem statusBaru, String pelaku, String keterangan) {
        if (this.terkunci) {
            throw new IllegalStateException("Barang [" + id + " - " + nama + "] sudah DIKEMBALIKAN dan datanya TERKUNCI.");
        }

        if (!isValidTransition(this.status, statusBaru)) {
            throw new IllegalArgumentException("Transisi status tidak valid: dari " + this.status + " ke " + statusBaru + "!");
        }

        this.status = statusBaru;

        // Otomatis buat Kode QR jika barang Temuan berstatus DIVERIFIKASI (US-011 & US-017)
        if (statusBaru == StatusItem.DIVERIFIKASI && this.jenis == JenisLaporan.TEMUAN) {
            if ("-".equals(this.kodeQR) || this.kodeQR == null) {
                this.kodeQR = "QR-" + this.id;
            }
        }

        // Kunci data jika barang sudah diserahterimakan / DIKEMBALIKAN (US-021)
        if (statusBaru == StatusItem.DIKEMBALIKAN) {
            this.terkunci = true;
        }

        tambahLog(statusBaru, pelaku, keterangan);
        return true;
    }

    private boolean isValidTransition(StatusItem lama, StatusItem baru) {
        if (lama == baru) return false;

        switch (lama) {
            case DILAPORKAN:
                return baru == StatusItem.DIVERIFIKASI || baru == StatusItem.DITOLAK;
            case DIVERIFIKASI:
                return baru == StatusItem.COCOK || baru == StatusItem.DIKLAIM;
            case COCOK:
                return baru == StatusItem.DIKLAIM;
            case DIKLAIM:
                return baru == StatusItem.DIKEMBALIKAN;
            case DIKEMBALIKAN:
            case DITOLAK:
            default:
                return false;
        }
    }

    public void tambahLog(StatusItem status, String pelaku, String keterangan) {
        riwayatLog.add(new LogStatus(status, pelaku, keterangan));
    }

    /**
     * US-013: Atur Lokasi Penyimpanan Fisik Barang (Khusus Temuan).
     */
    public void setLokasiSimpan(String lokasiSimpan, String pelaku) {
        if (this.terkunci) {
            throw new IllegalStateException("Data barang terkunci, tidak dapat mengubah lokasi simpan.");
        }
        if (this.jenis != JenisLaporan.TEMUAN) {
            throw new IllegalArgumentException("Lokasi penyimpanan fisik hanya berlaku untuk barang berjenis TEMUAN!");
        }
        this.lokasiSimpan = lokasiSimpan;
        tambahLog(this.status, pelaku, "Update lokasi simpan menjadi: " + lokasiSimpan);
    }

    /**
     * US-008: Method Polimorfisme untuk menampilkan rincian barang.
     */
    public abstract String tampilkanDetail();

    // Getter dan Setter
    public String getId() { return id; }
    public JenisLaporan getJenis() { return jenis; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public KategoriItem getKategori() { return kategori; }
    public String getLokasiKejadian() { return lokasiKejadian; }
    public void setLokasiKejadian(String lokasiKejadian) { this.lokasiKejadian = lokasiKejadian; }
    public String getKeteranganLokasi() { return keteranganLokasi; }
    public void setKeteranganLokasi(String keteranganLokasi) { this.keteranganLokasi = keteranganLokasi; }
    public LocalDateTime getWaktuKejadian() { return waktuKejadian; }
    public void setWaktuKejadian(LocalDateTime waktuKejadian) { this.waktuKejadian = waktuKejadian; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public String getFotoPath() { return fotoPath; }
    public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }
    public String getPelapor() { return pelapor; }
    public StatusItem getStatus() { return status; }
    public String getLokasiSimpan() { return lokasiSimpan; }
    public String getKodeQR() { return kodeQR; }
    public void setKodeQR(String kodeQR) { this.kodeQR = kodeQR; }
    public boolean isTerkunci() { return terkunci; }
    public List<LogStatus> getRiwayatLog() { return riwayatLog; }

    public String getRiwayatLogFormatted() {
        StringBuilder sb = new StringBuilder();
        for (LogStatus log : riwayatLog) {
            sb.append(log.toString()).append("\n");
        }
        return sb.toString();
    }
}
