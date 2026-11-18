package model;

import model.enums.JenisLaporan;
import model.enums.KategoriItem;

/**
 * US-007, US-008: Subclass Barang Non-Elektronik dengan atribut bahan dan ciri fisik khusus.
 */
public class BarangNonElektronik extends Item {
    private String bahan;
    private String ciriFisik;

    public BarangNonElektronik(String id, JenisLaporan jenis, String nama,
                               String lokasiKejadian, String pelapor, String petugasAwal,
                               String bahan, String ciriFisik) {
        super(id, jenis, nama, KategoriItem.NON_ELEKTRONIK, lokasiKejadian, pelapor, petugasAwal);
        this.bahan = bahan;
        this.ciriFisik = ciriFisik;
    }

    public String getBahan() { return bahan; }
    public void setBahan(String bahan) { this.bahan = bahan; }

    public String getCiriFisik() { return ciriFisik; }
    public void setCiriFisik(String ciriFisik) { this.ciriFisik = ciriFisik; }

    @Override
    public String tampilkanDetail() {
        return "=== DETAIL BARANG NON-ELEKTRONIK ===\n" +
                "ID Barang       : " + getId() + "\n" +
                "Jenis Laporan   : " + getJenis().getLabel() + "\n" +
                "Nama Barang     : " + getNama() + "\n" +
                "Kategori        : " + getKategori().getLabel() + "\n" +
                "Bahan/Material  : " + (bahan != null && !bahan.isEmpty() ? bahan : "-") + "\n" +
                "Ciri Fisik Unik : " + (ciriFisik != null && !ciriFisik.isEmpty() ? ciriFisik : "-") + "\n" +
                "Lokasi Kejadian : " + getLokasiKejadian() + "\n" +
                "Lokasi Simpan   : " + getLokasiSimpan() + "\n" +
                "Kode QR         : " + getKodeQR() + "\n" +
                "Pelapor         : " + getPelapor() + "\n" +
                "Status Barang   : " + getStatus() + (isTerkunci() ? " [TERKUNCI]" : "") + "\n\n" +
                "--- Riwayat Transisi Status (US-012) ---\n" +
                getRiwayatLogFormatted();
    }
}
