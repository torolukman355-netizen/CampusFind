package com.campusfind.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BarangElektronik extends Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String merek;
    private final String nomorSeri;

    public BarangElektronik(String id, JenisLaporan jenis, String nama, KategoriBarang kategori,
                            String deskripsi, String lokasiKejadian, LocalDate tanggalKejadian,
                            String posId, String fotoPath, String ciriRahasia,
                            StatusItem status, LocalDateTime dibuatPada, User pelapor,
                            String merek, String nomorSeri) {
        super(id, jenis, nama, kategori, deskripsi, lokasiKejadian, tanggalKejadian,
              posId, fotoPath, ciriRahasia, status, dibuatPada, pelapor);
        this.merek = merek;
        this.nomorSeri = nomorSeri;
    }

    public String getMerek() {
        return merek;
    }

    public String getNomorSeri() {
        return nomorSeri;
    }
}
