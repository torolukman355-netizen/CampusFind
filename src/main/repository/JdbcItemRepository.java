package com.campusfind.repository;

import com.campusfind.model.BarangElektronik;
import com.campusfind.model.BarangNonElektronik;
import com.campusfind.model.Item;
import com.campusfind.model.JenisLaporan;
import com.campusfind.model.KategoriBarang;
import com.campusfind.model.StatusItem;
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
 * Implementasi {@link ItemRepository} berbasis PostgreSQL.
 */
public class JdbcItemRepository implements ItemRepository {

    private static final Logger LOG = Logger.getLogger(JdbcItemRepository.class.getName());

    private static final String COLUMNS = """
            id, jenis, nama, kategori, deskripsi, lokasi_kejadian, tanggal_kejadian,
            pos_id, tempat_penitipan, foto_path, foto_bytes, ciri_rahasia, status, dibuat_pada,
            pelapor_nomor_identitas, pelapor_nama, pelapor_role,
            merek, nomor_seri, warna, bahan
            """;

    private static final String UPSERT = """
            INSERT INTO items (""" + COLUMNS + """
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                jenis = EXCLUDED.jenis,
                nama = EXCLUDED.nama,
                kategori = EXCLUDED.kategori,
                deskripsi = EXCLUDED.deskripsi,
                lokasi_kejadian = EXCLUDED.lokasi_kejadian,
                tanggal_kejadian = EXCLUDED.tanggal_kejadian,
                pos_id = EXCLUDED.pos_id,
                tempat_penitipan = EXCLUDED.tempat_penitipan,
                foto_path = EXCLUDED.foto_path,
                foto_bytes = EXCLUDED.foto_bytes,
                ciri_rahasia = EXCLUDED.ciri_rahasia,
                status = EXCLUDED.status,
                dibuat_pada = EXCLUDED.dibuat_pada,
                pelapor_nomor_identitas = EXCLUDED.pelapor_nomor_identitas,
                pelapor_nama = EXCLUDED.pelapor_nama,
                pelapor_role = EXCLUDED.pelapor_role,
                merek = EXCLUDED.merek,
                nomor_seri = EXCLUDED.nomor_seri,
                warna = EXCLUDED.warna,
                bahan = EXCLUDED.bahan
            """;

    private final DatabaseManager databaseManager;

    public JdbcItemRepository(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager");
    }

