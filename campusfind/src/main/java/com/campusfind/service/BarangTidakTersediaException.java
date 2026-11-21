package com.campusfind.service;

/**
 * Dilempar ketika barang yang akan diklaim tidak ditemukan atau tidak berstatus TERSEDIA.
 */
public class BarangTidakTersediaException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public BarangTidakTersediaException(String message) {
        super(message);
    }
}
