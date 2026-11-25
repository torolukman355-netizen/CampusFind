package com.campusfind.repository;

import com.campusfind.model.Notification;
import com.campusfind.model.NotificationType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementasi {@link NotificationRepository} berbasis PostgreSQL.
 */
public class JdbcNotificationRepository implements NotificationRepository {

    private static final Logger LOG = Logger.getLogger(JdbcNotificationRepository.class.getName());

    private static final String COLUMNS = """
            id, penerima_nomor_identitas, tipe, judul, isi, referensi_id, dibuat_pada, dibaca
            """;

    private static final String UPSERT = """
            INSERT INTO notifications (""" + COLUMNS + """
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                penerima_nomor_identitas = EXCLUDED.penerima_nomor_identitas,
                tipe = EXCLUDED.tipe,
                judul = EXCLUDED.judul,
                isi = EXCLUDED.isi,
                referensi_id = EXCLUDED.referensi_id,
                dibuat_pada = EXCLUDED.dibuat_pada,
                dibaca = EXCLUDED.dibaca
            """;

    private final DatabaseManager databaseManager;

    public JdbcNotificationRepository(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager");
    }

    @Override
    public void save(Notification notification) {
        Objects.requireNonNull(notification, "notification");
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPSERT)) {
            int i = 1;
            ps.setString(i++, notification.getId());
            ps.setString(i++, notification.getPenerimaNomorIdentitas());
            ps.setString(i++, notification.getTipe().name());
            ps.setString(i++, notification.getJudul());
            JdbcSupport.setStringOrNull(ps, i++, notification.getIsi());
            JdbcSupport.setStringOrNull(ps, i++, notification.getReferensiId());
            JdbcSupport.setTimestamp(ps, i++, notification.getDibuatPada());
            ps.setBoolean(i, notification.isDibaca());
            ps.executeUpdate();
            LOG.fine("Notifikasi disimpan ke database: " + notification.getId());
        } catch (SQLException e) {
            throw JdbcSupport.failure("menyimpan notifikasi " + notification.getId(), e);
        }
    }

    @Override
    public Optional<Notification> findById(String id) {
        String sql = "SELECT " + COLUMNS + " FROM notifications WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("mencari notifikasi " + id, e);
        }
    }

    @Override
    public List<Notification> findByPenerima(String nomorIdentitas) {
        String sql = "SELECT " + COLUMNS + """
                FROM notifications
                WHERE penerima_nomor_identitas = ?
                ORDER BY dibuat_pada ASC, id ASC
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomorIdentitas);
            try (ResultSet rs = ps.executeQuery()) {
                List<Notification> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return List.copyOf(result);
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("membaca notifikasi penerima " + nomorIdentitas, e);
        }
    }

    @Override
    public int countBelumDibaca(String nomorIdentitas) {
        String sql = """
                SELECT COUNT(*) FROM notifications
                WHERE penerima_nomor_identitas = ? AND dibaca = FALSE
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomorIdentitas);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("menghitung notifikasi belum dibaca", e);
        }
    }

    @Override
    public void markAsRead(String id, String nomorIdentitas) {
        String sql = "SELECT penerima_nomor_identitas FROM notifications WHERE id = ?";
        try (Connection connection = databaseManager.getConnection()) {
            String penerima;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return;
                    }
                    penerima = rs.getString(1);
                }
            }
            if (!Objects.equals(penerima, nomorIdentitas)) {
                throw new SecurityException(
                        "Anda tidak memiliki akses untuk menandai notifikasi pengguna lain.");
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE notifications SET dibaca = TRUE WHERE id = ?")) {
                ps.setString(1, id);
                ps.executeUpdate();
            }
            LOG.fine("Notifikasi ditandai dibaca: " + id);
        } catch (SQLException e) {
            throw JdbcSupport.failure("menandai notifikasi " + id + " dibaca", e);
        }
    }

    @Override
    public void markAllAsRead(String nomorIdentitas) {
        String sql = """
                UPDATE notifications SET dibaca = TRUE
                WHERE penerima_nomor_identitas = ? AND dibaca = FALSE
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomorIdentitas);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcSupport.failure("menandai semua notifikasi dibaca", e);
        }
    }

    @Override
    @Deprecated
    public void markAsRead(String id) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE notifications SET dibaca = TRUE WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcSupport.failure("menandai notifikasi " + id + " dibaca", e);
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        NotificationType tipe = JdbcSupport.enumOr(NotificationType.class,
                rs.getString("tipe"), NotificationType.KLAIM_DITERIMA);
        LocalDateTime dibuatPada = JdbcSupport.readTimestamp(rs, "dibuat_pada");
        return new Notification(
                rs.getString("id"),
                rs.getString("penerima_nomor_identitas"),
                tipe,
                rs.getString("judul"),
                rs.getString("isi"),
                rs.getString("referensi_id"),
                dibuatPada,
                rs.getBoolean("dibaca"));
    }
}
