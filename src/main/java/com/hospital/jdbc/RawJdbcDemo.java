package com.hospital.jdbc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Traditional JDBC, shown side by side with Spring JDBC.
 *
 * <p>The repositories use {@code JdbcTemplate} (Spring JDBC). This class
 * does the same kind of work the old way, with the raw JDBC interfaces
 * from {@code java.sql}: {@link DriverManager} opens a
 * {@link Connection}, the Connection creates a {@link PreparedStatement},
 * the statement returns a {@link ResultSet}, and every one of them must
 * be closed by hand (try-with-resources does it here). Compare with a
 * repository method: JdbcTemplate performs exactly these steps for you,
 * which is why the repositories contain SQL and mapping only, and no
 * connection handling at all.</p>
 */
@Component
public class RawJdbcDemo {

    private final String url;
    private final String username;
    private final String password;

    public RawJdbcDemo(@Value("${spring.datasource.url}") String url,
                       @Value("${spring.datasource.username}") String username,
                       @Value("${spring.datasource.password}") String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /** Prints the row counts of all three tables using plain JDBC. */
    public void showCounts() {
        System.out.println("--- Traditional JDBC check (DriverManager + PreparedStatement) ---");
        try (Connection con = DriverManager.getConnection(url, username, password)) {
            System.out.println("Connected with plain JDBC: " + con.getMetaData().getDatabaseProductName()
                    + " " + con.getMetaData().getDatabaseProductVersion());
            for (String table : new String[]{"patient", "doctor", "appointment"}) {
                int n = countRows(con, table);
                System.out.println("  " + table + ": " + n + " row(s)");
            }
        } catch (SQLException e) {
            System.out.println("Plain-JDBC check failed: " + e.getMessage());
        }
        System.out.println("--- End of JDBC check ---\n");
    }

    private int countRows(Connection con, String table) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + table;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
