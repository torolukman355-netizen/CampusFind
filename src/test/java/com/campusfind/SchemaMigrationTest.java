package com.campusfind;

import com.campusfind.model.DataSnapshot;
import com.campusfind.model.Item;
import com.campusfind.model.ItemFactory;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.Klaim;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.SesiLayanan;
import com.campusfind.model.StatusItem;
import com.campusfind.model.StatusKlaim;
import com.campusfind.repository.FileDataStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pengujian Migrasi Skema DataStore (schemaVersion 1 -> 2)")
class SchemaMigrationTest {

    @Test
    @DisplayName("File data versi 1 dengan status MENUNGGU_VERIFIKASI berhasil dimigrasi ke DIAJUKAN dan membuat .bak")
    void testMigrasiSchemaVersion1Ke2(@TempDir Path tempDir) throws IOException {
        // 1. Buat data snapshot versi lama (schemaVersion 1)
        DataSnapshot oldSnapshot = new DataSnapshot();
        oldSnapshot.setSchemaVersion(1);

        Item item = ItemFactory.create(
                "1001",
                JenisLaporan.TEMUAN,
                "Helm KYT",
                KategoriBarang.LAINNYA,
                "Helm hitam di kantin",
                "Kantin",
                LocalDate.of(2026, 11, 10),
                "POS-0001",
                null,
                "Ada stiker merah",
                StatusItem.DALAM_PROSES_KLAIM,
                LocalDateTime.now(),
                "Ahmad",
                "111222333",
                "Hitam",
                "Plastik"
        );
        oldSnapshot.getItems().put(item.getId(), item);

        Klaim klaimLama = new Klaim(
                "CLM-2026-0001",
                item.getId(),
                new Mahasiswa("Budi", "222333444"),
                "Helm saya ada lecet",
                "data/bukti/test.jpg",
                LocalDate.of(2026, 11, 23),
                SesiLayanan.PAGI,
                "POS-0001",
                true,
                true,
                80,
                StatusKlaim.MENUNGGU_VERIFIKASI, // Status versi lama
                LocalDateTime.now(),
                null
        );
        oldSnapshot.getKlaim().put(klaimLama.getId(), klaimLama);

        // Tulis file snapshot versi 1 secara manual
        Path dataFile = tempDir.resolve("campusfind.dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(dataFile))) {
            oos.writeObject(oldSnapshot);
            oos.flush();
        }

        assertTrue(Files.exists(dataFile), "File data versi 1 harus ada");

        // 2. Muat dengan FileDataStore baru (schemaVersion 2)
        FileDataStore store = new FileDataStore(tempDir);
        DataSnapshot migratedSnapshot = store.getSnapshot();

        // 3. Verifikasi hasil migrasi
        assertEquals(2, migratedSnapshot.getSchemaVersion(), "Skema harus termigrasi ke versi 2");
        Klaim migratedKlaim = migratedSnapshot.getKlaim().get("CLM-2026-0001");
        assertNotNull(migratedKlaim);
        assertEquals(StatusKlaim.DIAJUKAN, migratedKlaim.getStatus(), "Status harus termigrasi ke DIAJUKAN");

        // File cadangan backup harus terbuat
        Path backupFile = tempDir.resolve("campusfind.dat.bak");
        assertTrue(Files.exists(backupFile), "File backup .bak harus terbentuk");

        // Verifikasi active index
        assertEquals("CLM-2026-0001", migratedSnapshot.getKlaimAktifIndex().get(item.getId()));
    }
}
