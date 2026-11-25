package com.campusfind.exception;

/**
 * Dilempar ketika pengguna tidak memiliki hak akses yang dibutuhkan
 * untuk menjalankan sebuah operasi.
 */
public class AksesDitolakException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AksesDitolakException(String message) {
        super(message);
    }

    public AksesDitolakException(String message, Throwable cause) {
        super(message, cause);
    }
}
