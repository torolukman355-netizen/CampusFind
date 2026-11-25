package com.campusfind.repository;

import com.campusfind.exception.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Mengelola koneksi JDBC ke database PostgreSQL.
 * <p>
 * Konfigurasi dibaca secara berurutan (yang pertama ditemukan menang):
 * <ol>
 *     <li>Environment variable: {@code CAMPUSFIND_DB_URL}, {@code CAMPUSFIND_DB_USER},
 *         {@code CAMPUSFIND_DB_PASSWORD} (atau {@code DATABASE_URL},
 *         {@code DB_USER}, {@code DB_PASSWORD}).</li>
 *     <li>File {@code database.properties} di direktori kerja aplikasi.</li>
 *     <li>Nilai default pengembangan: {@code localhost:5432/campusfind}.</li>
 * </ol>
 * Kredensial tidak pernah ditulis di kode dan file {@code database.properties}
 * diabaikan oleh git.
 */
public class DatabaseManager {

    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());

    private static final String CONFIG_FILE_NAME = "database.properties";
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/campusfind";
    private static final String DEFAULT_USER = "campusfind";
    private static final String DEFAULT_PASSWORD = "campusfind";

    private final String url;
    private final String user;
    private final String password;
    private final String connectTimeout;

    /**
     * Membuat manager dengan konfigurasi dari environment variable /
     * {@code database.properties} / default.
     */
    public DatabaseManager() {
        this(loadConfig());
    }

    /**
     * Membuat manager dengan properti eksplisit (berguna untuk pengujian).
     *
     * @param config properti berisi {@code db.url}, {@code db.user}, {@code db.password}
     */
    public DatabaseManager(Properties config) {
        this.url = config.getProperty("db.url", DEFAULT_URL);
        this.user = config.getProperty("db.user", DEFAULT_USER);
        this.password = config.getProperty("db.password", DEFAULT_PASSWORD);
        this.connectTimeout = config.getProperty("db.connectTimeout", "5");
    }

    /**
     * Membuka koneksi baru ke database.
     *
     * @return koneksi JDBC yang masih terbuka
     * @throws StorageException bila driver tidak ada atau koneksi gagal
     */
    public Connection getConnection() {
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("connectTimeout", connectTimeout);
        try {
            return DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            throw new StorageException(
                    "Gagal terhubung ke database " + safeUrl() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Menguji koneksi dan mencetak informasi server ke stdout.
     *
     * @return {@code true} bila koneksi berhasil
     * @throws StorageException bila koneksi gagal (dengan pesan yang jelas)
     */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            System.out.println("Koneksi database BERHASIL.");
            System.out.println("  URL      : " + safeUrl());
            System.out.println("  Pengguna : " + user);
            System.out.println("  Server   : " + meta.getDatabaseProductName()
                    + " " + meta.getDatabaseProductVersion());
            System.out.println("  Driver   : " + meta.getDriverName()
                    + " " + meta.getDriverVersion());
            System.out.println("  Database : " + connection.getCatalog());
            System.out.println("  Versi DB : " + queryDatabaseVersion(connection));
            return true;
        } catch (SQLException e) {
            throw new StorageException(
                    "Gagal terhubung ke database " + safeUrl() + ": " + e.getMessage(), e);
        }
    }

    /**
     * URL koneksi tanpa kredensial, aman untuk dicetak ke log.
     */
    public String safeUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private static String queryDatabaseVersion(Connection connection) {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT version()")) {
            return rs.next() ? rs.getString(1) : "tidak diketahui";
        } catch (SQLException e) {
            LOG.fine("Tidak dapat membaca versi database: " + e.getMessage());
            return "tidak diketahui";
        }
    }

    private static Properties loadConfig() {
        Properties props = new Properties();

        // 1. File database.properties (opsional).
        Path configPath = Path.of(System.getProperty("user.dir"), CONFIG_FILE_NAME);
        if (Files.exists(configPath)) {
            try (InputStream in = Files.newInputStream(configPath)) {
                props.load(in);
                LOG.info("Konfigurasi database dimuat dari " + configPath.toAbsolutePath());
            } catch (IOException e) {
                LOG.warning("Gagal membaca " + configPath + ": " + e.getMessage());
            }
        }

        // 2. Environment variable menimpa file konfigurasi.
        applyEnv(props, "db.url", "CAMPUSFIND_DB_URL", "DATABASE_URL");
        applyEnv(props, "db.user", "CAMPUSFIND_DB_USER", "DB_USER");
        applyEnv(props, "db.password", "CAMPUSFIND_DB_PASSWORD", "DB_PASSWORD");

        return props;
    }

    private static void applyEnv(Properties props, String key, String... envNames) {
        for (String envName : envNames) {
            String value = System.getenv(envName);
            if (value != null && !value.isBlank()) {
                props.setProperty(key, value);
                return;
            }
        }
    }
}
