package com.campusfind.validation;

import com.campusfind.model.Role;

/**
 * Validator untuk nomor identitas (NIM / NIP / nama pengguna admin).
 */
public final class IdentitasValidator {

    public static final int NIM_LENGTH = 9;
    public static final int NIP_LENGTH = 18;

    private IdentitasValidator() {
        // utility class
    }

    /**
     * Memvalidasi nomor identitas tanpa memandang role
     * (NIM 9 digit atau NIP 18 digit).
     *
     * @param nomorIdentitas nomor identitas yang akan divalidasi
     * @return hasil validasi
     */
    public static ValidationResult validate(String nomorIdentitas) {
        if (isBlank(nomorIdentitas)) {
            return ValidationResult.fail("Nomor identitas wajib diisi.");
        }

        String trimmed = nomorIdentitas.trim();

        if (!trimmed.matches("\\d+")) {
            return ValidationResult.fail("Hanya boleh berisi angka.");
        }

        if (trimmed.length() != NIM_LENGTH && trimmed.length() != NIP_LENGTH) {
            return ValidationResult.fail("NIM harus 9 digit atau NIP harus 18 digit.");
        }

        return ValidationResult.ok();
    }

    /**
     * Memvalidasi identitas sesuai aturan role yang dipilih saat login.
     * <ul>
     *     <li>Mahasiswa: NIM tepat 9 digit</li>
     *     <li>Petugas: NIP tepat 18 digit</li>
     *     <li>Admin: nama pengguna 3–32 karakter</li>
     * </ul>
     *
     * @param nomorIdentitas nomor identitas / nama pengguna
     * @param role           role yang dipilih
     * @return hasil validasi
     */
    public static ValidationResult validate(String nomorIdentitas, Role role) {
        if (role == null) {
            return validate(nomorIdentitas);
        }

        if (isBlank(nomorIdentitas)) {
            return ValidationResult.fail(role == Role.ADMIN
                    ? "Nama pengguna admin wajib diisi."
                    : "Nomor identitas wajib diisi.");
        }

        String trimmed = nomorIdentitas.trim();

        if (role == Role.ADMIN) {
            if (!trimmed.matches("[A-Za-z0-9._-]{3,32}")) {
                return ValidationResult.fail("Nama pengguna admin 3-32 karakter (huruf, angka, titik, garis bawah, atau strip).");
            }
            return ValidationResult.ok();
        }

        if (!trimmed.matches("\\d+")) {
            return ValidationResult.fail("Hanya boleh berisi angka.");
        }

        if (trimmed.length() != role.getPanjangIdentitas()) {
            return ValidationResult.fail(role == Role.MAHASISWA
                    ? "NIM Mahasiswa harus 9 digit."
                    : "NIP Petugas harus 18 digit.");
        }

        return ValidationResult.ok();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
