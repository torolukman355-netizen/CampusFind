package com.campusfind.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BarangNonElektronik extends Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String warna;
    private final String bahan;

    public BarangNonElektronik(String id, JenisLaporan jenis, String nama, KategoriBarang kategori,
                               String deskripsi, String lokasiKejadian, LocalDate tanggalKejadian,
                               String posId, String fotoPath, String ciriRahasia,
                               StatusItem status, LocalDateTime dibuatPada, User pelapor,
                               String warna, String bahan) {
        super(id, jenis, nama, kategori, deskripsi, lokasiKejadian, tanggalKejadian,
              posId, fotoPath, ciriRahasia, status, dibuatPada, pelapor);
        this.warna = warna;
        this.bahan = bahan;
    }

    public String getWarna() {
        return warna;
    }

    public String getBahan() {
        return bahan;
    }
}
