package com.campusfind.service;

/**
 * Strategi pencocokan ciri rahasia terhadap ciri khusus yang diklaim.
 */
public interface MatchingStrategy {

    /**
     * Menghitung skor kecocokan antara ciri rahasia (dari pelapor)
     * dan ciri khusus (dari pengklaim).
     *
     * @param ciriRahasia  ciri rahasia yang disimpan oleh pelapor
     * @param ciriKhusus   ciri khusus yang dideskripsikan oleh pengklaim
     * @return skor kecocokan 0–100
     */
    int skor(String ciriRahasia, String ciriKhusus);
}
