package com.campusfind.repository;

import com.campusfind.exception.StorageException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Helper kecil untuk pemetaan tipe Java ↔ tipe SQL yang dipakai repository JDBC.
 */
final class JdbcSupport {

    private JdbcSupport() {
    }

    static StorageException failure(String action, SQLException e) {
        return new StorageException("Gagal " + action + ": " + e.getMessage(), e);
    }

    /**
     * Membaca enum dengan aman; nilai yang tidak dikenal memakai fallback
     * agar data lama tidak membuat aplikasi berhenti.
     */
    static <E extends Enum<E>> E enumOr(Class<E> type, String value, E fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    static LocalDate readDate(ResultSet rs, String column) throws SQLException {
        Date date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    static LocalDateTime readTimestamp(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    static void setDate(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    static void setTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    static void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    static void setBytesOrNull(PreparedStatement ps, int index, byte[] value) throws SQLException {
        if (value == null || value.length == 0) {
            ps.setNull(index, Types.BINARY);
        } else {
            ps.setBytes(index, value);
        }
    }
}
