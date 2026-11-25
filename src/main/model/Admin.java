package com.campusfind.model;

import java.io.Serializable;

/**
 * Pengguna dengan role {@link Role#ADMIN}.
 * <p>
 * Admin memiliki seluruh hak akses Petugas ditambah akses statistik layanan.
 * Nomor identitas admin berupa nama pengguna (bukan NIM/NIP).
 */
public class Admin extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    public Admin(String nama, String namaPengguna) {
        super(nama, namaPengguna);
    }

    public Admin(String nama, String namaPengguna, String email, String passwordHash, boolean aktif) {
        super(nama, namaPengguna, email, passwordHash, aktif);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
