package com.campusfind.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Validator untuk data pos penyimpanan.
 */
public final class PosValidator {

    private PosValidator() {
        // utility class
    }

    /**
     * Validasi nama pos (3–60 karakter, unik secara case-insensitive).
     */
    public static ValidationResult validateNama(String nama, Collection<String> existingNames) {
        if (nama == null || nama.trim().isEmpty()) {
            return ValidationResult.fail("Nama pos wajib diisi.");
        }

        String trimmed = nama.trim();

        if (trimmed.length() < 3) {
            return ValidationResult.fail("Nama pos minimal 3 karakter.");
        }

        if (trimmed.length() > 60) {
            return ValidationResult.fail("Nama pos maksimal 60 karakter.");
        }

        if (existingNames != null) {
            String lowerTrimmed = trimmed.toLowerCase(Locale.ROOT);
            for (String existing : existingNames) {
                if (existing != null && existing.trim().toLowerCase(Locale.ROOT).equals(lowerTrimmed)) {
                    return ValidationResult.fail("Nama pos sudah digunakan.");
                }
            }
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi lokasi pos (5–120 karakter).
     */
    public static ValidationResult validateLokasi(String lokasi) {
        if (lokasi == null || lokasi.trim().isEmpty()) {
            return ValidationResult.fail("Lokasi pos wajib diisi.");
        }

        String trimmed = lokasi.trim();

        if (trimmed.length() < 5) {
            return ValidationResult.fail("Lokasi pos minimal 5 karakter.");
        }

        if (trimmed.length() > 120) {
            return ValidationResult.fail("Lokasi pos maksimal 120 karakter.");
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi label pendek pos (2–16 karakter).
     */
    public static ValidationResult validateLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            return ValidationResult.fail("Label pos wajib diisi.");
        }

        String trimmed = label.trim();

        if (trimmed.length() < 2) {
            return ValidationResult.fail("Label pos minimal 2 karakter.");
        }

        if (trimmed.length() > 16) {
            return ValidationResult.fail("Label pos maksimal 16 karakter.");
        }

        return ValidationResult.ok();
    }

    /**
     * Menjalankan semua validasi pos sekaligus.
     *
     * @return {@link ValidationResult} gabungan dari semua validasi
     */
    public static ValidationResult validateAll(
            String nama,
            Collection<String> existingNames,
            String lokasi,
            String label) {

        List<String> allErrors = new ArrayList<>();

        collectErrors(allErrors, validateNama(nama, existingNames));
        collectErrors(allErrors, validateLokasi(lokasi));
        collectErrors(allErrors, validateLabel(label));

        return allErrors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(allErrors);
    }

    private static void collectErrors(List<String> target, ValidationResult result) {
        if (!result.isValid()) {
            target.addAll(result.getErrors());
        }
    }
}
