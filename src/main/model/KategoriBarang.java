package com.campusfind.model;

<<<<<<< HEAD:src/main/model/KategoriBarang.java
import java.io.Serializable;

/**
 * Kategori barang. Hanya dibedakan menjadi barang elektronik dan
 * non-elektronik; kategori elektronik memakai subclass {@link BarangElektronik}.
 */
public enum KategoriBarang implements Serializable {
=======
public enum KategoriBarang {
>>>>>>> ca2e27ff81bd0eaf0f0496ef3f356f4006abcbb8:src/main/java/com/campusfind/model/KategoriBarang.java
    ELEKTRONIK("Elektronik"),
    NON_ELEKTRONIK("Non-Elektronik");

    private final String label;

    KategoriBarang(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
