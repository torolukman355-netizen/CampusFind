package model;

import model.enums.StatusKlaim;

import java.time.LocalDateTime;

/**
 * US-019 & US-020: Model Pengajuan Klaim Barang oleh Mahasiswa yang diperiksa Petugas.
 */
public class Klaim {
    private final String idKlaim;
    private final String idBarang;
    private final String pengaju;
    private final String bukti;
    private StatusKlaim statusKlaim;
    private String catatanPetugas;
    private final LocalDateTime tanggalPengajuan;

    public Klaim(String idKlaim, String idBarang, String pengaju, String bukti) {
        this.idKlaim = idKlaim;
        this.idBarang = idBarang;
        this.pengaju = pengaju;
        this.bukti = bukti;
        this.statusKlaim = StatusKlaim.DIAJUKAN;
        this.catatanPetugas = "-";
        this.tanggalPengajuan = LocalDateTime.now();
    }

    public String getIdKlaim() { return idKlaim; }
    public String getIdBarang() { return idBarang; }
    public String getPengaju() { return pengaju; }
    public String getBukti() { return bukti; }
    public StatusKlaim getStatusKlaim() { return statusKlaim; }
    public void setStatusKlaim(StatusKlaim statusKlaim) { this.statusKlaim = statusKlaim; }
    public String getCatatanPetugas() { return catatanPetugas; }
    public void setCatatanPetugas(String catatanPetugas) { this.catatanPetugas = catatanPetugas; }
    public LocalDateTime getTanggalPengajuan() { return tanggalPengajuan; }
}
