package com.campusfind.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final JenisLaporan jenis;
    private final String nama;
    private final KategoriBarang kategori;
    private final String deskripsi;
    private final String lokasiKejadian;
    private final LocalDate tanggalKejadian;
    private String posId;
    private String fotoPath;
    private final String ciriRahasia;
    private StatusItem status;
    private final LocalDateTime dibuatPada;
    private final User pelapor;

    protected Item(String id, JenisLaporan jenis, String nama, KategoriBarang kategori,
                   String deskripsi, String lokasiKejadian, LocalDate tanggalKejadian,
                   String posId, String fotoPath, String ciriRahasia,
                   StatusItem status, LocalDateTime dibuatPada, User pelapor) {
        this.id = id;
        this.jenis = jenis;
        this.nama = nama;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.lokasiKejadian = lokasiKejadian;
        this.tanggalKejadian = tanggalKejadian;
        this.posId = posId;
        this.fotoPath = fotoPath;
        this.ciriRahasia = ciriRahasia;
        this.status = status;
        this.dibuatPada = dibuatPada;
        this.pelapor = pelapor;
    }

    public String kode() {
        return "#" + id;
    }

    public String getId() {
        return id;
    }

    public JenisLaporan getJenis() {
        return jenis;
    }

    public String getNama() {
        return nama;
    }

    public KategoriBarang getKategori() {
        return kategori;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getLokasiKejadian() {
        return lokasiKejadian;
    }

    public LocalDate getTanggalKejadian() {
        return tanggalKejadian;
    }

    public String getPosId() {
        return posId;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public String getCiriRahasia() {
        return ciriRahasia;
    }

    public StatusItem getStatus() {
        return status;
    }

    public LocalDateTime getDibuatPada() {
        return dibuatPada;
    }

    public User getPelapor() {
        return pelapor;
    }

    public void setStatus(StatusItem status) {
        this.status = status;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    /**
     * US-008: Method polimorfik untuk menampilkan rincian lengkap barang
     * sesuai tipe spesifiknya (BarangElektronik / BarangNonElektronik).
     *
     * @return representasi string rincian objek barang
     */
    public abstract String tampilkanDetail();

    /**
     * Dokumen Konsep v2.0 (Section 3.2): Method polimorfik untuk menentukan syarat bukti kepemilikan utama.
     * Barang Elektronik menggunakan verifikasi akun terdaftar (OTP / Unlock / Recovery);
     * Barang Non-Elektronik menggunakan ciri fisik.
     *
     * @return array JenisBukti yang dipersyaratkan
     */
    public abstract JenisBukti[] getSyaratBuktiUtama();

    /**
     * Memeriksa apakah data barang sudah terkunci permanen setelah diserahterimakan/diarsipkan (US-021, v2.0).
     *
     * @return true jika status adalah DIKEMBALIKAN, DISERAHKAN, DITOLAK, atau DIARSIPKAN
     */
    public boolean isTerkunci() {
        return status == StatusItem.DIKEMBALIKAN
                || status == StatusItem.DISERAHKAN
                || status == StatusItem.DIARSIPKAN
                || status == StatusItem.DITOLAK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
