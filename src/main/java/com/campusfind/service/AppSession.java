package com.campusfind.service;

/**
 * Menyimpan sesi pengguna aktif saat ini.
 * <p>
 * Bukan singleton — instance diteruskan melalui constructor
 * agar dapat digunakan dan diuji secara independen.
 */
public class AppSession {

    private String nomorIdentitas;
    private String nama;

    /**
     * Menetapkan identitas pengguna yang sedang login.
     *
     * @param nomor nomor identitas (NIM atau NIP)
     * @param nama  nama pengguna
     */
    public void setIdentitas(String nomor, String nama) {
        this.nomorIdentitas = nomor;
        this.nama = nama;
    }

    public String getNomorIdentitas() {
        return nomorIdentitas;
    }

    public String getNama() {
        return nama;
    }

    /**
     * Memeriksa apakah pengguna sedang login.
     *
     * @return {@code true} jika nomor identitas sudah diset
     */
    public boolean isLoggedIn() {
        return nomorIdentitas != null;
    }

    /**
     * Menghapus semua data sesi (logout).
     */
    public void clear() {
        this.nomorIdentitas = null;
        this.nama = null;
    }
}
