package com.campusfind.exception;

/**
 * Kelas dasar (induk) untuk seluruh exception kustom di CampusFind.
 */
public class CampusFindException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CampusFindException(String message) {
        super(message);
    }

    public CampusFindException(String message, Throwable cause) {
        super(message, cause);
    }
}