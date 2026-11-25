package com.campusfind.repository;

import com.campusfind.model.Klaim;
import com.campusfind.model.SesiLayanan;
import com.campusfind.model.StatusKlaim;
import com.campusfind.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementasi {@link KlaimRepository} berbasis PostgreSQL.
 */
public class JdbcKlaimRepository implements KlaimRepository {

    private static final Logger LOG = Logger.getLogger(JdbcKlaimRepository.class.getName());

    private static final String STATUS_AKTIF = "('DIAJUKAN', 'DISETUJUI', 'MENUNGGU_VERIFIKASI')";

    private static final String COLUMNS = """
            id, item_id, pengklaim_nomor_identitas, pengklaim_nama, pengklaim_role,
            ciri_khusus, bukti_path, bukti_bytes, tanggal_serah_terima, sesi, pos_id,
            tempat_penitipan,
            pernyataan_identitas_asli, pernyataan_kepemilikan, skor_kecocokan,
            status, diajukan_pada, catatan_petugas
            """;

    private static final String UPSERT = """
            INSERT INTO klaim (""" + COLUMNS + """
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                item_id = EXCLUDED.item_id,
                pengklaim_nomor_identitas = EXCLUDED.pengklaim_nomor_identitas,
                pengklaim_nama = EXCLUDED.pengklaim_nama,
                pengklaim_role = EXCLUDED.pengklaim_role,
                ciri_khusus = EXCLUDED.ciri_khusus,
                bukti_path = EXCLUDED.bukti_path,
                bukti_bytes = EXCLUDED.bukti_bytes,
                tanggal_serah_terima = EXCLUDED.tanggal_serah_terima,
                sesi = EXCLUDED.sesi,
                pos_id = EXCLUDED.pos_id,
                tempat_penitipan = EXCLUDED.tempat_penitipan,
                pernyataan_identitas_asli = EXCLUDED.pernyataan_identitas_asli,
                pernyataan_kepemilikan = EXCLUDED.pernyataan_kepemilikan,
                skor_kecocokan = EXCLUDED.skor_kecocokan,
                status = EXCLUDED.status,
                diajukan_pada = EXCLUDED.diajukan_pada,
                catatan_petugas = EXCLUDED.catatan_petugas
            """;

    private final DatabaseManager databaseManager;

    public JdbcKlaimRepository(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager");
    }

