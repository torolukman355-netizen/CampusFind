package com.campusfind.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dilempar ketika input pengguna atau aturan transisi data tidak valid.
 */
public class ValidationException extends CampusFindException {

    private static final long serialVersionUID = 1L;
    private final Map<String, String> fieldErrors = new LinkedHashMap<>();

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(String field, String message) {
        super(message);
        if (field != null && !field.isBlank()) {
            this.fieldErrors.put(field, message);
        }
    }

    public ValidationException(Map<String, String> errors) {
        super(errors != null && !errors.isEmpty()
                ? errors.values().iterator().next()
                : "Data input tidak valid.");
        if (errors != null) {
            this.fieldErrors.putAll(errors);
        }
    }

    public Map<String, String> getFieldErrors() {
        return Collections.unmodifiableMap(fieldErrors);
    }
}