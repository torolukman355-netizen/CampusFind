package com.campusfind.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    private ItemFactory() {
        // utility class
    }

    public static Item create(String id, JenisLaporan jenis, String namaBarang,
                              KategoriBarang kategori, String deskripsi,
                              String lokasiKejadian, LocalDate tanggalKejadian,
                              String posId, String fotoPath, String ciriRahasia,
                              StatusItem status, LocalDateTime dibuatPada,
                              String namaPelapor, String nomorIdentitas,
                              String merekAtauWarna, String nomorSeriAtauBahan) {

        User pelapor;
        if (nomorIdentitas != null && nomorIdentitas.length() == 9) {
            pelapor = new Mahasiswa(namaPelapor, nomorIdentitas);
        } else if (nomorIdentitas != null && nomorIdentitas.length() == 18) {
            pelapor = new Petugas(namaPelapor, nomorIdentitas);
        } else {
            throw new IllegalArgumentException(
                    "Nomor identitas harus 9 digit (Mahasiswa) atau 18 digit (Petugas).");
        }

        if (kategori == KategoriBarang.ELEKTRONIK) {
            return new BarangElektronik(id, jenis, namaBarang, kategori, deskripsi,
                    lokasiKejadian, tanggalKejadian, posId, fotoPath, ciriRahasia,
                    status, dibuatPada, pelapor, merekAtauWarna, nomorSeriAtauBahan);
        } else {
            return new BarangNonElektronik(id, jenis, namaBarang, kategori, deskripsi,
                    lokasiKejadian, tanggalKejadian, posId, fotoPath, ciriRahasia,
                    status, dibuatPada, pelapor, merekAtauWarna, nomorSeriAtauBahan);
        }
    }
}
