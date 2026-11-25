package com.campusfind.service;

import com.campusfind.model.HakAkses;
import com.campusfind.model.Item;
import com.campusfind.model.JenisBukti;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.Klaim;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.NotificationType;
import com.campusfind.model.Petugas;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.SesiLayanan;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.model.User;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.repository.NotificationRepository;
import com.campusfind.repository.PosRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logika bisnis untuk pengajuan dan pengelolaan klaim barang.
 */
public class KlaimService {

    private static final Logger LOG = Logger.getLogger(KlaimService.class.getName());

    private final KlaimRepository klaimRepository;
    private final ItemRepository itemRepository;
    private final PosRepository posRepository;
    private final NotificationService notificationService;
    private final MatchingStrategy matchingStrategy;
    private final Clock clock;

    @SuppressWarnings("deprecation")
    private static boolean isStatusDiajukan(StatusKlaim status) {
        return status == StatusKlaim.DIAJUKAN || status == StatusKlaim.MENUNGGU_VERIFIKASI;
    }

    public KlaimService(KlaimRepository klaimRepository,
                        ItemRepository itemRepository,
                        PosRepository posRepository,
                        NotificationService notificationService,
                        MatchingStrategy matchingStrategy,
                        Clock clock) {
        this.klaimRepository = klaimRepository;
        this.itemRepository = itemRepository;
        this.posRepository = posRepository;
        this.notificationService = notificationService;
        this.matchingStrategy = matchingStrategy;
        this.clock = clock;
    }

    public KlaimService(KlaimRepository klaimRepository,
                        ItemRepository itemRepository,
                        PosRepository posRepository,
                        NotificationRepository notificationRepository,
                        MatchingStrategy matchingStrategy,
                        Clock clock) {
        this(klaimRepository, itemRepository, posRepository,
             new NotificationService(notificationRepository, clock),
             matchingStrategy, clock);
    }

    /**
     * Mengajukan klaim atas suatu barang temuan.
     * <p>
     * Method ini disinkronisasi untuk memastikan atomisitas:
     * hanya satu klaim aktif per item pada satu waktu.
     *
     * @return klaim yang berhasil diajukan
     * @throws BarangTidakTersediaException jika barang tidak ada, bukan temuan, atau status bukan TERSEDIA
     * @throws KlaimGandaException           jika sudah ada klaim aktif pada barang ini
     * @throws IllegalStateException        jika pelapor mengklaim barang sendiri
     */
    public synchronized Klaim ajukan(String itemId,
                                     String nomorIdentitas,
                                     String namaPengklaim,
                                     String ciriKhusus,
                                     File buktiFile,
                                     LocalDate tanggal,
                                     SesiLayanan sesi,
                                     boolean pernyataanIdentitas,
                                     boolean pernyataanKepemilikan) {

        // 1. Item harus ada, berjenis TEMUAN, dan berstatus TERSEDIA
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new BarangTidakTersediaException("Barang ini sudah tidak tersedia untuk diklaim."));

        if (item.getJenis() != JenisLaporan.TEMUAN
                || (item.getStatus() != StatusItem.TERSEDIA
                    && item.getStatus() != StatusItem.DIVERIFIKASI
                    && item.getStatus() != StatusItem.COCOK)) {
            throw new BarangTidakTersediaException("Barang ini sudah tidak tersedia untuk diklaim.");
        }

        // 2. Pengklaim tidak boleh sama dengan pelapor
        if (nomorIdentitas.equals(item.getPelapor().getNomorIdentitas())) {
            throw new IllegalStateException("Pelapor barang tidak dapat mengklaim barangnya sendiri.");
        }

        // 3. Double-check: tidak ada klaim aktif untuk item ini
        boolean adaKlaimAktif = klaimRepository.findAll().stream()
                .anyMatch(k -> k.getItemId().equals(itemId)
                        && (isStatusDiajukan(k.getStatus())
                            || k.getStatus() == StatusKlaim.DISETUJUI));
        if (adaKlaimAktif) {
            throw new KlaimGandaException("Barang ini sedang dalam proses klaim aktif.");
        }

        // Validasi bukti wajib
        if (buktiFile == null) {
            throw new IllegalArgumentException("Bukti kepemilikan wajib diunggah.");
        }

        // 4. Hitung skor kecocokan
        int skorKecocokan = matchingStrategy.skor(item.getCiriRahasia(), ciriKhusus);

