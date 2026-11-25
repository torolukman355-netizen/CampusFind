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
    private String tempatPenitipan;
    private String fotoPath;
    private byte[] fotoBytes;
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
        this.tempatPenitipan = posId;
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

    public byte[] getFotoBytes() {
        return fotoBytes;
    }

    public void setFotoBytes(byte[] fotoBytes) {
        this.fotoBytes = fotoBytes;
    }

    public boolean hasFotoBytes() {
        return fotoBytes != null && fotoBytes.length > 0;
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

<<<<<<< HEAD:src/main/model/Item.java
    public String getTempatPenitipan() {
        return (tempatPenitipan != null && !tempatPenitipan.isBlank()) ? tempatPenitipan : posId;
    }

    public void setTempatPenitipan(String tempatPenitipan) {
        this.tempatPenitipan = tempatPenitipan;
        if (this.posId == null || this.posId.isBlank()) {
            this.posId = tempatPenitipan;
        }
    }

    /**
     * Memeriksa apakah barang temuan dapat diajukan klaim oleh mahasiswa.
     * Hanya barang berjenis TEMUAN dengan status TERSEDIA, DIVERIFIKASI, atau COCOK yang dapat diklaim.
     */
    public boolean isBisaDiklaim() {
        return jenis == JenisLaporan.TEMUAN &&
                (status == StatusItem.TERSEDIA
                 || status == StatusItem.DIVERIFIKASI
                 || status == StatusItem.COCOK);
=======
    /**
     * US-008: Method polimorfik untuk menampilkan rincian lengkap barang
     * sesuai tipe spesifiknya (BarangElektronik / BarangNonElektronik).
     *
     * @return representasi string rincian objek barang
     */
    public abstract String tampilkanDetail();

    /**
     * Memeriksa apakah data barang sudah terkunci permanen setelah diserahterimakan (US-021).
     *
     * @return true jika status adalah DIKEMBALIKAN atau DISERAHKAN
     */
    public boolean isTerkunci() {
        return status == StatusItem.DIKEMBALIKAN || status == StatusItem.DISERAHKAN;
>>>>>>> ca2e27ff81bd0eaf0f0496ef3f356f4006abcbb8:src/main/java/com/campusfind/model/Item.java
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
