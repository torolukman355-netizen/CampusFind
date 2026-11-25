package com.campusfind.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BarangNonElektronik extends Item {

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

    @Override
    public JenisBukti[] getSyaratBuktiUtama() {
        return new JenisBukti[]{JenisBukti.CIRI_FISIK};
    }

    @Override
    public String tampilkanDetail() {
        String pelaporInfo = (getPelapor() != null)
                ? getPelapor().getNama() + " (" + getPelapor().getNomorIdentitas() + ")"
                : "-";
        return "=== DETAIL BARANG NON-ELEKTRONIK (US-008) ===\n" +
                "ID Barang       : " + getId() + "\n" +
                "Jenis Laporan   : " + getJenis().name() + "\n" +
                "Nama Barang     : " + getNama() + "\n" +
                "Kategori        : " + getKategori().getLabel() + "\n" +
                "Warna           : " + (warna != null && !warna.isBlank() ? warna : "-") + "\n" +
                "Bahan / Jenis   : " + (bahan != null && !bahan.isBlank() ? bahan : "-") + "\n" +
                "Lokasi Kejadian : " + getLokasiKejadian() + "\n" +
                "Tanggal Kejadian: " + getTanggalKejadian() + "\n" +
                "Ciri Fisik Khusus: " + (getCiriRahasia() != null && !getCiriRahasia().isBlank() ? getCiriRahasia() : "-") + "\n" +
                "Pelapor         : " + pelaporInfo + "\n" +
                "Status Barang   : " + getStatus().getLabel() + (isTerkunci() ? " [TERKUNCI]" : "");
    }
}
