
package com.hms.database;

import com.hms.util.DataAccessException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central point for obtaining JDBC connections and bootstrapping the schema.
 *
 * The system ships wired for SQLite (file-based, zero external setup) so the
 * project runs immediately after cloning. A MySQL-flavoured schema is also
 * provided (sql/mysql_schema.sql) and switching the connection target is a
 * one-line change — see docs/DATABASE_GUIDE.md for exact steps.
 *
 * A single shared connection is used because SQLite serializes writes
 * internally and this is a single-user desktop application; for a
 * true multi-connection pool (e.g. against MySQL in production) swap this
 * class's body for a HikariCP DataSource without touching any repository code,
 * since all repositories depend only on DatabaseConnection.getConnection().
 */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Cross-platform path handling using java.nio.file.Path
    private static final Path DB_FOLDER_PATH = Paths.get(System.getProperty("user.home"), ".hms");
    private static final Path DB_FILE_PATH = DB_FOLDER_PATH.resolve("hospital.db");
    private static final String URL = "jdbc:sqlite:" + DB_FILE_PATH.toAbsolutePath();

    private static Connection connection;

    private DatabaseConnection() {
    }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Files.createDirectories(DB_FOLDER_PATH);
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                try (Statement st = connection.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
            }
            return connection;
        } catch (SQLException | ClassNotFoundException | IOException e) {
            throw new DataAccessException("Unable to connect to the database.", e);
        }
    }

    /**
     * Runs the bundled schema script (idempotent - uses CREATE TABLE IF NOT
     * EXISTS) and seeds demo data on first launch only. Also runs small,
     * additive migrations (like adding a column to an already-created
     * table) that CREATE TABLE IF NOT EXISTS can't express on its own.
     */
    public static synchronized void initializeDatabase() {
        Connection conn = getConnection();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/sql/schema.sqlite.sql")) {
            if (in == null) {
                throw new DataAccessException("schema.sqlite.sql not found on classpath.");
            }
            String script = stripLineComments(readAll(in));
            try (Statement st = conn.createStatement()) {
                for (String rawStatement : script.split(";")) {
                    String sql = rawStatement.trim();
                    if (!sql.isEmpty()) {
                        st.execute(sql);
                    }
                }
            }
            runMigrations(conn);
            LOGGER.info("Database schema initialized/verified successfully.");
        } catch (SQLException | IOException e) {
            throw new DataAccessException("Failed to initialize database schema.", e);
        }
    }

    /**
     * Additive, non-destructive column migrations for databases created by
     * an earlier version of this app (e.g. before profile pictures were
     * added). Each migration checks PRAGMA table_info() first so it's safe
     * to run on every startup, on both brand-new and pre-existing databases.
     */
    private static void runMigrations(Connection conn) throws SQLException {
        addColumnIfMissing(conn, "users", "avatar_path", "TEXT");
        addColumnIfMissing(conn, "patients", "room_id", "INTEGER");
        addColumnIfMissing(conn, "invoices", "payment_status", "TEXT NOT NULL DEFAULT 'UNPAID'");
    }

    private static void addColumnIfMissing(Connection conn, String table, String column, String type) throws SQLException {
        boolean exists = false;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }
        if (!exists) {
            try (Statement st = conn.createStatement()) {
                st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
            }
            LOGGER.info("Migration: added column " + table + "." + column);
        }
    }

    /**
     * Removes full-line "-- comment" lines before the schema script is
     * split on semicolons.
     */
    private static String stripLineComments(String script) {
        StringBuilder sb = new StringBuilder();
        for (String line : script.split("\n", -1)) {
            if (!line.trim().startsWith("--")) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private static String readAll(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        }
    }

    /**
     * Retrieves the autoincrement id via SQLite's connection-scoped
     * last_insert_rowid() function.
     */
    public static int lastInsertRowId(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }

    public static Path getDatabaseFilePath() {
        return DB_FILE_PATH;
    }
}