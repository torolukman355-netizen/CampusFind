package com.campusfind.model;

import java.io.Serializable;

public class Petugas extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    public Petugas(String nama, String nip) {
        super(nama, nip);
    }
}
