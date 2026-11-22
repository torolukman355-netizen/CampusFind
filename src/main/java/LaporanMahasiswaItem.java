import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LaporanMahasiswaItem {
    private final String idLaporan;
    private final String namaMahasiswa; // Untuk filter privasi dashboard
    private final String jenisLaporan; // "Hilang" atau "Temuan"
    private final String namaBarang;
    private final String kategori; // "Elektronik" atau "Non-Elektronik"
    private final String warna;
    private final String lokasiKejadian;
    private final String keteranganLokasi;
    private final LocalDateTime waktuKejadian;
    private final String deskripsi;
    private final String pathFoto; // Opsional

    // Atribut dinamis Elektronik
    private final String merk;
    private final String nomorSeri;

    // Atribut dinamis Non-Elektronik
    private final String bahanJenis;
    private final String ciriFisik;

    // Status alur: DILAPORKAN -> DIVERIFIKASI -> COCOK -> DIKLAIM -> DIKEMBALIKAN
    private String status;

    public LaporanMahasiswaItem(
            String idLaporan,
            String namaMahasiswa,
            String jenisLaporan,
            String namaBarang,
            String kategori,
            String warna,
            String lokasiKejadian,
            String keteranganLokasi,
            LocalDateTime waktuKejadian,
            String deskripsi,
            String pathFoto,
            String merk,
            String nomorSeri,
            String bahanJenis,
            String ciriFisik,
            String status) {
        this.idLaporan = idLaporan;
        this.namaMahasiswa = namaMahasiswa;
        this.jenisLaporan = jenisLaporan;
        this.namaBarang = namaBarang;
        this.kategori = kategori;
        this.warna = warna;
        this.lokasiKejadian = lokasiKejadian;
        this.keteranganLokasi = keteranganLokasi;
        this.waktuKejadian = waktuKejadian;
        this.deskripsi = deskripsi;
        this.pathFoto = pathFoto;
        this.merk = merk;
        this.nomorSeri = nomorSeri;
        this.bahanJenis = bahanJenis;
        this.ciriFisik = ciriFisik;
        this.status = status;
    }

    public String getIdLaporan() {
        return idLaporan;
    }

    public String getNamaMahasiswa() {
        return namaMahasiswa;
    }

    public String getJenisLaporan() {
        return jenisLaporan;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public String getKategori() {
        return kategori;
    }

    public String getWarna() {
        return warna;
    }

    public String getLokasiKejadian() {
        return lokasiKejadian;
    }

    public String getKeteranganLokasi() {
        return keteranganLokasi;
    }

    public LocalDateTime getWaktuKejadian() {
        return waktuKejadian;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public String getPathFoto() {
        return pathFoto;
    }

    public String getMerk() {
        return merk;
    }

    public String getNomorSeri() {
        return nomorSeri;
    }

    public String getBahanJenis() {
        return bahanJenis;
    }

    public String getCiriFisik() {
        return ciriFisik;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWaktuKejadianFormatted() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return waktuKejadian != null ? waktuKejadian.format(fmt) : "-";
    }
}