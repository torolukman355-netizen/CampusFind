package com.campusfind.validation;

import com.campusfind.model.SesiLayanan;

import java.io.File;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validator untuk pengajuan klaim barang.
 */
public final class KlaimValidator {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");
    private static final int CIRI_KHUSUS_MIN = 10;
    private static final int CIRI_KHUSUS_MAX = 500;
    private static final int MAX_HARI_KE_DEPAN = 30;

    private KlaimValidator() {
        // utility class
    }

    /**
     * Validasi nomor identitas, didelegasikan ke {@link IdentitasValidator}.
     */
    public static ValidationResult validateNomorIdentitas(String nomorIdentitas) {
        return IdentitasValidator.validate(nomorIdentitas);
    }

    /**
     * Validasi ciri khusus barang.
     */
    public static ValidationResult validateCiriKhusus(String ciriKhusus) {
        if (ciriKhusus == null || ciriKhusus.trim().isEmpty()) {
            return ValidationResult.fail("Ciri khusus wajib diisi.");
        }

        String trimmed = ciriKhusus.trim();

        if (trimmed.length() < CIRI_KHUSUS_MIN) {
            return ValidationResult.fail("Jelaskan minimal 10 karakter.");
        }

        if (trimmed.length() > CIRI_KHUSUS_MAX) {
            return ValidationResult.fail("Maksimal 500 karakter.");
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi berkas bukti (wajib diunggah).
     */
    public static ValidationResult validateBukti(File bukti) {
        if (bukti == null) {
            return ValidationResult.fail("Bukti kepemilikan wajib diunggah.");
        }

        if (!bukti.exists() || !bukti.canRead()) {
            return ValidationResult.fail("Berkas tidak dapat dibaca.");
        }

        String name = bukti.getName().toLowerCase(Locale.ROOT);
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx < 0 || !ALLOWED_EXTENSIONS.contains(name.substring(dotIdx + 1))) {
            return ValidationResult.fail("Format berkas harus JPG, PNG, atau PDF.");
        }

        if (bukti.length() > MAX_FILE_SIZE) {
            return ValidationResult.fail("Ukuran berkas melebihi 10MB.");
        }

        String ext = name.substring(dotIdx + 1);
        if (!isValidMagicNumber(bukti, ext)) {
            return ValidationResult.fail("Berkas tidak sesuai dengan format ekstensi.");
        }

        return ValidationResult.ok();
    }

    private static boolean isValidMagicNumber(File file, String ext) {
        try (java.io.InputStream is = java.nio.file.Files.newInputStream(file.toPath())) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 3) {
                return false;
            }
            if ("jpg".equals(ext) || "jpeg".equals(ext)) {
                // JPEG magic bytes: FF D8 FF
                return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF;
            }
            if (read < 4) {
                return false;
            }
            if ("pdf".equals(ext)) {
                // PDF magic bytes: %PDF (25 50 44 46)
                return header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46;
            }
            if ("png".equals(ext)) {
                // PNG magic bytes: 89 50 4E 47
                return (header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50 && (header[2] & 0xFF) == 0x4E && (header[3] & 0xFF) == 0x47;
            }
            return false;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    /**
     * Validasi tanggal serah-terima.
     */
    public static ValidationResult validateTanggal(LocalDate tanggal, Clock clock) {
        if (tanggal == null) {
            return ValidationResult.fail("Tanggal tidak valid.");
        }

        LocalDate today = LocalDate.now(clock);

        if (tanggal.isBefore(today)) {
            return ValidationResult.fail("Tanggal tidak boleh sebelum hari ini.");
        }

        if (tanggal.isAfter(today.plusDays(MAX_HARI_KE_DEPAN))) {
            return ValidationResult.fail("Tanggal maksimal 30 hari ke depan.");
        }

        DayOfWeek day = tanggal.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return ValidationResult.fail("Layanan hanya tersedia Senin\u2013Jumat.");
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi sesi layanan. Jika tanggal adalah hari ini dan sesi sudah dimulai, ditolak.
     */
    public static ValidationResult validateSesi(LocalDate tanggal, SesiLayanan sesi, Clock clock) {
        if (tanggal == null || sesi == null) {
            return ValidationResult.fail("Tanggal dan sesi wajib dipilih.");
        }

        LocalDate today = LocalDate.now(clock);
        if (tanggal.isEqual(today)) {
            LocalTime now = LocalTime.now(clock);
            if (!now.isBefore(sesi.getMulai())) {
                return ValidationResult.fail("Sesi ini sudah lewat.");
            }
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi checkbox pernyataan.
     */
    public static ValidationResult validateCheckbox(boolean identitas, boolean kepemilikan) {
        List<String> errors = new ArrayList<>();

        if (!identitas) {
            errors.add("Centang pernyataan ini untuk melanjutkan.");
        }
        if (!kepemilikan) {
            errors.add("Centang pernyataan ini untuk melanjutkan.");
        }

        return errors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(errors);
    }

    /**
     * Menjalankan semua validasi klaim sekaligus.
     *
     * @return {@link ValidationResult} gabungan dari semua validasi
     */
    public static ValidationResult validateAll(
            String nomorIdentitas,
            String ciriKhusus,
            File bukti,
            LocalDate tanggal,
            SesiLayanan sesi,
            boolean identitasChecked,
            boolean kepemilikanChecked,
            Clock clock) {

        List<String> allErrors = new ArrayList<>();

        collectErrors(allErrors, validateNomorIdentitas(nomorIdentitas));
        collectErrors(allErrors, validateCiriKhusus(ciriKhusus));
        collectErrors(allErrors, validateBukti(bukti));
        collectErrors(allErrors, validateTanggal(tanggal, clock));
        collectErrors(allErrors, validateSesi(tanggal, sesi, clock));
        collectErrors(allErrors, validateCheckbox(identitasChecked, kepemilikanChecked));

        return allErrors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(allErrors);
    }

    private static void collectErrors(List<String> target, ValidationResult result) {
        if (!result.isValid()) {
            target.addAll(result.getErrors());
        }
    }
}
