package com.campusfind.model;

import java.io.Serializable;

public class Mahasiswa extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    public Mahasiswa(String nama, String nim) {
        super(nama, nim);
    }
}
