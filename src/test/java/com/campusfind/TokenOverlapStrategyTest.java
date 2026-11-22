package com.campusfind;

import com.campusfind.service.MatchingStrategy;
import com.campusfind.service.TokenOverlapStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Pengujian TokenOverlapStrategy")
class TokenOverlapStrategyTest {

    private MatchingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new TokenOverlapStrategy();
    }

    @Test
    @DisplayName("Kecocokan persis menghasilkan skor 100")
    void testExactMatch() {
        int skor = strategy.skor("bluetooth airpods pro", "bluetooth airpods pro");
        assertEquals(100, skor);
    }

    @Test
    @DisplayName("Kecocokan parsial menghasilkan skor di antara 1 dan 99")
    void testPartialMatch() {
        // Ciri rahasia: "bluetooth", "airpods", "pro" (3 token)
        // Ciri khusus: "airpods", "warna", "putih"
        // Irisan: "airpods" (1 token) -> 1/3 * 100 = 33
        int skor = strategy.skor("bluetooth airpods pro", "airpods warna putih");
        assertTrue(skor > 0 && skor < 100);
        assertEquals(33, skor);
    }

    @Test
    @DisplayName("Tidak ada token cocok menghasilkan skor 0")
    void testNoMatch() {
        int skor = strategy.skor("bluetooth airpods", "samsung galaxy");
        assertEquals(0, skor);
    }

    @Test
    @DisplayName("Ciri rahasia null menghasilkan skor 0")
    void testNullRahasia() {
        int skor = strategy.skor(null, "anything");
        assertEquals(0, skor);
    }

    @Test
    @DisplayName("Ciri rahasia kosong menghasilkan skor 0")
    void testEmptyRahasia() {
        int skor = strategy.skor("", "anything");
        assertEquals(0, skor);
    }

    @Test
    @DisplayName("Token pendek (< 2 karakter) diabaikan")
    void testShortTokensIgnored() {
        // "a" dan "b" disaring karena panjangnya < 2. Sisa "cd" (1 token).
        // Irisan dengan "cd" adalah 1 / 1 * 100 = 100.
        int skor = strategy.skor("a b cd", "cd");
        assertEquals(100, skor);
    }
}
