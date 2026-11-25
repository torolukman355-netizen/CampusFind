package com.campusfind.model;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Klaim implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final SecureRandom OTP_RNG = new SecureRandom();

    private final String id;
    private final String itemId;
    private final User pengklaim;
    private final String ciriKhusus;
    private String buktiPath;
    private final LocalDate tanggalSerahTerima;
    private final SesiLayanan sesi;
    private final String posId;
    private final boolean pernyataanIdentitasAsli;
    private final boolean pernyataanKepemilikan;
    private final int skorKecocokan;
    private StatusKlaim status;
    private final LocalDateTime diajukanPada;
    private String catatanPetugas;

    // Upgrade Dokumen Konsep v2.0: Verifikasi Challenge-Response (OTP / Bukti Kepemilikan)
    private String kodeOTP;
    private boolean otpTerverifikasi;
    private JenisBukti jenisBukti = JenisBukti.CIRI_FISIK;

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

    // --- Method v2.0 Challenge-Response Verification ---

    public boolean ajukan() {
        if (this.status == null) {
            this.status = StatusKlaim.DIAJUKAN;
        }
        return true;
    }

    /**
     * Menghasilkan tantangan kode OTP 6-digit untuk verifikasi akun terdaftar perangkat elektronik.
     */
    public String kirimOTP() {
        int code = 100000 + OTP_RNG.nextInt(900000);
        this.kodeOTP = String.valueOf(code);
        this.otpTerverifikasi = false;
        this.jenisBukti = JenisBukti.OTP_AKUN_TERDAFTAR;
        return this.kodeOTP;
    }

    /**
     * Pemilik membuktikan penguasaan akun terdaftar dengan memasukkan OTP yang diterima.
     * Petugas tidak perlu melihat isi perangkat maupun akun; cukup melihat hasil boolean ini.
     */
    public boolean cekOTP(String input) {
        if (this.kodeOTP == null || input == null) {
            return false;
        }
        boolean cocok = this.kodeOTP.equals(input.trim());
        if (cocok) {
            this.otpTerverifikasi = true;
        }
        return cocok;
    }

    public void ubahStatusKlaim(StatusKlaim statusBaru) {
        this.status = Objects.requireNonNull(statusBaru, "Status klaim baru tidak boleh null");
    }

    public String getKodeOTP() {
        return kodeOTP;
    }

    public boolean isOtpTerverifikasi() {
        return otpTerverifikasi;
    }

    public void setOtpTerverifikasi(boolean otpTerverifikasi) {
        this.otpTerverifikasi = otpTerverifikasi;
    }

    public JenisBukti getJenisBukti() {
        return jenisBukti != null ? jenisBukti : JenisBukti.CIRI_FISIK;
    }

    public void setJenisBukti(JenisBukti jenisBukti) {
        this.jenisBukti = jenisBukti;
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
