package com.campusfind.tools;

import com.campusfind.exception.StorageException;
import com.campusfind.model.DataSnapshot;
import com.campusfind.model.Item;
import com.campusfind.model.Klaim;
import com.campusfind.model.Notification;
import com.campusfind.model.PosPenyimpanan;
import com.campusfind.repository.DatabaseManager;
import com.campusfind.repository.DatabaseSchema;
import com.campusfind.repository.FileDataStore;
import com.campusfind.repository.JdbcItemRepository;
import com.campusfind.repository.JdbcKlaimRepository;
import com.campusfind.repository.JdbcNotificationRepository;
import com.campusfind.repository.JdbcPosRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Memindahkan seluruh data dari file {@code data/campusfind.dat} ke PostgreSQL.
 * <p>
 * Jalankan sekali setelah database dibuat:
 * <pre>
 * mvn -q compile exec:java -Dexec.mainClass=com.campusfind.tools.MigrateFileDataToDatabase
 * </pre>
 * Secara default proses dibatalkan bila database sudah berisi data. Gunakan
 * argumen {@code --overwrite} untuk menimpa data yang sudah ada.
 */
public final class MigrateFileDataToDatabase {

    private MigrateFileDataToDatabase() {
    }

    public static void main(String[] args) {
        boolean overwrite = List.of(args).contains("--overwrite");

        try {
            DatabaseManager databaseManager = new DatabaseManager();
            DatabaseSchema.ensureSchema(databaseManager);

            if (!overwrite && databaseSudahBerisi(databaseManager)) {
                System.out.println("Database sudah berisi data. Migrasi dibatalkan.");
                System.out.println("Gunakan argumen --overwrite bila ingin menimpa dengan isi file.");
                return;
            }

            FileDataStore fileStore = new FileDataStore();
            DataSnapshot snapshot = fileStore.getSnapshot();

            JdbcPosRepository posRepository = new JdbcPosRepository(databaseManager);
            JdbcItemRepository itemRepository = new JdbcItemRepository(databaseManager);
            JdbcKlaimRepository klaimRepository = new JdbcKlaimRepository(databaseManager);
            JdbcNotificationRepository notificationRepository =
                    new JdbcNotificationRepository(databaseManager);

            // Urutan penting: pos → item → klaim (relasi foreign key).
            for (PosPenyimpanan pos : snapshot.getPos().values()) {
                posRepository.save(pos);
            }
            for (Item item : snapshot.getItems().values()) {
                itemRepository.save(item);
            }
            for (Klaim klaim : snapshot.getKlaim().values()) {
                klaimRepository.save(klaim);
            }
            for (Notification notification : snapshot.getNotifikasi()) {
                notificationRepository.save(notification);
            }

            System.out.println("Migrasi file → PostgreSQL selesai.");
            System.out.println("  Pos          : " + snapshot.getPos().size());
            System.out.println("  Item         : " + snapshot.getItems().size());
            System.out.println("  Klaim        : " + snapshot.getKlaim().size());
            System.out.println("  Notifikasi   : " + snapshot.getNotifikasi().size());
            System.out.println("Sumber data: data/campusfind.dat");
            System.out.println("Tujuan     : " + databaseManager.safeUrl());
        } catch (StorageException e) {
            System.err.println("Migrasi GAGAL: " + e.getMessage());
            System.exit(1);
        }
    }

    private static boolean databaseSudahBerisi(DatabaseManager databaseManager) {
        String sql = """
                SELECT (SELECT COUNT(*) FROM pos)
                     + (SELECT COUNT(*) FROM items)
                     + (SELECT COUNT(*) FROM klaim)
                     + (SELECT COUNT(*) FROM notifications) AS total
                """;
        try (Connection connection = databaseManager.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() && rs.getInt("total") > 0;
        } catch (SQLException e) {
            throw new StorageException(
                    "Gagal memeriksa isi database: " + e.getMessage(), e);
        }
    }
}