        // 5. Buat User berdasarkan panjang nomor identitas
        User pengklaim;
        if (nomorIdentitas.length() == ServicePolicy.NIM_LENGTH) {
            pengklaim = new Mahasiswa(namaPengklaim, nomorIdentitas);
        } else {
            pengklaim = new Petugas(namaPengklaim, nomorIdentitas);
        }

        // 6. Salin bukti file
        String buktiPath = copyBukti(buktiFile);
        Path copiedBukti = Path.of(buktiPath);

        // Simpan status lama untuk rollback
        StatusItem statusLama = item.getStatus();
        String klaimId = null;

        try {
            // 7. Buat klaim dan ubah status item
            int tahun = LocalDate.now(clock).getYear();
            klaimId = klaimRepository.nextId(tahun);

            // Ambil pos untuk notifikasi
            PosPenyimpanan pos = posRepository.findById(item.getPosId()).orElse(null);

            Klaim klaim = new Klaim(
                    klaimId, itemId, pengklaim, ciriKhusus, buktiPath,
                    tanggal, sesi,
                    item.getPosId(),
                    pernyataanIdentitas, pernyataanKepemilikan,
                    skorKecocokan, StatusKlaim.DIAJUKAN,
                    LocalDateTime.now(clock), null);

            item.setStatus(StatusItem.DALAM_PROSES_KLAIM);
            itemRepository.save(item);
            klaimRepository.save(klaim);

            // 8. Buat notifikasi untuk pengklaim via NotificationService & NotificationMessages (ADR-4, ADR-5)
            String isiNotifikasi = NotificationMessages.isiKlaimDiterima(item, klaim, pos, tanggal, sesi);
            notificationService.kirim(
                    nomorIdentitas,
                    NotificationType.KLAIM_DITERIMA,
                    NotificationMessages.judulKlaimDiterima(),
                    isiNotifikasi,
                    klaim.getId()
            );

            LOG.info(() -> "Klaim diajukan: " + klaim.getId() + " untuk item " + item.kode());
            return klaim;

        } catch (Exception e) {
            // 9. Rollback atomik: kembalikan status item, hapus klaim hantu (G5), dan hapus file bukti
            LOG.log(Level.SEVERE, "Gagal menyimpan klaim, melakukan rollback", e);
            item.setStatus(statusLama);
            try {
                itemRepository.save(item);
            } catch (Exception rollbackEx) {
                LOG.log(Level.SEVERE, "Gagal rollback status item", rollbackEx);
            }
            if (klaimId != null) {
                try {
                    klaimRepository.delete(klaimId);
                } catch (Exception deleteEx) {
                    LOG.log(Level.SEVERE, "Gagal rollback menghapus klaim", deleteEx);
                }
            }
            if (copiedBukti != null) {
                try {
                    Files.deleteIfExists(copiedBukti);
                } catch (IOException ioEx) {
                    LOG.log(Level.WARNING, "Gagal menghapus file bukti saat rollback", ioEx);
                }
            }
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Gagal mengajukan klaim: " + e.getMessage(), e);
        }
    }

    /**
     * Menyetujui klaim oleh Petugas.
     * Hanya berlaku untuk klaim berstatus DIAJUKAN.
     * Klaim aktif lain pada item yang sama otomatis ditolak.
     *
     * @param klaimId ID klaim yang disetujui
     * @param aktor   petugas yang menyetujui klaim
     */
    public synchronized void setujui(String klaimId, Petugas aktor) {
        if (aktor == null || !aktor.punyaAkses(HakAkses.KEPUTUSAN_KLAIM)) {
            throw new SecurityException("Aksi persetujuan klaim hanya dapat dilakukan oleh Security / Petugas Verifikator berwenang.");
        }

        Klaim klaim = klaimRepository.findById(klaimId).orElseThrow(() ->
                new IllegalArgumentException("Klaim tidak ditemukan."));

        if (!isStatusDiajukan(klaim.getStatus())) {
            throw new IllegalStateException("Hanya klaim dengan status Diajukan yang dapat disetujui.");
        }

        klaim.setStatus(StatusKlaim.DISETUJUI);
        klaim.setCatatanPetugas("Disetujui oleh " + aktor.getNama());
        klaimRepository.save(klaim);

        Item item = itemRepository.findById(klaim.getItemId()).orElse(null);
        if (item != null) {
            item.setStatus(StatusItem.DALAM_PROSES_KLAIM);
            itemRepository.save(item);
        }

        // Otomatis tolak klaim aktif lain pada item yang sama
        String alasanAutoTolak = "Otomatis: klaim lain untuk barang ini telah disetujui";
        List<Klaim> klaimLain = klaimRepository.findAll().stream()
                .filter(k -> k.getItemId().equals(klaim.getItemId())
                        && !k.getId().equals(klaimId)
                        && isStatusDiajukan(k.getStatus()))
                .toList();

        for (Klaim lain : klaimLain) {
            lain.setStatus(StatusKlaim.DITOLAK);
            lain.setCatatanPetugas(alasanAutoTolak);
            klaimRepository.save(lain);

            String isiTolak = NotificationMessages.isiKlaimDitolak(item, lain, alasanAutoTolak);
            notificationService.kirim(
                    lain.getPengklaim().getNomorIdentitas(),
                    NotificationType.KLAIM_DITOLAK,
                    NotificationMessages.judulKlaimDitolak(),
                    isiTolak,
                    lain.getId()
            );
        }

        // Notifikasi ke pengklaim yang disetujui
        String isiSetuju = NotificationMessages.isiKlaimDisetujui(item, klaim);
        notificationService.kirim(
                klaim.getPengklaim().getNomorIdentitas(),
                NotificationType.KLAIM_DISETUJUI,
                NotificationMessages.judulKlaimDisetujui(),
                isiSetuju,
                klaim.getId()
        );

        LOG.info(() -> "Klaim " + klaimId + " disetujui oleh " + aktor.getNama());
    }

    /**
     * Menolak klaim oleh Petugas.
     * Hanya berlaku untuk klaim berstatus DIAJUKAN. Alasan penolakan wajib diisi (minimal 5 karakter).
     * Status barang kembali menjadi TERSEDIA.
     *
     * @param klaimId ID klaim yang ditolak
     * @param alasan  alasan penolakan
     * @param aktor   petugas yang menolak klaim
     */
    public synchronized void tolak(String klaimId, String alasan, Petugas aktor) {
        if (aktor == null || !aktor.punyaAkses(HakAkses.KEPUTUSAN_KLAIM)) {
            throw new SecurityException("Aksi penolakan klaim hanya dapat dilakukan oleh Security / Petugas Verifikator berwenang.");
        }

        if (alasan == null || alasan.trim().length() < 5) {
            throw new IllegalArgumentException("Alasan penolakan wajib diisi (minimal 5 karakter).");
        }

        Klaim klaim = klaimRepository.findById(klaimId).orElseThrow(() ->
                new IllegalArgumentException("Klaim tidak ditemukan."));

        if (!isStatusDiajukan(klaim.getStatus())) {
            throw new IllegalStateException("Hanya klaim dengan status Diajukan yang dapat ditolak.");
        }

        klaim.setStatus(StatusKlaim.DITOLAK);
        klaim.setCatatanPetugas(alasan.trim());
        klaimRepository.save(klaim);

        // Kembalikan status item menjadi TERSEDIA
        Item item = itemRepository.findById(klaim.getItemId()).orElse(null);
        if (item != null) {
            item.setStatus(StatusItem.TERSEDIA);
            itemRepository.save(item);
        }

        // Notifikasi ke pengklaim
        String isiTolak = NotificationMessages.isiKlaimDitolak(item, klaim, alasan.trim());
        notificationService.kirim(
                klaim.getPengklaim().getNomorIdentitas(),
                NotificationType.KLAIM_DITOLAK,
                NotificationMessages.judulKlaimDitolak(),
                isiTolak,
                klaim.getId()
        );

        LOG.info(() -> "Klaim " + klaimId + " ditolak oleh " + aktor.getNama() + ". Alasan: " + alasan.trim());
    }

    /**
     * Membatalkan klaim yang sedang diajukan.
     *
     * @param klaimId        ID klaim
     * @param nomorIdentitas nomor identitas pengklaim
     */
    public synchronized void batalkan(String klaimId, String nomorIdentitas) {
        Klaim klaim = klaimRepository.findById(klaimId).orElseThrow(() ->
                new IllegalArgumentException("Klaim tidak ditemukan."));

        if (!isStatusDiajukan(klaim.getStatus())) {
            throw new IllegalStateException("Hanya klaim dengan status Diajukan yang dapat dibatalkan.");
        }

        if (!klaim.getPengklaim().getNomorIdentitas().equals(nomorIdentitas)) {
            throw new IllegalStateException("Anda tidak memiliki akses untuk membatalkan klaim ini.");
        }

        klaim.setStatus(StatusKlaim.DIBATALKAN);
        klaimRepository.save(klaim);

        // Kembalikan status item menjadi TERSEDIA
        itemRepository.findById(klaim.getItemId()).ifPresent(item -> {
            item.setStatus(StatusItem.TERSEDIA);
            itemRepository.save(item);
        });

        LOG.info(() -> "Klaim dibatalkan: " + klaimId);
    }

    /**
     * Mencari semua klaim berdasarkan nomor identitas pengklaim.
     */
    public List<Klaim> findByPengklaim(String nomorIdentitas) {
        return klaimRepository.findByPengklaim(nomorIdentitas);
    }

    /**
     * Dokumen Konsep v2.0 (Section 3.5): Mengirim tantangan OTP ke akun terdaftar pemilik barang elektronik.
     */
    public synchronized String kirimTantanganOTP(String klaimId, Petugas aktor) {
        if (aktor == null || !aktor.punyaAkses(HakAkses.KEPUTUSAN_KLAIM)) {
            throw new SecurityException("Hanya Security / Verifikator yang dapat mengirim tantangan OTP.");
        }
        Klaim klaim = klaimRepository.findById(klaimId).orElseThrow(() ->
                new IllegalArgumentException("Klaim tidak ditemukan."));
        String otp = klaim.kirimOTP();
        klaimRepository.save(klaim);

        notificationService.kirim(
                klaim.getPengklaim().getNomorIdentitas(),
                NotificationType.KLAIM_DITERIMA,
                "Kode OTP Verifikasi Kepemilikan",
                "Kode OTP verifikasi kepemilikan Anda untuk klaim " + klaim.getId() + " adalah: " + otp + ". Tunjukkan ke Security.",
                klaim.getId()
        );
        return otp;
    }

    /**
     * Dokumen Konsep v2.0 (Section 3.5): Verifikasi Challenge-Response OTP oleh pemilik.
     */
    public synchronized boolean verifikasiTantanganOTP(String klaimId, String inputOtp) {
        Klaim klaim = klaimRepository.findById(klaimId).orElseThrow(() ->
                new IllegalArgumentException("Klaim tidak ditemukan."));
        boolean cocok = klaim.cekOTP(inputOtp);
        if (cocok) {
            klaim.setCatatanPetugas("Challenge-Response OTP Terverifikasi (COCOK)");
        }
        klaimRepository.save(klaim);
        return cocok;
    }

    /**
     * Alternatif bukti elektronik v2.0: Pemilik berhasil membuka kunci layar atau memverifikasi recovery akun di hadapan Security.
     */
    public synchronized void verifikasiBuktiPerangkatTerbuka(String klaimId, JenisBukti metode, Petugas aktor) {
        if (aktor == null || !aktor.punyaAkses(HakAkses.KEPUTUSAN_KLAIM)) {
            throw new SecurityException("Hanya Security / Verifikator yang dapat mengonfirmasi pembuktian perangkat.");
        }
        Klaim klaim = klaimRepository.findById(klaimId).orElseThrow(() ->
                new IllegalArgumentException("Klaim tidak ditemukan."));
        klaim.setJenisBukti(metode != null ? metode : JenisBukti.UNLOCK_PERANGKAT);
        klaim.setOtpTerverifikasi(true);
        klaim.setCatatanPetugas("Terverifikasi via " + klaim.getJenisBukti().getLabel() + " oleh " + aktor.getNama());
        klaimRepository.save(klaim);
    }

    /**
     * Menyalin berkas bukti ke data/bukti/{UUID}.{ext}.
     */
    private String copyBukti(File source) {
        try {
            String originalName = source.getName();
            String ext = "";
            int dotIdx = originalName.lastIndexOf('.');
            if (dotIdx >= 0) {
                ext = originalName.substring(dotIdx + 1).toLowerCase(Locale.ROOT);
            }

            String targetName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path targetDir = Path.of("data", "bukti");
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(targetName);
            Files.copy(source.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            LOG.fine(() -> "Bukti disalin ke " + targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Gagal menyalin berkas bukti", e);
            throw new RuntimeException("Gagal menyalin berkas bukti: " + e.getMessage(), e);
        }
    }
}
