package com.campusfind.service;

import com.campusfind.model.HakAkses;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Role;
import com.campusfind.model.User;

import java.util.Objects;

/**
 * Menyimpan sesi pengguna aktif saat ini, termasuk role dan hak aksesnya.
 * <p>
 * Bukan singleton — instance diteruskan melalui constructor
 * agar dapat digunakan dan diuji secara independen.
 */
public class AppSession {

    private User user;

    /**
     * Memulai sesi untuk pengguna yang sudah terautentikasi.
     */
    public void login(User user) {
        this.user = Objects.requireNonNull(user, "user tidak boleh null");
    }

    /**
     * Kompatibilitas alur lama: menetapkan identitas sebagai Mahasiswa.
     * Untuk login dengan role tertentu gunakan {@link #login(User)}.
     */
    public void setIdentitas(String nomor, String nama) {
        String namaPengguna = (nama == null || nama.isBlank())
                ? Role.MAHASISWA.getLabel() + " " + nomor
                : nama;
        login(new Mahasiswa(namaPengguna, nomor));
    }

    public User getUser() {
        return user;
    }

    /**
     * @return nomor identitas pengguna aktif, atau {@code null} jika belum login
     */
    public String getNomorIdentitas() {
        return user == null ? null : user.getNomorIdentitas();
    }

    /**
     * @return nama pengguna aktif, atau {@code null} jika belum login
     */
    public String getNama() {
        return user == null ? null : user.getNama();
    }

    /**
     * @return role pengguna aktif, atau {@code null} jika belum login
     */
    public Role getRole() {
        return user == null ? null : user.getRole();
    }

    /**
     * Memeriksa apakah pengguna aktif memiliki hak akses tertentu.
     */
    public boolean punyaHak(HakAkses hakAkses) {
        return user != null && user.punyaHak(hakAkses);
    }

    /**
     * Memeriksa apakah pengguna sedang login.
     *
     * @return {@code true} jika sudah ada pengguna aktif
     */
    public boolean isLoggedIn() {
        return user != null;
    }

    /**
     * Menghapus semua data sesi (logout).
     */
    public void clear() {
        this.user = null;
    }
}
