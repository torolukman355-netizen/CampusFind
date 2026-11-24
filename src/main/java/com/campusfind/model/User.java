package com.campusfind.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Superclass User mewakili entitas pengguna dalam sistem CampusFind.
 * Mendukung kontrol hak akses peran (Role-Based Access Control).
 */
public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nama;
    private final String nomorIdentitas;
    protected final Set<HakAkses> daftarHakAkses = new HashSet<>();

    protected User(String nama, String nomorIdentitas) {
        this.nama = nama;
        this.nomorIdentitas = nomorIdentitas;
    }

    public String getNama() {
        return nama;
    }

    public String getNomorIdentitas() {
        return nomorIdentitas;
    }

    public boolean punyaAkses(HakAkses hakAkses) {
        if (hakAkses == null) return false;
        return daftarHakAkses.contains(hakAkses);
    }

    public Set<HakAkses> getDaftarHakAkses() {
        return Collections.unmodifiableSet(daftarHakAkses);
    }

    public String getRole() {
        return getClass().getSimpleName();
    }

    public String tampilDashboard() {
        return "Dashboard Pengguna: " + getNama() + " (" + getRole() + ")";
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

    @Override
    public String toString() {
        return getRole() + "{" +
                "nama='" + nama + '\'' +
                ", id='" + nomorIdentitas + '\'' +
                '}';
    }
}
