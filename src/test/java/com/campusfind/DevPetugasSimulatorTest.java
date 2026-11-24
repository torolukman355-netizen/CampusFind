package com.campusfind;

import com.campusfind.integration.DevPetugasSimulator;
import com.campusfind.model.Item;
import com.campusfind.model.ItemFactory;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.SesiLayanan;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.FileItemRepository;
import com.campusfind.repository.FileKlaimRepository;
import com.campusfind.repository.FileNotificationRepository;
import com.campusfind.repository.FilePosRepository;
import com.campusfind.repository.ItemRepository;
import com.campusfind.repository.KlaimRepository;
import com.campusfind.repository.NotificationRepository;
import com.campusfind.repository.PosRepository;
import com.campusfind.service.KlaimService;
import com.campusfind.service.TokenOverlapStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian DevPetugasSimulator (Simulasi Aksi Petugas)")
class DevPetugasSimulatorTest {

    private Path tempDirPath;
    private FileDataStore dataStore;
    private ItemRepository itemRepository;
    private KlaimRepository klaimRepository;
    private PosRepository posRepository;
    private NotificationRepository notificationRepository;
    private KlaimService klaimService;
    private DevPetugasSimulator simulator;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDirPath = tempDir;
        Clock clock = Clock.fixed(Instant.parse("2026-11-20T10:00:00Z"), ZoneId.of("Asia/Jakarta"));
        dataStore = new FileDataStore(tempDir);
        itemRepository = new FileItemRepository(dataStore);
        klaimRepository = new FileKlaimRepository(dataStore);
        posRepository = new FilePosRepository(dataStore);
        posRepository.save(new PosPenyimpanan("POS-0001", "Pos Satpam", "Gedung A", "08123"));
        notificationRepository = new FileNotificationRepository(dataStore);

        klaimService = new KlaimService(
                klaimRepository,
                itemRepository,
                posRepository,
                notificationRepository,
                new TokenOverlapStrategy(),
                clock
        );

        simulator = new DevPetugasSimulator(klaimService);
    }

    private File createBuktiFile() throws IOException {
        File f = tempDirPath.resolve("bukti.jpg").toFile();
        Files.write(f.toPath(), new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});
        return f;
    }

    @Test
    @DisplayName("Simulasi persetujuan klaim berhasil mengubah status menjadi DISETUJUI")
    void testSimulasikanSetujui() throws IOException {
        Item item = ItemFactory.create(
                "ARC-0001", JenisLaporan.TEMUAN, "Kunci Motor", KategoriBarang.KUNCI_KENDARAAN,
                "Gantungan kunci", "Parkir", LocalDate.of(2026, 11, 20),
                "POS-0001", null, "Kunci Honda", StatusItem.TERSEDIA,
                LocalDateTime.now(), "Penemu", "111222333", "Hitam", "Besi"
        );
        itemRepository.save(item);

        Klaim klaim = klaimService.ajukan(
                "ARC-0001", "444555666", "Pengklaim", "Kunci Honda",
                createBuktiFile(), LocalDate.of(2026, 11, 25), SesiLayanan.PAGI,
                true, true
        );

        simulator.simulasikanSetujui(klaim.getId());

        Klaim updated = klaimRepository.findById(klaim.getId()).orElseThrow();
        assertEquals(StatusKlaim.DISETUJUI, updated.getStatus());
        Item itemUpdated = itemRepository.findById("ARC-0001").orElseThrow();
        assertEquals(StatusItem.DALAM_PROSES_KLAIM, itemUpdated.getStatus());
    }

    @Test
    @DisplayName("Simulasi penolakan klaim berhasil mengubah status menjadi DITOLAK dengan alasan")
    void testSimulasikanTolak() throws IOException {
        Item item = ItemFactory.create(
                "ARC-0001", JenisLaporan.TEMUAN, "Kunci Motor", KategoriBarang.KUNCI_KENDARAAN,
                "Gantungan kunci", "Parkir", LocalDate.of(2026, 11, 20),
                "POS-0001", null, "Kunci Honda", StatusItem.TERSEDIA,
                LocalDateTime.now(), "Penemu", "111222333", "Hitam", "Besi"
        );
        itemRepository.save(item);

        Klaim klaim = klaimService.ajukan(
                "ARC-0001", "444555666", "Pengklaim", "Kunci Honda",
                createBuktiFile(), LocalDate.of(2026, 11, 25), SesiLayanan.PAGI,
                true, true
        );

        simulator.simulasikanTolak(klaim.getId(), "Ciri kunci tidak cocok dengan fisik barang");

        Klaim updated = klaimRepository.findById(klaim.getId()).orElseThrow();
        assertEquals(StatusKlaim.DITOLAK, updated.getStatus());
        assertEquals("Ciri kunci tidak cocok dengan fisik barang", updated.getAlasanPenolakan());
        Item itemUpdated = itemRepository.findById("ARC-0001").orElseThrow();
        assertEquals(StatusItem.TERSEDIA, itemUpdated.getStatus());
    }
}
