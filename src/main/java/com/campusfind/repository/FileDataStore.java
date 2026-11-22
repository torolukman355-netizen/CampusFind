package com.campusfind.repository;

import com.campusfind.model.DataSnapshot;
import com.campusfind.model.Klaim;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.model.StatusKlaim;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central file-based data store that manages a {@link DataSnapshot} in memory
 * and persists it to {@code data/campusfind.dat} using Java serialization.
 * <p>
 * All access is guarded by a {@link ReentrantLock} for thread safety.
 * Writes go to a {@code .tmp} file first, then are atomically moved into place.
 */
public class FileDataStore {

    private static final Logger LOG = Logger.getLogger(FileDataStore.class.getName());
    private static final String DATA_FILE_NAME = "campusfind.dat";
    private static final int CURRENT_SCHEMA_VERSION = 2;

    private final Path dataDir;
    private final Path dataFile;
    private final ReentrantLock lock = new ReentrantLock();
    private DataSnapshot snapshot;

    /**
     * Creates a FileDataStore using the default data directory
     * ({@code System.getProperty("user.dir") + "/data/"}).
     */
    public FileDataStore() {
        this(Path.of(System.getProperty("user.dir"), "data"));
    }

    /**
     * Creates a FileDataStore with a custom data directory (for testability).
     *
     * @param dataDir the root data directory
     */
    public FileDataStore(Path dataDir) {
        this.dataDir = dataDir;
        this.dataFile = dataDir.resolve(DATA_FILE_NAME);
        initDirectories();
        load();
    }

