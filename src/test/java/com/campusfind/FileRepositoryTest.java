package com.campusfind;

import com.campusfind.model.Item;
import com.campusfind.model.ItemFactory;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.StatusItem;
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.FileItemRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian FileRepository dan Persistensi FileDataStore")
class FileRepositoryTest {

    @Test
    @DisplayName("Data disimpan dan terbaca kembali saat reload (Round-Trip)")
    void testRoundTrip(@TempDir Path tempDir) {
        FileDataStore ds1 = new FileDataStore(tempDir);
        FileItemRepository repo1 = new FileItemRepository(ds1);

        String id = repo1.nextId();
        Item item = ItemFactory.create(
                id,
                JenisLaporan.TEMUAN,
                "Dompet Kulit",
                KategoriBarang.NON_ELEKTRONIK,
                "Dompet cokelat berisi uang",
                "Kantin Perpustakaan",
                LocalDate.of(2026, 11, 1),
                "POS-0001",
                null,
                "Ada struk ATM",
                StatusItem.TERSEDIA,
                LocalDateTime.now(),
                "Dewi",
                "123456789",
                "Cokelat",
                "Kulit"
        );
        repo1.save(item);

        // Buka ulang data store dari path direktori yang sama
        FileDataStore ds2 = new FileDataStore(tempDir);
        FileItemRepository repo2 = new FileItemRepository(ds2);

        Optional<Item> loaded = repo2.findById(id);
        assertTrue(loaded.isPresent());
        assertEquals("Dompet Kulit", loaded.get().getNama());
        assertEquals(StatusItem.TERSEDIA, loaded.get().getStatus());
        assertEquals("POS-0001", loaded.get().getPosId());
    }

    @Test
    @DisplayName("ID bertambah terus dan tidak pernah dipakai ulang setelah restart")
    void testIdTidakDipakaiUlangSetelahRestart(@TempDir Path tempDir) {
        FileDataStore ds1 = new FileDataStore(tempDir);
        FileItemRepository repo1 = new FileItemRepository(ds1);

        String id1 = repo1.nextId(); // ARC-0001
        String id2 = repo1.nextId(); // ARC-0002
        String id3 = repo1.nextId(); // ARC-0003

        assertEquals("ARC-0001", id1);
        assertEquals("ARC-0002", id2);
        assertEquals("ARC-0003", id3);

        // Simulasi aplikasi ditutup dan dibuka ulang
        FileDataStore ds2 = new FileDataStore(tempDir);
        FileItemRepository repo2 = new FileItemRepository(ds2);

        String id4 = repo2.nextId();
        assertEquals("ARC-0004", id4);
    }

    @Test
    @DisplayName("File korup dipulihkan ke backup dan data store mulai bersih tanpa crash")
    void testFileKorupDipulihkan(@TempDir Path tempDir) throws IOException {
        Path dataFile = tempDir.resolve("campusfind.dat");
        // Tulis byte sampah agar file tidak bisa di-deserialize
        Files.writeString(dataFile, "INI BUKAN FORMAT SERIALIZATION JAVA YANG VALID");

        // Inisialisasi store tidak boleh crash (melempar exception yang mematikan aplikasi)
        assertDoesNotThrow(() -> {
            FileDataStore ds = new FileDataStore(tempDir);
            assertNotNull(ds.getSnapshot());
            // Seed default pos harus tetap ada setelah reset
            assertFalse(ds.getSnapshot().getPos().isEmpty());
        });

        // Verifikasi bahwa file korup telah di-rename dengan pola .corrupt-
        try (var stream = Files.list(tempDir)) {
            boolean backupExists = stream.anyMatch(p -> p.getFileName().toString().contains(".corrupt-"));
            assertTrue(backupExists, "File backup corrupt harus dibuat.");
        }
    }
}
