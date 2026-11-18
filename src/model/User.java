package model;

import model.enums.HakAkses;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * US-001 & US-002: Base Class User untuk hierarki role pengguna CampusFind.
 */
public abstract class User {
    protected String id;
    protected String nama;
    protected String email;
    protected String password;
    protected boolean aktif;
    protected Set<HakAkses> daftarHakAkses = new HashSet<>();

    public User(String id, String nama, String email, String password) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.aktif = true;
    }

    public boolean punyaAkses(HakAkses hakAkses) {
        return aktif && daftarHakAkses.contains(hakAkses);
    }

    public String getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAktif() {
        return aktif;
    }

    public void setAktif(boolean aktif) {
        this.aktif = aktif;
    }

    public Set<HakAkses> getDaftarHakAkses() {
        return Collections.unmodifiableSet(daftarHakAkses);
    }

    public abstract String getRole();

    // US-010: Polimorfisme tampilan dashboard sesuai role masing-masing
    public abstract String tampilDashboard();
}
