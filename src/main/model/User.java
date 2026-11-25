package com.campusfind.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nama;
    private final String nomorIdentitas;
    private final String email;
    private final String passwordHash;
    private boolean aktif;
    protected final Set<HakAkses> daftarHakAkses = new HashSet<>();

    protected User(String nama, String nomorIdentitas) {
        this(nama, nomorIdentitas, null, null, true);
    }

    protected User(String nama, String nomorIdentitas, String email, String passwordHash, boolean aktif) {
        this.nama = nama;
        this.nomorIdentitas = nomorIdentitas;
        this.email = (email != null && !email.isBlank()) ? email.trim() : generateDefaultEmail(nomorIdentitas);
        this.passwordHash = passwordHash;
        this.aktif = aktif;
    }

    private static String generateDefaultEmail(String nomorIdentitas) {
        if (nomorIdentitas == null || nomorIdentitas.isBlank()) {
            return "user@kampus.ac";
        }
        return nomorIdentitas.toLowerCase().replaceAll("[^a-z0-9]", "") + "@kampus.ac";
    }

    public String getNama() {
        return nama;
    }

    public String getNomorIdentitas() {
        return nomorIdentitas;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isAktif() {
        return aktif;
    }

    public void setAktif(boolean aktif) {
        this.aktif = aktif;
    }

    /**
     * Role pengguna, ditentukan oleh subclass masing-masing.
     */
    public abstract Role getRole();

    /**
     * Memeriksa hak akses pengguna. Keputusan diambil dari {@link Role}
     * atau {@code daftarHakAkses} sehingga pemanggil tidak perlu mengetahui kelas konkret pengguna.
     *
     * @param hakAkses hak yang ingin diperiksa
     * @return {@code true} jika role pengguna memiliki hak tersebut
     */
    public boolean punyaHak(HakAkses hakAkses) {
        if (hakAkses == null) return false;
        if (daftarHakAkses.contains(hakAkses)) return true;
        return getRole() != null && getRole().boleh(hakAkses);
    }

    /**
     * Alias kompatibilitas untuk memeriksa hak akses.
     */
    public boolean punyaAkses(HakAkses hakAkses) {
        return punyaHak(hakAkses);
    }

    public Set<HakAkses> getDaftarHakAkses() {
        if (!daftarHakAkses.isEmpty()) {
            return Collections.unmodifiableSet(daftarHakAkses);
        }
        return getRole() != null ? getRole().getHakAkses() : Collections.emptySet();
    }

    public String tampilDashboard() {
        return "Dashboard Pengguna: " + getNama() + " (" + getRole().getLabel() + ")";
    }

    /**
     * Label role untuk ditampilkan di antarmuka.
     */
    public String getLabelRole() {
        return getRole().getLabel();
    }

    @Override
    public String toString() {
        return nama + " (" + getRole().getLabel() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(nomorIdentitas, user.nomorIdentitas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomorIdentitas);
    }
}
