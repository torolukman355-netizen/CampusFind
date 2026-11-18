package model;

/**
 * US-015: Rekomendasi pasangan barang hilang dan temuan dengan skor kemiripan >= 70%.
 */
public class MatchRecord {
    private final String idHilang;
    private final String namaHilang;
    private final String idTemuan;
    private final String namaTemuan;
    private final int skor;

    public MatchRecord(String idHilang, String namaHilang, String idTemuan, String namaTemuan, int skor) {
        this.idHilang = idHilang;
        this.namaHilang = namaHilang;
        this.idTemuan = idTemuan;
        this.namaTemuan = namaTemuan;
        this.skor = skor;
    }

    public String getIdHilang() { return idHilang; }
    public String getNamaHilang() { return namaHilang; }
    public String getIdTemuan() { return idTemuan; }
    public String getNamaTemuan() { return namaTemuan; }
    public int getSkor() { return skor; }
}