    @Override
    public void save(Item item) {
        Objects.requireNonNull(item, "item");
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPSERT)) {
            int i = 1;
            ps.setString(i++, item.getId());
            ps.setString(i++, item.getJenis().name());
            ps.setString(i++, item.getNama());
            ps.setString(i++, item.getKategori().name());
            JdbcSupport.setStringOrNull(ps, i++, item.getDeskripsi());
            JdbcSupport.setStringOrNull(ps, i++, item.getLokasiKejadian());
            JdbcSupport.setDate(ps, i++, item.getTanggalKejadian());
            JdbcSupport.setStringOrNull(ps, i++, item.getPosId());
            JdbcSupport.setStringOrNull(ps, i++, item.getTempatPenitipan());
            JdbcSupport.setStringOrNull(ps, i++, item.getFotoPath());
            JdbcSupport.setBytesOrNull(ps, i++, item.getFotoBytes());
            JdbcSupport.setStringOrNull(ps, i++, item.getCiriRahasia());
            ps.setString(i++, item.getStatus().name());
            JdbcSupport.setTimestamp(ps, i++, item.getDibuatPada());

            User pelapor = item.getPelapor();
            JdbcSupport.setStringOrNull(ps, i++, JdbcUserFactory.nomorOf(pelapor));
            JdbcSupport.setStringOrNull(ps, i++, JdbcUserFactory.namaOf(pelapor));
            JdbcSupport.setStringOrNull(ps, i++, JdbcUserFactory.roleOf(pelapor));

            if (item instanceof BarangElektronik elektronik) {
                JdbcSupport.setStringOrNull(ps, i++, elektronik.getMerek());
                JdbcSupport.setStringOrNull(ps, i++, elektronik.getNomorSeri());
                JdbcSupport.setStringOrNull(ps, i++, null);
                JdbcSupport.setStringOrNull(ps, i++, null);
            } else if (item instanceof BarangNonElektronik nonElektronik) {
                JdbcSupport.setStringOrNull(ps, i++, null);
                JdbcSupport.setStringOrNull(ps, i++, null);
                JdbcSupport.setStringOrNull(ps, i++, nonElektronik.getWarna());
                JdbcSupport.setStringOrNull(ps, i, nonElektronik.getBahan());
            } else {
                JdbcSupport.setStringOrNull(ps, i++, null);
                JdbcSupport.setStringOrNull(ps, i++, null);
                JdbcSupport.setStringOrNull(ps, i++, null);
                JdbcSupport.setStringOrNull(ps, i, null);
            }

            ps.executeUpdate();
            LOG.fine("Item disimpan ke database: " + item.getId());
        } catch (SQLException e) {
            throw JdbcSupport.failure("menyimpan item " + item.getId(), e);
        }
    }

    @Override
    public Optional<Item> findById(String id) {
        String sql = "SELECT " + COLUMNS + " FROM items WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("mencari item " + id, e);
        }
    }

    @Override
    public List<Item> findAll() {
        return query("SELECT " + COLUMNS + " FROM items ORDER BY dibuat_pada DESC, id DESC");
    }

    @Override
    public List<Item> findByJenis(JenisLaporan jenis) {
        if (jenis == null) {
            return List.of();
        }
        return query("SELECT " + COLUMNS + " FROM items WHERE jenis = ? ORDER BY dibuat_pada DESC, id DESC",
                jenis.name());
    }

    @Override
    public List<Item> findByStatus(StatusItem status) {
        if (status == null) {
            return List.of();
        }
        return query("SELECT " + COLUMNS + " FROM items WHERE status = ? ORDER BY dibuat_pada DESC, id DESC",
                status.name());
    }

    @Override
    public List<Item> search(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }
        String pattern = "%" + query.trim().toLowerCase() + "%";
        return query("SELECT " + COLUMNS + """
                         FROM items
                         WHERE LOWER(nama) LIKE ?
                            OR LOWER(id) LIKE ?
                            OR LOWER(COALESCE(lokasi_kejadian, '')) LIKE ?
                         ORDER BY dibuat_pada DESC, id DESC
                         """,
                pattern, pattern, pattern);
    }

    @Override
    public String nextId() {
        String floorSql = """
                SELECT COALESCE(MAX(CAST(SUBSTRING(id FROM '([0-9]+)$') AS INTEGER)), 0)
                FROM items
                """;
        int floor = JdbcSequence.floorFromIds(databaseManager, floorSql, null);
        int next = JdbcSequence.next(databaseManager, "item", floor);
        return String.format("ARC-%04d", next);
    }

    @Override
    public void updateStatus(String id, StatusItem status) {
        if (id == null || status == null) {
            return;
        }
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "UPDATE items SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw JdbcSupport.failure("mengubah status item " + id, e);
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private List<Item> query(String sql, String... parameters) {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                ps.setString(i + 1, parameters[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Item> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return List.copyOf(result);
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("membaca daftar item", e);
        }
    }

    private Item map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        JenisLaporan jenis = JdbcSupport.enumOr(JenisLaporan.class,
                rs.getString("jenis"), JenisLaporan.TEMUAN);
        KategoriBarang kategori = JdbcSupport.enumOr(KategoriBarang.class,
                rs.getString("kategori"), KategoriBarang.NON_ELEKTRONIK);
        StatusItem status = JdbcSupport.enumOr(StatusItem.class,
                rs.getString("status"), StatusItem.DILAPORKAN);

        String nama = rs.getString("nama");
        String deskripsi = rs.getString("deskripsi");
        String lokasi = rs.getString("lokasi_kejadian");
        LocalDate tanggal = JdbcSupport.readDate(rs, "tanggal_kejadian");
        String posId = rs.getString("pos_id");
        String tempatPenitipan = rs.getString("tempat_penitipan");
        String fotoPath = rs.getString("foto_path");
        byte[] fotoBytes = rs.getBytes("foto_bytes");
        String ciriRahasia = rs.getString("ciri_rahasia");
        LocalDateTime dibuatPada = JdbcSupport.readTimestamp(rs, "dibuat_pada");
        User pelapor = JdbcUserFactory.fromRow(
                rs.getString("pelapor_role"),
                rs.getString("pelapor_nama"),
                rs.getString("pelapor_nomor_identitas"));

        Item item;
        if (kategori == KategoriBarang.ELEKTRONIK) {
            item = new BarangElektronik(id, jenis, nama, kategori, deskripsi, lokasi, tanggal,
                    posId, fotoPath, ciriRahasia, status, dibuatPada, pelapor,
                    rs.getString("merek"), rs.getString("nomor_seri"));
        } else {
            item = new BarangNonElektronik(id, jenis, nama, kategori, deskripsi, lokasi, tanggal,
                    posId, fotoPath, ciriRahasia, status, dibuatPada, pelapor,
                    rs.getString("warna"), rs.getString("bahan"));
        }
        item.setFotoBytes(fotoBytes);
        if (tempatPenitipan != null && !tempatPenitipan.isBlank()) {
            item.setTempatPenitipan(tempatPenitipan);
        }
        return item;
    }
}
