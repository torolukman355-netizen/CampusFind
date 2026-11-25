package com.campusfind.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementasi PostgreSQL untuk penyimpanan log status dan data serah terima petugas.
 */
public class JdbcPetugasAuditRepository implements PetugasAuditRepository {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DatabaseManager databaseManager;

    public JdbcPetugasAuditRepository(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager");
    }

    @Override
    public void simpanLog(String itemId, String aksi, String pelaku) {
        String sql = "INSERT INTO status_log (id, item_id, aksi, pelaku, waktu) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "LOG-" + UUID.randomUUID().toString().substring(0, 8));
            ps.setString(2, itemId);
            ps.setString(3, aksi);
            ps.setString(4, pelaku);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcSupport.failure("menyimpan status_log item " + itemId, e);
        }
    }

    @Override
    public List<String> ambilLog(String itemId) {
        String sql = "SELECT aksi, pelaku, waktu FROM status_log WHERE item_id = ? ORDER BY waktu ASC";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> hasil = new ArrayList<>();
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("waktu");
                    String waktu = ts != null ? ts.toLocalDateTime().format(DTF) : "-";
                    hasil.add("[" + waktu + "] " + rs.getString("aksi") + " (oleh " + rs.getString("pelaku") + ")");
                }
                return hasil;
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("membaca status_log item " + itemId, e);
        }
    }

    @Override
    public void simpanSerahTerima(String itemId, String namaPenerima, LocalDate tanggal, String nipPetugas) {
        String sql = """
                INSERT INTO serah_terima (id, item_id, nama_penerima, tanggal, nip_petugas)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (item_id) DO UPDATE SET
                    nama_penerima = EXCLUDED.nama_penerima,
                    tanggal = EXCLUDED.tanggal,
                    nip_petugas = EXCLUDED.nip_petugas
                """;
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "ST-" + UUID.randomUUID().toString().substring(0, 8));
            ps.setString(2, itemId);
            ps.setString(3, namaPenerima);
            ps.setDate(4, Date.valueOf(tanggal != null ? tanggal : LocalDate.now()));
            ps.setString(5, nipPetugas != null ? nipPetugas : "-");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcSupport.failure("menyimpan serah_terima item " + itemId, e);
        }
    }
}