package com.campusfind.repository;

import com.campusfind.exception.StorageException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Penghasil nomor urut persisten untuk ID buatan aplikasi (item, klaim, pos).
 * <p>
 * Nilai disimpan di tabel {@code app_sequence} sehingga ID tidak terpakai ulang
 * setelah data dihapus. Nilai awal diambil dari data yang sudah ada
 * ({@code floor}) agar aman ketika database diisi lewat migrasi.
 */
final class JdbcSequence {

    private JdbcSequence() {
    }

    static int next(DatabaseManager databaseManager, String key, int floor) {
        Objects.requireNonNull(databaseManager, "databaseManager");
        String ensure = """
                INSERT INTO app_sequence (nama, nilai) VALUES (?, ?)
                ON CONFLICT (nama) DO UPDATE
                    SET nilai = GREATEST(app_sequence.nilai, EXCLUDED.nilai)
                """;
        String select = "SELECT nilai FROM app_sequence WHERE nama = ? FOR UPDATE";
        String update = "UPDATE app_sequence SET nilai = ? WHERE nama = ?";

        try (Connection connection = databaseManager.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(ensure)) {
                    ps.setString(1, key);
                    ps.setInt(2, Math.max(floor, 0));
                    ps.executeUpdate();
                }

                int current;
                try (PreparedStatement ps = connection.prepareStatement(select)) {
                    ps.setString(1, key);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Baris sequence tidak ditemukan: " + key);
                        }
                        current = rs.getInt(1);
                    }
                }

                int next = current + 1;
                try (PreparedStatement ps = connection.prepareStatement(update)) {
                    ps.setInt(1, next);
                    ps.setString(2, key);
                    ps.executeUpdate();
                }

                connection.commit();
                return next;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new StorageException("Gagal membuat nomor urut " + key + ": " + e.getMessage(), e);
        }
    }

    static int floorFromIds(DatabaseManager databaseManager, String sql, String parameter) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (parameter != null) {
                ps.setString(1, parameter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new StorageException("Gagal membaca nomor urut terakhir: " + e.getMessage(), e);
        }
    }
}
