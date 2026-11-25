package com.campusfind.validation;

import com.campusfind.model.JenisLaporan;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validator untuk formulir pelaporan barang hilang/temuan.
 */
public final class LaporanValidator {

    private static final long MAX_FOTO_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_FOTO_EXT = Set.of("jpg", "jpeg", "png");

    private LaporanValidator() {
        // utility class
    }

    /**
     * Validasi nama barang (3–80 karakter).
     */
    public static ValidationResult validateNama(String nama) {
        if (nama == null || nama.trim().isEmpty()) {
            return ValidationResult.fail("Nama barang wajib diisi.");
        }

        String trimmed = nama.trim();

        if (trimmed.length() < 3) {
            return ValidationResult.fail("Nama barang minimal 3 karakter.");
        }

        if (trimmed.length() > 80) {
            return ValidationResult.fail("Nama barang maksimal 80 karakter.");
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi lokasi kejadian (3–120 karakter).
     */
    public static ValidationResult validateLokasi(String lokasi) {
        if (lokasi == null || lokasi.trim().isEmpty()) {
            return ValidationResult.fail("Lokasi wajib diisi.");
        }

        String trimmed = lokasi.trim();

        if (trimmed.length() < 3) {
            return ValidationResult.fail("Lokasi minimal 3 karakter.");
        }

        if (trimmed.length() > 120) {
            return ValidationResult.fail("Lokasi maksimal 120 karakter.");
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi deskripsi (maks 500 karakter).
     */
    public static ValidationResult validateDeskripsi(String deskripsi) {
        if (deskripsi == null || deskripsi.trim().isEmpty()) {
            return ValidationResult.fail("Deskripsi wajib diisi.");
        }

        String trimmed = deskripsi.trim();

        if (trimmed.length() > 500) {
            return ValidationResult.fail("Deskripsi maksimal 500 karakter.");
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi tanggal kejadian: tidak boleh di masa depan.
     */
    public static ValidationResult validateTanggalKejadian(LocalDate tanggal, Clock clock) {
        if (tanggal == null) {
            return ValidationResult.fail("Tanggal kejadian wajib diisi.");
        }

        LocalDate today = LocalDate.now(clock);

        if (tanggal.isAfter(today)) {
            return ValidationResult.fail("Tanggal kejadian tidak boleh di masa depan.");
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi tempat penitipan / pos penyimpanan: wajib untuk jenis TEMUAN.
     */
    public static ValidationResult validatePos(String posId, JenisLaporan jenis) {
        if (jenis == JenisLaporan.TEMUAN) {
            if (posId == null || posId.trim().isEmpty()) {
                return ValidationResult.fail("Tempat penitipan barang wajib diisi untuk barang temuan.");
            }
        }
        return ValidationResult.ok();
    }

    /**
     * Validasi ciri rahasia (opsional; jika diisi minimal 5 karakter).
     */
    public static ValidationResult validateCiriRahasia(String ciriRahasia) {
        if (ciriRahasia == null || ciriRahasia.trim().isEmpty()) {
            return ValidationResult.ok();
        }

        String trimmed = ciriRahasia.trim();

        if (trimmed.length() < 5) {
            return ValidationResult.fail("Ciri rahasia minimal 5 karakter.");
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi foto (opsional). Format harus JPG/PNG valid, ukuran maks 10 MB,
     * dan harus dapat dibaca oleh {@link ImageIO}.
     */
    public static ValidationResult validateFoto(File foto) {
        if (foto == null) {
            return ValidationResult.ok();
        }

        if (!foto.exists() || !foto.canRead()) {
            return ValidationResult.fail("Berkas foto tidak dapat dibaca.");
        }

        String name = foto.getName().toLowerCase(Locale.ROOT);
        int dotIdx = name.lastIndexOf('.');
        if (dotIdx < 0 || !ALLOWED_FOTO_EXT.contains(name.substring(dotIdx + 1))) {
            return ValidationResult.fail("Format foto harus JPG atau PNG yang valid.");
        }

        if (foto.length() > MAX_FOTO_SIZE) {
            return ValidationResult.fail("Ukuran foto melebihi 10MB.");
        }

        try {
            BufferedImage img = ImageIO.read(foto);
            if (img == null) {
                return ValidationResult.fail("Format foto harus JPG atau PNG yang valid.");
            }
        } catch (IOException e) {
            return ValidationResult.fail("Format foto harus JPG atau PNG yang valid.");
        }

        return ValidationResult.ok();
    }

    /**
     * Validasi nomor identitas, didelegasikan ke {@link IdentitasValidator}.
     */
    public static ValidationResult validateNomorIdentitas(String nomorIdentitas) {
        return IdentitasValidator.validate(nomorIdentitas);
    }

    /**
     * Validasi nama pelapor (3–80 karakter).
     */
    public static ValidationResult validateNamaPelapor(String namaPelapor) {
        if (namaPelapor == null || namaPelapor.trim().isEmpty()) {
            return ValidationResult.fail("Nama pelapor wajib diisi.");
        }

        String trimmed = namaPelapor.trim();

        if (trimmed.length() < 3) {
            return ValidationResult.fail("Nama pelapor minimal 3 karakter.");
        }

        if (trimmed.length() > 80) {
            return ValidationResult.fail("Nama pelapor maksimal 80 karakter.");
        }

        return ValidationResult.ok();
    }

    /**
     * Menjalankan semua validasi laporan sekaligus.
     *
     * @return {@link ValidationResult} gabungan dari semua validasi
     */
    public static ValidationResult validateAll(
            String nama,
            String lokasi,
            String deskripsi,
            LocalDate tanggalKejadian,
            String posId,
            JenisLaporan jenis,
            String ciriRahasia,
            File foto,
            String nomorIdentitas,
            String namaPelapor,
            Clock clock) {

        List<String> allErrors = new ArrayList<>();

        collectErrors(allErrors, validateNama(nama));
        collectErrors(allErrors, validateLokasi(lokasi));
        collectErrors(allErrors, validateDeskripsi(deskripsi));
        collectErrors(allErrors, validateTanggalKejadian(tanggalKejadian, clock));
        collectErrors(allErrors, validatePos(posId, jenis));
        collectErrors(allErrors, validateCiriRahasia(ciriRahasia));
        collectErrors(allErrors, validateFoto(foto));
        collectErrors(allErrors, validateNomorIdentitas(nomorIdentitas));
        collectErrors(allErrors, validateNamaPelapor(namaPelapor));

        return allErrors.isEmpty() ? ValidationResult.ok() : ValidationResult.fail(allErrors);
    }

    private static void collectErrors(List<String> target, ValidationResult result) {
        if (!result.isValid()) {
            target.addAll(result.getErrors());
        }
    }
}
