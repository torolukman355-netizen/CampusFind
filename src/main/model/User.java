package com.campusfind.model;

import java.io.Serializable;
import java.util.Objects;

public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nama;
    private final String nomorIdentitas;
    private final String email;
    private final String passwordHash;
    private boolean aktif;

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
     * sehingga pemanggil tidak perlu mengetahui kelas konkret pengguna.
     *
     * @param hakAkses hak yang ingin diperiksa
     * @return {@code true} jika role pengguna memiliki hak tersebut
     */
    public boolean punyaHak(HakAkses hakAkses) {
        return getRole().boleh(hakAkses);
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
