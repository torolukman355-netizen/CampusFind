package com.campusfind.service;

import com.campusfind.model.Item;
import com.campusfind.model.MatchResult;

/**
 * Kontrak strategi pencocokan kandidat antara laporan kehilangan (HILANG) dan barang temuan (TEMUAN).
 * Dipisahkan dari MatchingStrategy yang menilai ciri rahasia vs ciri khusus klaim (ADR-2).
 */
public interface CandidateMatchingStrategy {

    /**
     * Mengevaluasi kecocokan antara barang hilang dan barang temuan.
     *
     * @param hilang laporan barang hilang
     * @param temuan laporan barang temuan
     * @return {@link MatchResult} hasil evaluasi skor (0-100) dan daftar alasan kecocokan
     */
    MatchResult evaluate(Item hilang, Item temuan);
}
