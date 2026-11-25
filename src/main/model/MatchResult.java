package com.campusfind.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Hasil evaluasi kecocokan kandidat antara laporan kehilangan dan barang temuan (immutable).
 */
public final class MatchResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String itemTemuanId;
    private final int skor;
    private final List<String> alasan;

    public MatchResult(String itemTemuanId, int skor, List<String> alasan) {
        this.itemTemuanId = Objects.requireNonNull(itemTemuanId, "itemTemuanId tidak boleh null");
        this.skor = Math.max(0, Math.min(100, skor));
        this.alasan = (alasan != null) ? List.copyOf(alasan) : List.of();
    }

    public String getItemTemuanId() {
        return itemTemuanId;
    }

    public int getSkor() {
        return skor;
    }

    public List<String> getAlasan() {
        return alasan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MatchResult that = (MatchResult) o;
        return skor == that.skor &&
                Objects.equals(itemTemuanId, that.itemTemuanId) &&
                Objects.equals(alasan, that.alasan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemTemuanId, skor, alasan);
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "itemTemuanId='" + itemTemuanId + '\'' +
                ", skor=" + skor +
                ", alasan=" + alasan +
                '}';
    }
}
