package com.campusfind.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BarangElektronik extends Item {

    private static final long serialVersionUID = 1L;

    private final String merek;
    private final String nomorSeri;
    private String tipe;
    private String akunTerdaftar; // Email / No. HP terdaftar di perangkat (Bukti utama v2.0)

    public BarangElektronik(String id, JenisLaporan jenis, String nama, KategoriBarang kategori,
                            String deskripsi, String lokasiKejadian, LocalDate tanggalKejadian,
                            String posId, String fotoPath, String ciriRahasia,
                            StatusItem status, LocalDateTime dibuatPada, User pelapor,
                            String merek, String nomorSeri) {
        this(id, jenis, nama, kategori, deskripsi, lokasiKejadian, tanggalKejadian,
             posId, fotoPath, ciriRahasia, status, dibuatPada, pelapor, merek, nomorSeri, "", "");
    }

    public BarangElektronik(String id, JenisLaporan jenis, String nama, KategoriBarang kategori,
                            String deskripsi, String lokasiKejadian, LocalDate tanggalKejadian,
                            String posId, String fotoPath, String ciriRahasia,
                            StatusItem status, LocalDateTime dibuatPada, User pelapor,
                            String merek, String nomorSeri, String tipe, String akunTerdaftar) {
        super(id, jenis, nama, kategori, deskripsi, lokasiKejadian, tanggalKejadian,
              posId, fotoPath, ciriRahasia, status, dibuatPada, pelapor);
        this.merek = merek;
        this.nomorSeri = nomorSeri;
        this.tipe = tipe;
        this.akunTerdaftar = akunTerdaftar;
    }

    public String getMerek() {
        return merek;
    }

    public String getNomorSeri() {
        return nomorSeri;
    }

    public String getTipe() {
        return tipe;
    }

    public void setTipe(String tipe) {
        this.tipe = tipe;
    }

    public String getAkunTerdaftar() {
        return akunTerdaftar;
    }

    public void setAkunTerdaftar(String akunTerdaftar) {
        this.akunTerdaftar = akunTerdaftar;
    }

    @Override
    public JenisBukti[] getSyaratBuktiUtama() {
        return new JenisBukti[]{
                JenisBukti.OTP_AKUN_TERDAFTAR,
                JenisBukti.UNLOCK_PERANGKAT,
                JenisBukti.RECOVERY_AKUN
        };
    }

    @Override
    public String tampilkanDetail() {
        String pelaporInfo = (getPelapor() != null)
                ? getPelapor().getNama() + " (" + getPelapor().getNomorIdentitas() + ")"
                : "-";
        String akunMask = (akunTerdaftar != null && !akunTerdaftar.isBlank())
                ? samarkanAkun(akunTerdaftar)
                : "(Belum dicantumkan - gunakan Unlock / Recovery)";
        return "=== DETAIL BARANG ELEKTRONIK (US-008 & v2.0) ===\n" +
                "ID Barang       : " + getId() + "\n" +
                "Jenis Laporan   : " + getJenis().name() + "\n" +
                "Nama Barang     : " + getNama() + "\n" +
                "Kategori        : " + getKategori().getLabel() + "\n" +
                "Merk / Tipe     : " + (merek != null && !merek.isBlank() ? merek : "-")
                + (tipe != null && !tipe.isBlank() ? " (" + tipe + ")" : "") + "\n" +
                "Bukti Utama     : Verifikasi Akun Terdaftar (OTP / Unlock / Recovery)\n" +
                "Akun Perangkat  : " + akunMask + "\n" +
                "Nomor Seri (SN) : " + (nomorSeri != null && !nomorSeri.isBlank() ? nomorSeri : "-") + " [RAHASIA PETUGAS / Bukti Cadangan]\n" +
                "Lokasi Kejadian : " + getLokasiKejadian() + "\n" +
                "Tanggal Kejadian: " + getTanggalKejadian() + "\n" +
                "Ciri Khusus     : " + (getCiriRahasia() != null && !getCiriRahasia().isBlank() ? getCiriRahasia() : "-") + "\n" +
                "Pelapor         : " + pelaporInfo + "\n" +
                "Status Barang   : " + getStatus().getLabel() + (isTerkunci() ? " [TERKUNCI]" : "");
    }

    private String samarkanAkun(String akun) {
        int atIdx = akun.indexOf('@');
        if (atIdx > 2) {
            return akun.substring(0, 2) + "***" + akun.substring(atIdx);
        }
        if (akun.length() > 4) {
            return akun.substring(0, 2) + "***" + akun.substring(akun.length() - 2);
        }
        return "***";
    }
}
