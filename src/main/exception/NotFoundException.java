package com.campusfind.exception;

/**
 * Dilempar ketika data barang, pengguna, atau klaim tidak ditemukan.
 */
public class NotFoundException extends CampusFindException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}