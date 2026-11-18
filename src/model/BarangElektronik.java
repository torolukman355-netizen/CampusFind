package model;

import model.enums.JenisLaporan;
import model.enums.KategoriItem;

/**
 * US-007, US-008: Subclass Barang Elektronik dengan atribut merk dan nomor seri.
 * Nomor seri bersifat rahasia dari publik untuk memvalidasi klaim kepemilikan.
 */
public class BarangElektronik extends Item {
    private String merk;
    private String nomorSeri;

    public BarangElektronik(String id, JenisLaporan jenis, String nama,
                            String lokasiKejadian, String pelapor, String petugasAwal,
                            String merk, String nomorSeri) {
        super(id, jenis, nama, KategoriItem.ELEKTRONIK, lokasiKejadian, pelapor, petugasAwal);
        this.merk = merk;
        this.nomorSeri = nomorSeri;
    }

    public String getMerk() { return merk; }
    public void setMerk(String merk) { this.merk = merk; }

    public String getNomorSeri() { return nomorSeri; }
    public void setNomorSeri(String nomorSeri) { this.nomorSeri = nomorSeri; }

    @Override
    public String tampilkanDetail() {
        return "=== DETAIL BARANG ELEKTRONIK ===\n" +
                "ID Barang       : " + getId() + "\n" +
                "Jenis Laporan   : " + getJenis().getLabel() + "\n" +
                "Nama Barang     : " + getNama() + "\n" +
                "Kategori        : " + getKategori().getLabel() + "\n" +
                "Merk            : " + (merk != null && !merk.isEmpty() ? merk : "-") + "\n" +
                "Nomor Seri (SN) : " + (nomorSeri != null && !nomorSeri.isEmpty() ? nomorSeri : "-") + " [RAHASIA PETUGAS]\n" +
                "Lokasi Kejadian : " + getLokasiKejadian() + "\n" +
                "Lokasi Simpan   : " + getLokasiSimpan() + "\n" +
                "Kode QR         : " + getKodeQR() + "\n" +
                "Pelapor         : " + getPelapor() + "\n" +
                "Status Barang   : " + getStatus() + (isTerkunci() ? " [TERKUNCI]" : "") + "\n\n" +
                "--- Riwayat Transisi Status (US-012) ---\n" +
                getRiwayatLogFormatted();
    }
}
