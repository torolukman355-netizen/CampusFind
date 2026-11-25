package com.campusfind.repository;

import com.campusfind.exception.StorageException;
import com.campusfind.model.Admin;
import com.campusfind.model.Mahasiswa;
import com.campusfind.model.Petugas;
import com.campusfind.model.Role;
import com.campusfind.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementasi JDBC untuk {@link UserRepository} berbasis PostgreSQL.
 */
public class JdbcUserRepository implements UserRepository {

    private static final String COLUMNS =
            "nomor_identitas, nama, email, password_hash, role, status";

    private final DatabaseManager databaseManager;

    public JdbcUserRepository(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager, "databaseManager tidak boleh null");
    }

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "user tidak boleh null");

        String email = user.getEmail();
        if (email != null && !email.isBlank()) {
            Optional<User> existing = findByEmail(email);
            if (existing.isPresent() && !existing.get().getNomorIdentitas().equals(user.getNomorIdentitas())) {
                throw new StorageException("Email sudah terdaftar: " + email);
            }
        }

        String sql = """
                INSERT INTO users (nomor_identitas, nama, email, password_hash, role, status, dibuat_pada)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (nomor_identitas) DO UPDATE SET
                    nama = EXCLUDED.nama,
                    email = EXCLUDED.email,
                    password_hash = COALESCE(EXCLUDED.password_hash, users.password_hash),
                    role = EXCLUDED.role,
                    status = EXCLUDED.status
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getNomorIdentitas());
            ps.setString(2, user.getNama());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole().name());
            ps.setString(6, user.isAktif() ? "AKTIF" : "NONAKTIF");
            ps.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw JdbcSupport.failure("Gagal menyimpan data pengguna " + user.getNomorIdentitas(), e);
        }
    }

    @Override
    public Optional<User> findById(String nomorIdentitas) {
        if (nomorIdentitas == null || nomorIdentitas.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT " + COLUMNS + " FROM users WHERE nomor_identitas = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomorIdentitas.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("Gagal mencari pengguna ID " + nomorIdentitas, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT " + COLUMNS + " FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("Gagal mencari pengguna dengan email " + email, e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM users ORDER BY nama ASC";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return List.copyOf(list);
        } catch (SQLException e) {
            throw JdbcSupport.failure("Gagal mengambil daftar pengguna", e);
        }
    }

    @Override
    public boolean deleteById(String nomorIdentitas) {
        if (nomorIdentitas == null || nomorIdentitas.isBlank()) {
            return false;
        }
        String sql = "DELETE FROM users WHERE nomor_identitas = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomorIdentitas.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw JdbcSupport.failure("Gagal menghapus pengguna ID " + nomorIdentitas, e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?) LIMIT 1";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("Gagal memeriksa keberadaan email " + email, e);
        }
    }

    @Override
    public boolean existsByNomorIdentitas(String nomorIdentitas) {
        if (nomorIdentitas == null || nomorIdentitas.isBlank()) {
            return false;
        }
        String sql = "SELECT 1 FROM users WHERE nomor_identitas = ? LIMIT 1";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nomorIdentitas.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw JdbcSupport.failure("Gagal memeriksa keberadaan pengguna ID " + nomorIdentitas, e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        String nomorIdentitas = rs.getString("nomor_identitas");
        String nama = rs.getString("nama");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String roleStr = rs.getString("role");
        String statusStr = rs.getString("status");

        boolean aktif = statusStr == null || !"NONAKTIF".equalsIgnoreCase(statusStr);
        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (Exception e) {
            role = Role.MAHASISWA;
        }

        return switch (role) {
            case MAHASISWA -> new Mahasiswa(nama, nomorIdentitas, email, passwordHash, aktif);
            case PETUGAS -> new Petugas(nama, nomorIdentitas, email, passwordHash, aktif);
            case ADMIN -> new Admin(nama, nomorIdentitas, email, passwordHash, aktif);
        };
    }
}
