package com.campusfind.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Klaim implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String itemId;
    private final User pengklaim;
    private final String ciriKhusus;
    private String buktiPath;
    private byte[] buktiBytes;
    private final LocalDate tanggalSerahTerima;
    private final SesiLayanan sesi;
    private final String posId;
    private final boolean pernyataanIdentitasAsli;
    private final boolean pernyataanKepemilikan;
    private final int skorKecocokan;
    private StatusKlaim status;
    private final LocalDateTime diajukanPada;
    private String catatanPetugas;

    public Klaim(String id, String itemId, User pengklaim, String ciriKhusus,
                 String buktiPath, LocalDate tanggalSerahTerima, SesiLayanan sesi,
                 String posId, boolean pernyataanIdentitasAsli, boolean pernyataanKepemilikan,
                 int skorKecocokan, StatusKlaim status, LocalDateTime diajukanPada,
                 String catatanPetugas) {
        this.id = id;
        this.itemId = itemId;
        this.pengklaim = pengklaim;
        this.ciriKhusus = ciriKhusus;
        this.buktiPath = buktiPath;
        this.tanggalSerahTerima = tanggalSerahTerima;
        this.sesi = sesi;
        this.posId = posId;
        this.pernyataanIdentitasAsli = pernyataanIdentitasAsli;
        this.pernyataanKepemilikan = pernyataanKepemilikan;
        this.skorKecocokan = skorKecocokan;
        this.status = status;
        this.diajukanPada = diajukanPada;
        this.catatanPetugas = catatanPetugas;
    }

    public String getId() {
        return id;
    }

    public String getItemId() {
        return itemId;
    }

    public User getPengklaim() {
        return pengklaim;
    }

    public String getCiriKhusus() {
        return ciriKhusus;
    }

    public String getBuktiPath() {
        return buktiPath;
    }

    public LocalDate getTanggalSerahTerima() {
        return tanggalSerahTerima;
    }

    public SesiLayanan getSesi() {
        return sesi;
    }

    public String getPosId() {
        return posId;
    }

    public boolean isPernyataanIdentitasAsli() {
        return pernyataanIdentitasAsli;
    }

    public boolean isPernyataanKepemilikan() {
        return pernyataanKepemilikan;
    }

    public int getSkorKecocokan() {
        return skorKecocokan;
    }

    public StatusKlaim getStatus() {
        return status;
    }

    public LocalDateTime getDiajukanPada() {
        return diajukanPada;
    }

    public String getCatatanPetugas() {
        return catatanPetugas;
    }

    public String getAlasanPenolakan() {
        return catatanPetugas;
    }

    public void setStatus(StatusKlaim status) {
        this.status = status;
    }

    public void setCatatanPetugas(String catatanPetugas) {
        this.catatanPetugas = catatanPetugas;
    }

    public void setAlasanPenolakan(String alasanPenolakan) {
        this.catatanPetugas = alasanPenolakan;
    }

    public void setBuktiPath(String buktiPath) {
        this.buktiPath = buktiPath;
    }

    public byte[] getBuktiBytes() {
        return buktiBytes != null ? buktiBytes.clone() : null;
    }

    public void setBuktiBytes(byte[] buktiBytes) {
        this.buktiBytes = buktiBytes != null ? buktiBytes.clone() : null;
    }

    public boolean hasBuktiBytes() {
        return buktiBytes != null && buktiBytes.length > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Klaim klaim = (Klaim) o;
        return Objects.equals(id, klaim.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