    /**
     * Returns the current in-memory snapshot.
     * Callers must hold no expectation of thread safety on the returned object
     * unless they coordinate through {@link #saveSnapshot()}.
     */
    public DataSnapshot getSnapshot() {
        lock.lock();
        try {
            return snapshot;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes an operation atomically under the internal reentrant lock.
     *
     * @param action supplier to run
     * @param <T>    return type
     * @return result of the supplier
     */
    public <T> T runAtomically(Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Executes a runnable atomically under the internal reentrant lock.
     *
     * @param action runnable to run
     */
    public void runAtomically(Runnable action) {
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Persists the current in-memory snapshot to disk.
     * Writes to a temporary file first, then atomically moves it into place.
     */
    public void saveSnapshot() {
        lock.lock();
        try {
            Path tmpFile = dataDir.resolve(DATA_FILE_NAME + ".tmp");
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    Files.newOutputStream(tmpFile))) {
                oos.writeObject(snapshot);
                oos.flush();
            } catch (IOException e) {
                throw new RuntimeException(
                        "Gagal menyimpan data ke file sementara: " + e.getMessage(), e);
            }

            try {
                Files.move(tmpFile, dataFile,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException atomicEx) {
                try {
                    Files.move(tmpFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(
                            "Gagal memindahkan file data: " + e.getMessage(), e);
                }
            }
            LOG.fine("Data berhasil disimpan ke " + dataFile);
        } finally {
            lock.unlock();
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private void initDirectories() {
        try {
            Files.createDirectories(dataDir);
            Files.createDirectories(dataDir.resolve("images"));
            Files.createDirectories(dataDir.resolve("bukti"));
        } catch (IOException e) {
            throw new RuntimeException(
                    "Gagal membuat direktori data: " + e.getMessage(), e);
        }
    }

    private void load() {
        lock.lock();
        try {
            if (Files.exists(dataFile)) {
                DataSnapshot loaded;
                try (ObjectInputStream ois = new ObjectInputStream(
                        Files.newInputStream(dataFile))) {
                    loaded = (DataSnapshot) ois.readObject();
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                    LOG.log(Level.WARNING, "File data korup: " + e.getMessage(), e);
                    moveCorruptAndStartFresh("File data korup: " + e.getMessage());
                    return;
                }

                if (loaded.getSchemaVersion() == 1) {
                    LOG.info("Mendeteksi file data versi 1. Melakukan backup dan migrasi ke versi 2...");
                    Path backupFile = dataDir.resolve(DATA_FILE_NAME + ".bak");
                    try {
                        Files.copy(dataFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
                        LOG.info("Backup file data lama disimpan di: " + backupFile);
                    } catch (IOException ioEx) {
                        LOG.log(Level.WARNING, "Gagal membuat backup sebelum migrasi: " + ioEx.getMessage(), ioEx);
                    }

                    // Migrasi klaim: petakan MENUNGGU_VERIFIKASI -> DIAJUKAN
                    if (loaded.getKlaim() != null) {
                        for (Klaim k : loaded.getKlaim().values()) {
                            if (k.getStatus() == StatusKlaim.MENUNGGU_VERIFIKASI) {
                                k.setStatus(StatusKlaim.DIAJUKAN);
                            }
                        }
                    }

                    loaded.setSchemaVersion(CURRENT_SCHEMA_VERSION);
                    this.snapshot = loaded;
                    this.snapshot.buildIndex();
                    saveSnapshot();
                    LOG.info("Migrasi data ke versi 2 berhasil.");
                    return;
                }

                if (loaded.getSchemaVersion() != CURRENT_SCHEMA_VERSION) {
                    LOG.warning("Schema version tidak dikenal: " + loaded.getSchemaVersion());
                    moveCorruptAndStartFresh("Schema version tidak dikenal (" +
                            loaded.getSchemaVersion() + ").");
                    return;
                }

                this.snapshot = loaded;
                this.snapshot.buildIndex();
                LOG.info("Data berhasil dimuat dari " + dataFile);
            } else {
                LOG.info("File data tidak ditemukan, memulai dengan data baru.");
                startFresh();
            }
        } finally {
            lock.unlock();
        }
    }

    private void moveCorruptAndStartFresh(String reason) {
        try {
            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path corruptFile = dataDir.resolve(DATA_FILE_NAME + ".corrupt-" + timestamp);
            Files.move(dataFile, corruptFile, StandardCopyOption.REPLACE_EXISTING);
            LOG.warning("File korup dipindahkan ke: " + corruptFile);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Gagal memindahkan file korup: " + e.getMessage(), e);
        }

        startFresh();
        showCorruptWarning(reason);
    }

    private void startFresh() {
        this.snapshot = new DataSnapshot();
        this.snapshot.setSchemaVersion(CURRENT_SCHEMA_VERSION);
        this.snapshot.buildIndex();
        seedDefaultPos();
    }

    private void seedDefaultPos() {
        if (snapshot.getPos().isEmpty()) {
            PosPenyimpanan defaultPos = new PosPenyimpanan(
                    "POS-0001",
                    "Pos Satpam Rektorat",
                    "Pos Kustodi Sentral - Rektorat Sayap Barat Lt. 1",
                    "POS REKTORAT"
            );
            snapshot.getPos().put(defaultPos.getId(), defaultPos);
            LOG.info("Default PosPenyimpanan berhasil ditambahkan.");
        }
    }

    private void showCorruptWarning(String reason) {
        if (java.awt.GraphicsEnvironment.isHeadless()
                || System.getProperty("surefire.test.class.path") != null
                || Boolean.getBoolean("campusfind.suppress.dialogs")) {
            LOG.warning("Peringatan Data: " + reason);
            return;
        }
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Data lama tidak dapat dibaca dan telah di-backup.\n" +
                                "Alasan: " + reason + "\n\n" +
                                "Aplikasi akan memulai dengan data baru.",
                        "Peringatan Data",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                SwingUtilities.invokeAndWait(() ->
                        JOptionPane.showMessageDialog(
                                null,
                                "Data lama tidak dapat dibaca dan telah di-backup.\n" +
                                        "Alasan: " + reason + "\n\n" +
                                        "Aplikasi akan memulai dengan data baru.",
                                "Peringatan Data",
                                JOptionPane.WARNING_MESSAGE
                        )
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warning("Dialog peringatan terganggu.");
        } catch (InvocationTargetException e) {
            LOG.log(Level.WARNING, "Gagal menampilkan dialog peringatan: " +
                    e.getMessage(), e);
        }
    }
}
