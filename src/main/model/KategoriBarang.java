package com.campusfind.model;

import java.io.Serializable;

/**
 * Kategori barang. Hanya dibedakan menjadi barang elektronik dan
 * non-elektronik; kategori elektronik memakai subclass {@link BarangElektronik}.
 */
public enum KategoriBarang implements Serializable {
    ELEKTRONIK("Elektronik"),
    NON_ELEKTRONIK("Non-Elektronik");

    private static final long serialVersionUID = 1L;

    private final String label;

    KategoriBarang(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
