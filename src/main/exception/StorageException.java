package com.campusfind.exception;

/**
 * Dilempar ketika terjadi kegagalan pada lapisan penyimpanan (database).
 */
public class StorageException extends CampusFindException {

    private static final long serialVersionUID = 1L;

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}