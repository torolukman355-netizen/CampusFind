package com.campusfind.exception;

/**
 * Dilempar ketika terjadi kegagalan pada lapisan penyimpanan (database).
 * <p>
 * {@code SQLException} dibungkus ke exception ini agar detail teknis JDBC
 * tidak bocor ke lapisan UI.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
