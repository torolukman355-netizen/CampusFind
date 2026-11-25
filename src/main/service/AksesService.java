package com.campusfind.service;

import com.campusfind.exception.AksesDitolakException;
import com.campusfind.model.HakAkses;
import com.campusfind.model.Role;

import java.util.Objects;

/**
 * Pusat kebijakan hak akses aplikasi.
 * <p>
 * Semua pemeriksaan otorisasi dilakukan lewat kelas ini agar aturan
 * akses tidak tersebar di lapisan UI.
 */
public class AksesService {

    private final AppSession session;

    public AksesService(AppSession session) {
        this.session = Objects.requireNonNull(session, "session tidak boleh null");
    }

    /**
     * @return {@code true} jika pengguna aktif memiliki hak tersebut
     */
    public boolean boleh(HakAkses hakAkses) {
        return session.punyaHak(hakAkses);
    }

    /**
     * Memastikan sudah ada pengguna yang login.
     *
     * @throws AksesDitolakException jika belum login
     */
    public void wajibLogin() {
        if (!session.isLoggedIn()) {
            throw new AksesDitolakException("Silakan login terlebih dahulu.");
        }
    }

    /**
     * Memastikan pengguna aktif memiliki hak akses tertentu.
     *
     * @throws AksesDitolakException jika belum login atau role tidak memiliki hak tersebut
     */
    public void wajibPunyaHak(HakAkses hakAkses) {
        wajibLogin();
        if (!session.punyaHak(hakAkses)) {
            throw new AksesDitolakException(pesanTolak(hakAkses));
        }
    }

    /**
     * Memastikan pengguna aktif memiliki role tertentu.
     *
     * @throws AksesDitolakException jika role tidak sesuai
     */
    public void wajibRole(Role role) {
        wajibLogin();
        if (session.getRole() != role) {
            throw new AksesDitolakException("Fitur ini hanya dapat diakses oleh " + role.getLabel() + ".");
        }
    }

    private String pesanTolak(HakAkses hakAkses) {
        Role role = session.getRole();
        String namaRole = role == null ? "Pengguna" : role.getLabel();
        return "Role " + namaRole + " tidak memiliki hak akses: " + hakAkses.getLabel() + ".";
    }
}
