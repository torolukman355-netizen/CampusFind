package com.campusfind.repository;

import com.campusfind.exception.StorageException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Membuat skema tabel PostgreSQL yang dibutuhkan CampusFind bila belum ada.
 * <p>
 * Seluruh pernyataan bersifat idempoten ({@code IF NOT EXISTS}) sehingga aman
 * dijalankan setiap aplikasi mulai.
 */
public final class DatabaseSchema {

    private static final String[] STATEMENTS = {

            """
            CREATE TABLE IF NOT EXISTS pos (
                id                VARCHAR(20)  PRIMARY KEY,
                nama              VARCHAR(150) NOT NULL,
                lokasi_penyerahan VARCHAR(255) NOT NULL,
                label_pendek      VARCHAR(80)  NOT NULL
            )
            """,

            """
            CREATE TABLE IF NOT EXISTS items (
                id                      VARCHAR(20)  PRIMARY KEY,
                jenis                   VARCHAR(20)  NOT NULL,
                nama                    VARCHAR(200) NOT NULL,
                kategori                VARCHAR(40)  NOT NULL,
                deskripsi               TEXT,
                lokasi_kejadian         VARCHAR(255),
                tanggal_kejadian        DATE,
                pos_id                  VARCHAR(255),
                tempat_penitipan        VARCHAR(255),
                foto_path               VARCHAR(500),
                foto_bytes              BYTEA,
                ciri_rahasia            TEXT,
                status                  VARCHAR(30)  NOT NULL,
                dibuat_pada             TIMESTAMP    NOT NULL,
                pelapor_nomor_identitas VARCHAR(40),
                pelapor_nama            VARCHAR(150),
                pelapor_role            VARCHAR(20),
                merek                   VARCHAR(150),
                nomor_seri              VARCHAR(150),
                warna                   VARCHAR(80),
                bahan                   VARCHAR(80),
                foto_bytes              BYTEA,
                tempat_penitipan        VARCHAR(255)
            )
            """,

            "ALTER TABLE items ADD COLUMN IF NOT EXISTS foto_bytes BYTEA",
            "ALTER TABLE items ADD COLUMN IF NOT EXISTS tempat_penitipan VARCHAR(255)",

            """
            CREATE TABLE IF NOT EXISTS klaim (
                id                         VARCHAR(30) PRIMARY KEY,
                item_id                    VARCHAR(20) NOT NULL REFERENCES items(id) ON DELETE CASCADE,
                pengklaim_nomor_identitas  VARCHAR(40) NOT NULL,
                pengklaim_nama             VARCHAR(150),
                pengklaim_role             VARCHAR(20) NOT NULL,
                ciri_khusus                TEXT,
                bukti_path                 VARCHAR(500),
                bukti_bytes                BYTEA,
                tanggal_serah_terima       DATE,
                sesi                       VARCHAR(20) NOT NULL,
                pos_id                     VARCHAR(255),
                tempat_penitipan           VARCHAR(255),
                pernyataan_identitas_asli  BOOLEAN     NOT NULL DEFAULT FALSE,
                pernyataan_kepemilikan     BOOLEAN     NOT NULL DEFAULT FALSE,
                skor_kecocokan             INTEGER     NOT NULL DEFAULT 0,
                status                     VARCHAR(30) NOT NULL,
                diajukan_pada              TIMESTAMP   NOT NULL,
                catatan_petugas            TEXT
            )
            """,

            """
            CREATE TABLE IF NOT EXISTS notifications (
                id                        VARCHAR(40) PRIMARY KEY,
                penerima_nomor_identitas  VARCHAR(40) NOT NULL,
                tipe                      VARCHAR(30) NOT NULL,
                judul                     VARCHAR(200) NOT NULL,
                isi                       TEXT,
                referensi_id              VARCHAR(60),
                dibuat_pada               TIMESTAMP   NOT NULL,
                dibaca                    BOOLEAN     NOT NULL DEFAULT FALSE
            )
            """,

            """
            CREATE TABLE IF NOT EXISTS app_sequence (
                nama  VARCHAR(40) PRIMARY KEY,
                nilai INTEGER     NOT NULL
            )
            """,

            """
            CREATE TABLE IF NOT EXISTS users (
                nomor_identitas   VARCHAR(40)  PRIMARY KEY,
                nama              VARCHAR(150) NOT NULL,
                email             VARCHAR(150) NOT NULL UNIQUE,
                password_hash     VARCHAR(255),
                role              VARCHAR(20)  NOT NULL,
                status            VARCHAR(20)  NOT NULL DEFAULT 'AKTIF',
                dibuat_pada       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """,

            "CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users (LOWER(email))",
            "CREATE INDEX IF NOT EXISTS ix_users_role ON users (role)",

            "CREATE INDEX IF NOT EXISTS ix_items_jenis ON items (jenis)",
            "CREATE INDEX IF NOT EXISTS ix_items_status ON items (status)",
            "CREATE INDEX IF NOT EXISTS ix_items_pelapor ON items (pelapor_nomor_identitas)",
            "CREATE INDEX IF NOT EXISTS ix_klaim_item ON klaim (item_id)",
            "CREATE INDEX IF NOT EXISTS ix_klaim_pengklaim ON klaim (pengklaim_nomor_identitas)",
            "CREATE INDEX IF NOT EXISTS ix_notifikasi_penerima ON notifications (penerima_nomor_identitas)",

            // Migrasi kolom baru jika tabel sudah pernah dibuat sebelumnya
            "ALTER TABLE items ADD COLUMN IF NOT EXISTS foto_bytes BYTEA",
            "ALTER TABLE klaim ADD COLUMN IF NOT EXISTS bukti_bytes BYTEA",
            "ALTER TABLE items DROP CONSTRAINT IF EXISTS items_pos_id_fkey",
            "ALTER TABLE klaim DROP CONSTRAINT IF EXISTS klaim_pos_id_fkey",
            "ALTER TABLE items ALTER COLUMN pos_id TYPE VARCHAR(255)",
            "ALTER TABLE items ADD COLUMN IF NOT EXISTS tempat_penitipan VARCHAR(255)",
            "ALTER TABLE klaim ALTER COLUMN pos_id TYPE VARCHAR(255)",
            "ALTER TABLE klaim ADD COLUMN IF NOT EXISTS tempat_penitipan VARCHAR(255)",

            // Satu item hanya boleh punya satu klaim aktif.
            """
            CREATE UNIQUE INDEX IF NOT EXISTS ux_klaim_aktif
                ON klaim (item_id)
                WHERE status IN ('DIAJUKAN', 'DISETUJUI', 'MENUNGGU_VERIFIKASI')
            """
    };

    private DatabaseSchema() {
    }

    /**
     * Membuat seluruh tabel dan indeks yang belum ada.
     *
     * @param manager sumber koneksi database
     * @throws StorageException bila skema gagal dibuat
     */
    public static void ensureSchema(DatabaseManager manager) {
        try (Connection connection = manager.getConnection();
             Statement statement = connection.createStatement()) {
            for (String ddl : STATEMENTS) {
                statement.execute(ddl);
            }
        } catch (SQLException e) {
            throw new StorageException(
                    "Gagal menyiapkan skema database: " + e.getMessage(), e);
        }
    }
}