    @Override
    public void save(Klaim klaim) {
        Objects.requireNonNull(klaim, "klaim");
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPSERT)) {
            int i = 1;
            ps.setString(i++, klaim.getId());
            ps.setString(i++, klaim.getItemId());

            User pengklaim = klaim.getPengklaim();
            ps.setString(i++, JdbcUserFactory.nomorOf(pengklaim));
            JdbcSupport.setStringOrNull(ps, i++, JdbcUserFactory.namaOf(pengklaim));
            JdbcSupport.setStringOrNull(ps, i++, JdbcUserFactory.roleOf(pengklaim));

            JdbcSupport.setStringOrNull(ps, i++, klaim.getCiriKhusus());
            JdbcSupport.setStringOrNull(ps, i++, klaim.getBuktiPath());
            JdbcSupport.setBytesOrNull(ps, i++, klaim.getBuktiBytes());
            JdbcSupport.setDate(ps, i++, klaim.getTanggalSerahTerima());
            ps.setString(i++, klaim.getSesi() == null ? null : klaim.getSesi().name());
            JdbcSupport.setStringOrNull(ps, i++, klaim.getPosId());
            JdbcSupport.setStringOrNull(ps, i++, klaim.getPosId());
            ps.setBoolean(i++, klaim.isPernyataanIdentitasAsli());
            ps.setBoolean(i++, klaim.isPernyataanKepemilikan());
            ps.setInt(i++, klaim.getSkorKecocokan());
            ps.setString(i++, klaim.getStatus().name());
            JdbcSupport.setTimestamp(ps, i++, klaim.getDiajukanPada());
            JdbcSupport.setStringOrNull(ps, i, klaim.getCatatanPetugas());

            ps.executeUpdate();
            LOG.fine("Klaim disimpan ke database: " + klaim.getId());
        } catch (SQLException e) {
            throw JdbcSupport.failure("menyimpan klaim " + klaim.getId(), e);
        }
    }

    @Override
    public Optional<Klaim> findById(String id) {
        String sql = "SELECT " + COLUMNS + " FROM klaim WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("mencari klaim " + id, e);
        }
    }

    @Override
    public List<Klaim> findAll() {
        return query("SELECT " + COLUMNS + " FROM klaim ORDER BY diajukan_pada ASC, id ASC");
    }

    @Override
    public List<Klaim> findByPengklaim(String nomorIdentitas) {
        return query("SELECT " + COLUMNS + """
                 FROM klaim
                WHERE pengklaim_nomor_identitas = ?
                ORDER BY diajukan_pada ASC, id ASC
                """, nomorIdentitas);
    }

    @Override
    public Optional<Klaim> findAktifByItemId(String itemId) {
        String sql = "SELECT " + COLUMNS + """
                 FROM klaim
                WHERE item_id = ? AND status IN """ + STATUS_AKTIF + """
                ORDER BY diajukan_pada DESC, id DESC
                LIMIT 1
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("mencari klaim aktif untuk item " + itemId, e);
        }
    }

    @Override
    public String nextId(int tahun) {
        String floorSql = """
                SELECT COALESCE(MAX(CAST(SUBSTRING(id FROM '([0-9]+)$') AS INTEGER)), 0)
                FROM klaim
                WHERE id LIKE ?
                """;
        int floor = JdbcSequence.floorFromIds(databaseManager, floorSql, "CLM-" + tahun + "-%");
        int next = JdbcSequence.next(databaseManager, "klaim:" + tahun, floor);
        return String.format("CLM-%d-%04d", tahun, next);
    }

    @Override
    public void updateStatus(String id, StatusKlaim status) {
        if (id == null || status == null) {
            return;
        }
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE klaim SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setString(2, id);
            ps.executeUpdate();
            LOG.fine("Status klaim " + id + " diubah ke " + status);
        } catch (SQLException e) {
            throw JdbcSupport.failure("mengubah status klaim " + id, e);
        }
    }

    @Override
    public void delete(String id) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement("DELETE FROM klaim WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            LOG.fine("Klaim dihapus: " + id);
        } catch (SQLException e) {
            throw JdbcSupport.failure("menghapus klaim " + id, e);
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private List<Klaim> query(String sql, String... parameters) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                ps.setString(i + 1, parameters[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Klaim> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return List.copyOf(result);
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("membaca daftar klaim", e);
        }
    }

    private Klaim map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String itemId = rs.getString("item_id");
        User pengklaim = JdbcUserFactory.fromRow(
                rs.getString("pengklaim_role"),
                rs.getString("pengklaim_nama"),
                rs.getString("pengklaim_nomor_identitas"));
        SesiLayanan sesi = JdbcSupport.enumOr(SesiLayanan.class,
                rs.getString("sesi"), SesiLayanan.PAGI);
        StatusKlaim status = JdbcSupport.enumOr(StatusKlaim.class,
                rs.getString("status"), StatusKlaim.DIAJUKAN);
        if (status == StatusKlaim.MENUNGGU_VERIFIKASI) {
            status = StatusKlaim.DIAJUKAN;
        }
        LocalDate serahTerima = JdbcSupport.readDate(rs, "tanggal_serah_terima");
        LocalDateTime diajukan = JdbcSupport.readTimestamp(rs, "diajukan_pada");
        byte[] buktiBytes = rs.getBytes("bukti_bytes");

        Klaim klaim = new Klaim(id, itemId, pengklaim,
                rs.getString("ciri_khusus"),
                rs.getString("bukti_path"),
                serahTerima,
                sesi,
                rs.getString("pos_id"),
                rs.getBoolean("pernyataan_identitas_asli"),
                rs.getBoolean("pernyataan_kepemilikan"),
                rs.getInt("skor_kecocokan"),
                status,
                diajukan,
                rs.getString("catatan_petugas"));
        klaim.setBuktiBytes(buktiBytes);
        return klaim;
    }
}
