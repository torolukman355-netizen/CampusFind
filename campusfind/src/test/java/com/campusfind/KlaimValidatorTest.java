package com.campusfind;

import com.campusfind.model.SesiLayanan;
import com.campusfind.validation.KlaimValidator;
import com.campusfind.validation.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian KlaimValidator")
class KlaimValidatorTest {

    private Clock clock;
    private ZoneId zone;

    @BeforeEach
    void setUp() {
        zone = ZoneId.of("Asia/Jakarta");
        // 2026-11-20 is a Friday
        Instant fixedInstant = LocalDate.of(2026, 11, 20)
                .atTime(10, 0, 0)
                .atZone(zone)
                .toInstant();
        clock = Clock.fixed(fixedInstant, zone);
    }

    private File createValidJpeg(Path dir, String filename, long size) throws IOException {
        File file = dir.resolve(filename).toFile();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.write(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
            if (size > 3) {
                raf.setLength(size);
            }
        }
        return file;
    }

    @Test
    @DisplayName("Tanggal di masa lalu ditolak")
    void testTanggalPast() {
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);
        ValidationResult res = KlaimValidator.validateTanggal(yesterday, clock);
        assertFalse(res.isValid());
        assertEquals("Tanggal tidak boleh sebelum hari ini.", res.getFirstError());
    }

    @Test
    @DisplayName("Layanan hari Minggu ditolak")
    void testTanggalSunday() {
        // 2026-11-22 is Sunday
        LocalDate sunday = LocalDate.of(2026, 11, 22);
        ValidationResult res = KlaimValidator.validateTanggal(sunday, clock);
        assertFalse(res.isValid());
        assertEquals("Layanan hanya tersedia Senin\u2013Jumat.", res.getFirstError());
    }

    @Test
    @DisplayName("Sesi pagi hari ini pada pukul 10:00 sudah lewat")
    void testSesiLewatToday() {
        LocalDate today = LocalDate.now(clock);
        ValidationResult res = KlaimValidator.validateSesi(today, SesiLayanan.PAGI, clock);
        assertFalse(res.isValid());
        assertEquals("Sesi ini sudah lewat.", res.getFirstError());
    }

    @Test
    @DisplayName("Sesi siang hari ini pada pukul 10:00 masih valid")
    void testSesiSiangTodayOk() {
        LocalDate today = LocalDate.now(clock);
        ValidationResult res = KlaimValidator.validateSesi(today, SesiLayanan.SIANG, clock);
        assertTrue(res.isValid());
    }

    @Test
    @DisplayName("Berkas bukti tepat 10MB diterima jika magic number valid")
    void testBuktiExact10MB(@TempDir Path tempDir) throws IOException {
        File file = createValidJpeg(tempDir, "test.jpg", 10L * 1024 * 1024);
        ValidationResult res = KlaimValidator.validateBukti(file);
        assertTrue(res.isValid());
    }

    @Test
    @DisplayName("Berkas bukti melebihi 10MB (10MB + 1 byte) ditolak")
    void testBuktiOver10MB(@TempDir Path tempDir) throws IOException {
        File file = createValidJpeg(tempDir, "over.jpg", 10L * 1024 * 1024 + 1);
        ValidationResult res = KlaimValidator.validateBukti(file);
        assertFalse(res.isValid());
        assertEquals("Ukuran berkas melebihi 10MB.", res.getFirstError());
    }

    @Test
    @DisplayName("Berkas bukti null ditolak (bukti wajib)")
    void testBuktiNullDitolak() {
        ValidationResult res = KlaimValidator.validateBukti(null);
        assertFalse(res.isValid());
        assertEquals("Bukti kepemilikan wajib diunggah.", res.getFirstError());
    }

    @Test
    @DisplayName("Format berkas ekstensi tidak didukung ditolak")
    void testBuktiWrongExtension(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("gambar.gif").toFile();
        assertTrue(file.createNewFile());
        ValidationResult res = KlaimValidator.validateBukti(file);
        assertFalse(res.isValid());
        assertEquals("Format berkas harus JPG, PNG, atau PDF.", res.getFirstError());
    }

    @Test
    @DisplayName("Berkas dengan ekstensi palsu (bukan JPEG/PNG/PDF asli) ditolak")
    void testBuktiEkstensiPalsuDitolak(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("palsu.jpg").toFile();
        Files.writeString(file.toPath(), "Ini file teks biasa yang dinamai .jpg");
        ValidationResult res = KlaimValidator.validateBukti(file);
        assertFalse(res.isValid());
        assertEquals("Berkas tidak sesuai dengan format ekstensi.", res.getFirstError());
    }

    @Test
    @DisplayName("Berkas PNG dan PDF dengan magic number valid diterima")
    void testBuktiValidFormats(@TempDir Path tempDir) throws IOException {
        File pngFile = tempDir.resolve("gambar.png").toFile();
        Files.write(pngFile.toPath(), new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        assertTrue(KlaimValidator.validateBukti(pngFile).isValid());

        File pdfFile = tempDir.resolve("dokumen.pdf").toFile();
        Files.write(pdfFile.toPath(), new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34});
        assertTrue(KlaimValidator.validateBukti(pdfFile).isValid());
    }

    @Test
    @DisplayName("Ciri khusus kurang dari 10 karakter ditolak")
    void testCiriKhususTooShort() {
        ValidationResult res = KlaimValidator.validateCiriKhusus("abc");
        assertFalse(res.isValid());
        assertEquals("Jelaskan minimal 10 karakter.", res.getFirstError());
    }

    @Test
    @DisplayName("Checkbox pernyataan tidak dicentang ditolak")
    void testCheckboxNotChecked() {
        ValidationResult res = KlaimValidator.validateCheckbox(false, false);
        assertFalse(res.isValid());
        assertEquals("Centang pernyataan ini untuk melanjutkan.", res.getFirstError());
        assertEquals(2, res.getErrors().size());
    }
}
