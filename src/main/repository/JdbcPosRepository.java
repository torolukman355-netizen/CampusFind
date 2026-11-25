package com.campusfind.repository;

import com.campusfind.model.PosPenyimpanan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implementasi {@link PosRepository} berbasis PostgreSQL.
 */
public class JdbcPosRepository implements PosRepository {

    private static final Logger LOG = Logger.getLogger(JdbcPosRepository.class.getName());

    private static final String COLUMNS = "id, nama, lokasi_penyerahan, label_pendek";

    private static final String UPSERT = """
            INSERT INTO pos (""" + COLUMNS + """
            ) VALUES (?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                nama = EXCLUDED.nama,
                lokasi_penyerahan = EXCLUDED.lokasi_penyerahan,
                label_pendek = EXCLUDED.label_pendek
            """;

    private final DatabaseManager databaseManager;

    public JdbcPosRepository(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager");
    }

    @Override
    public void save(PosPenyimpanan pos) {
        Objects.requireNonNull(pos, "pos");
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPSERT)) {
            ps.setString(1, pos.getId());
            ps.setString(2, pos.getNama());
            ps.setString(3, pos.getLokasiPenyerahan());
            ps.setString(4, pos.getLabelPendek());
            ps.executeUpdate();
            LOG.fine("Pos disimpan ke database: " + pos.getId());
        } catch (SQLException e) {
            throw JdbcSupport.failure("menyimpan pos " + pos.getId(), e);
        }
    }

    @Override
    public Optional<PosPenyimpanan> findById(String id) {
        String sql = "SELECT " + COLUMNS + " FROM pos WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("mencari pos " + id, e);
        }
    }

    @Override
    public List<PosPenyimpanan> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM pos ORDER BY id ASC";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PosPenyimpanan> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return List.copyOf(result);
        } catch (SQLException e) {
            throw JdbcSupport.failure("membaca daftar pos", e);
        }
    }

    @Override
    public boolean existsByNama(String nama) {
        if (nama == null) {
            return false;
        }
        String sql = "SELECT 1 FROM pos WHERE LOWER(nama) = LOWER(?) LIMIT 1";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nama);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("memeriksa nama pos", e);
        }
    }

    @Override
    public String nextId() {
        String floorSql = """
                SELECT COALESCE(MAX(CAST(SUBSTRING(id FROM '([0-9]+)$') AS INTEGER)), 0)
                FROM pos
                """;
        int floor = JdbcSequence.floorFromIds(databaseManager, floorSql, null);
        int next = JdbcSequence.next(databaseManager, "pos", floor);
        return String.format("POS-%04d", next);
    }

    private PosPenyimpanan map(ResultSet rs) throws SQLException {
        return new PosPenyimpanan(
                rs.getString("id"),
                rs.getString("nama"),
                rs.getString("lokasi_penyerahan"),
                rs.getString("label_pendek"));
    }
}
