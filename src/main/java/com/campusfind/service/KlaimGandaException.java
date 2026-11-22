package com.campusfind.service;

/**
 * Dilempar ketika barang yang akan diklaim sudah memiliki klaim aktif (DIAJUKAN atau DISETUJUI).
 */
public class KlaimGandaException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public KlaimGandaException(String message) {
        super(message);
    }
}
