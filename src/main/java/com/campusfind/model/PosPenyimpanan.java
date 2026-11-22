package com.campusfind.model;

import java.io.Serializable;
import java.util.Objects;

public class PosPenyimpanan implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final String nama;
    private final String lokasiPenyerahan;
    private final String labelPendek;

    public PosPenyimpanan(String id, String nama, String lokasiPenyerahan, String labelPendek) {
        this.id = id;
        this.nama = nama;
        this.lokasiPenyerahan = lokasiPenyerahan;
        this.labelPendek = labelPendek;
    }

    public String getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getLokasiPenyerahan() {
        return lokasiPenyerahan;
    }

    public String getLabelPendek() {
        return labelPendek;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PosPenyimpanan that = (PosPenyimpanan) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
