package com.campusfind.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Pasangan barang temuan beserta hasil evaluasi kecocokan dengan laporan kehilangan.
 */
public class KandidatCocokItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Item itemTemuan;
    private final MatchResult matchResult;

    public KandidatCocokItem(Item itemTemuan, MatchResult matchResult) {
        this.itemTemuan = Objects.requireNonNull(itemTemuan, "itemTemuan tidak boleh null");
        this.matchResult = Objects.requireNonNull(matchResult, "matchResult tidak boleh null");
    }

    public Item getItemTemuan() {
        return itemTemuan;
    }

    public MatchResult getMatchResult() {
        return matchResult;
    }

    public int getSkor() {
        return matchResult.getSkor();
    }
}
