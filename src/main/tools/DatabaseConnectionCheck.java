package com.campusfind.tools;

import com.campusfind.exception.StorageException;
import com.campusfind.model.Admin;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Petugas;
import com.campusfind.repository.DatabaseManager;
import com.campusfind.repository.DatabaseSchema;
import com.campusfind.repository.JdbcUserRepository;
import com.campusfind.repository.UserRepository;

/**
 * Program kecil untuk menguji koneksi ke database PostgreSQL tanpa
 * menjalankan aplikasi Swing.
 * <p>
 * Jalankan dengan:
 * <pre>
 * mvn -q exec:java -Dexec.mainClass=com.campusfind.tools.DatabaseConnectionCheck
 * </pre>
 */
public final class DatabaseConnectionCheck {

    private DatabaseConnectionCheck() {
    }

    public static void main(String[] args) {
        DatabaseManager manager = new DatabaseManager();
        System.out.println("Menguji koneksi ke PostgreSQL di " + manager.safeUrl() + " ...");

        try {
            manager.testConnection();
            DatabaseSchema.ensureSchema(manager);
            com.campusfind.repository.PosRepository posRepo = new com.campusfind.repository.JdbcPosRepository(manager);
            com.campusfind.repository.ItemRepository itemRepo = new com.campusfind.repository.JdbcItemRepository(manager);
            com.campusfind.repository.KlaimRepository klaimRepo = new com.campusfind.repository.JdbcKlaimRepository(manager);
            com.campusfind.repository.NotificationRepository notifRepo = new com.campusfind.repository.JdbcNotificationRepository(manager);
            UserRepository userRepo = new JdbcUserRepository(manager);

            com.campusfind.repository.DatabaseSeeder.seedIfEmpty(posRepo, itemRepo, klaimRepo, notifRepo, userRepo);
            System.out.println("Selesai. Skema dan data awal database siap dipakai.");
        } catch (StorageException e) {
            System.err.println();
            System.err.println("Koneksi database GAGAL.");
            System.err.println("  " + e.getMessage());
            printHint(e);
            System.exit(1);
        }
    }

    private static void printHint(StorageException e) {
        String message = String.valueOf(e.getMessage()).toLowerCase();

        if (message.contains("connection refused") || message.contains("connect timed out")) {
            System.err.println("  Kemungkinan server PostgreSQL belum berjalan.");
            System.err.println("  Jalankan: brew services start postgresql@16");
        } else if (message.contains("password authentication failed")) {
            System.err.println("  Periksa db.user dan db.password di database.properties.");
        } else if (message.contains("does not exist")) {
            System.err.println("  Database belum dibuat. Jalankan:");
            System.err.println("    createdb -O campusfind campusfind");
        } else if (message.contains("driver")) {
            System.err.println("  Driver PostgreSQL belum ada di classpath. Jalankan 'mvn compile'.");
        }

        System.err.println();
        System.err.println("Konfigurasi dibaca dari (urutan prioritas):");
        System.err.println("  1. Env CAMPUSFIND_DB_URL / CAMPUSFIND_DB_USER / CAMPUSFIND_DB_PASSWORD");
        System.err.println("  2. File database.properties di folder project");
        System.err.println("  3. Default: jdbc:postgresql://localhost:5432/campusfind (user & password: campusfind)");
    }
}
