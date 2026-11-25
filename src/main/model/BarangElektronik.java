package com.campusfind.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BarangElektronik extends Item {

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

    @Override
    public String tampilkanDetail() {
        String pelaporInfo = (getPelapor() != null)
                ? getPelapor().getNama() + " (" + getPelapor().getNomorIdentitas() + ")"
                : "-";
        return "=== DETAIL BARANG ELEKTRONIK (US-008) ===\n" +
                "ID Barang       : " + getId() + "\n" +
                "Jenis Laporan   : " + getJenis().name() + "\n" +
                "Nama Barang     : " + getNama() + "\n" +
                "Kategori        : " + getKategori().getLabel() + "\n" +
                "Merk / Brand    : " + (merek != null && !merek.isBlank() ? merek : "-") + "\n" +
                "Nomor Seri (SN) : " + (nomorSeri != null && !nomorSeri.isBlank() ? nomorSeri : "-") + " [RAHASIA PETUGAS: Verifikasi Klaim]\n" +
                "Lokasi Kejadian : " + getLokasiKejadian() + "\n" +
                "Tanggal Kejadian: " + getTanggalKejadian() + "\n" +
                "Ciri Khusus     : " + (getCiriRahasia() != null && !getCiriRahasia().isBlank() ? getCiriRahasia() : "-") + "\n" +
                "Pelapor         : " + pelaporInfo + "\n" +
                "Status Barang   : " + getStatus().getLabel() + (isTerkunci() ? " [TERKUNCI]" : "");
    }
}
