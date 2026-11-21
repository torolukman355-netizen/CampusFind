package com.campusfind.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Hasil validasi sederhana yang membawa status valid/tidak dan daftar pesan kesalahan.
 */
public final class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    private ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
    }

    /** Validasi berhasil, tanpa kesalahan. */
    public static ValidationResult ok() {
        return new ValidationResult(true, Collections.emptyList());
    }

    /** Validasi gagal dengan satu pesan kesalahan. */
    public static ValidationResult fail(String error) {
        List<String> list = new ArrayList<>();
        list.add(error);
        return new ValidationResult(false, list);
    }

    /** Validasi gagal dengan beberapa pesan kesalahan. */
    public static ValidationResult fail(List<String> errors) {
        return new ValidationResult(false, errors);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    /** Mengembalikan pesan kesalahan pertama, atau {@code null} jika tidak ada. */
    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }
}
