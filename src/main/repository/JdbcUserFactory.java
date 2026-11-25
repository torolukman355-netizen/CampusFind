package com.campusfind.repository;

import com.campusfind.exception.StorageException;
import com.campusfind.model.Admin;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Petugas;
import com.campusfind.model.Role;
import com.campusfind.model.User;

/**
 * Membangun objek {@link User} dari kolom-kolom yang tersimpan di database.
 * <p>
 * Aplikasi belum punya tabel akun tersendiri; identitas pelapor/pengklaim
 * disimpan bersama baris item/klaim agar data tetap lengkap.
 */
final class JdbcUserFactory {

    private JdbcUserFactory() {
    }

    static User fromRow(String role, String nama, String nomorIdentitas) {
        if (nomorIdentitas == null || nomorIdentitas.isBlank()) {
            return null;
        }
        String namaTampilan = (nama == null || nama.isBlank()) ? nomorIdentitas : nama;
        Role rolePengguna;
        try {
            rolePengguna = role == null ? null : Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            rolePengguna = null;
        }
        if (rolePengguna == null) {
            rolePengguna = Role.dariNomorIdentitas(nomorIdentitas);
        }
        return switch (rolePengguna) {
            case PETUGAS -> new Petugas(namaTampilan, nomorIdentitas);
            case ADMIN -> new Admin(namaTampilan, nomorIdentitas);
            case MAHASISWA -> new Mahasiswa(namaTampilan, nomorIdentitas);
        };
    }

    static String roleOf(User user) {
        return user == null ? null : user.getRole().name();
    }

    static String namaOf(User user) {
        return user == null ? null : user.getNama();
    }

    static String nomorOf(User user) {
        return user == null ? null : user.getNomorIdentitas();
    }
}
