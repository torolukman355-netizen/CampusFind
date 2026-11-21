package com.campusfind.validation;

/**
 * Validator untuk nomor identitas (NIM / NIP).
 */
public final class IdentitasValidator {

    public static final int NIM_LENGTH = 9;
    public static final int NIP_LENGTH = 18;

    private IdentitasValidator() {
        // utility class
    }

    /**
     * Memvalidasi nomor identitas.
     *
     * @param nomorIdentitas nomor identitas yang akan divalidasi
     * @return hasil validasi
     */
    public static ValidationResult validate(String nomorIdentitas) {
        if (nomorIdentitas == null || nomorIdentitas.trim().isEmpty()) {
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
}
