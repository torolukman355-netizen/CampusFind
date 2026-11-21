package com.campusfind;

import com.campusfind.validation.IdentitasValidator;
import com.campusfind.validation.ValidationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian IdentitasValidator")
class IdentitasValidatorTest {

    @Test
    @DisplayName("Input kosong atau null ditolak dengan pesan yang benar")
    void testEmpty() {
        ValidationResult resNull = IdentitasValidator.validate(null);
        assertFalse(resNull.isValid());
        assertEquals("Nomor identitas wajib diisi.", resNull.getFirstError());

        ValidationResult resEmpty = IdentitasValidator.validate("");
        assertFalse(resEmpty.isValid());
        assertEquals("Nomor identitas wajib diisi.", resEmpty.getFirstError());

        ValidationResult resBlank = IdentitasValidator.validate("   ");
        assertFalse(resBlank.isValid());
        assertEquals("Nomor identitas wajib diisi.", resBlank.getFirstError());
    }

    @Test
    @DisplayName("Input non-digit ditolak")
    void testNonDigit() {
        ValidationResult res = IdentitasValidator.validate("ABC123456");
        assertFalse(res.isValid());
        assertEquals("Hanya boleh berisi angka.", res.getFirstError());

        ValidationResult resSymbol = IdentitasValidator.validate("1234-5678");
        assertFalse(resSymbol.isValid());
        assertEquals("Hanya boleh berisi angka.", resSymbol.getFirstError());
    }

    @Test
    @DisplayName("Panjang 8 digit ditolak (kurang dari 9)")
    void testTooShort8Digits() {
        ValidationResult res = IdentitasValidator.validate("12345678");
        assertFalse(res.isValid());
        assertEquals("NIM harus 9 digit atau NIP harus 18 digit.", res.getFirstError());
    }

    @Test
    @DisplayName("Panjang 9 digit (NIM Mahasiswa) diterima")
    void testValid9Digits() {
        ValidationResult res = IdentitasValidator.validate("123456789");
        assertTrue(res.isValid());
        assertTrue(res.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Panjang 10 digit ditolak")
    void testTooLong10Digits() {
        ValidationResult res = IdentitasValidator.validate("1234567890");
        assertFalse(res.isValid());
        assertEquals("NIM harus 9 digit atau NIP harus 18 digit.", res.getFirstError());
    }

    @Test
    @DisplayName("Panjang 17 digit ditolak")
    void test17Digits() {
        ValidationResult res = IdentitasValidator.validate("12345678901234567");
        assertFalse(res.isValid());
        assertEquals("NIM harus 9 digit atau NIP harus 18 digit.", res.getFirstError());
    }

    @Test
    @DisplayName("Panjang 18 digit (NIP Petugas) diterima")
    void testValid18Digits() {
        ValidationResult res = IdentitasValidator.validate("123456789012345678");
        assertTrue(res.isValid());
        assertTrue(res.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Panjang 19 digit ditolak")
    void test19Digits() {
        ValidationResult res = IdentitasValidator.validate("1234567890123456789");
        assertFalse(res.isValid());
        assertEquals("NIM harus 9 digit atau NIP harus 18 digit.", res.getFirstError());
    }

    @Test
    @DisplayName("Input dengan spasi di awal/akhir otomatis di-trim")
    void testWithSpaces() {
        ValidationResult res = IdentitasValidator.validate(" 123456789 ");
        assertTrue(res.isValid());
    }
}
