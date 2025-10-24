package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Database file will be created in the project root (IMS) directory
    private static final String URL = "jdbc:sqlite:inventory.db";

    public static Connection getConnection() throws SQLException {
        // Load the SQLite driver and establish the connection
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // SQL to create the table if it doesn't exist
            String sql = "CREATE TABLE IF NOT EXISTS items (" +
                         "name TEXT PRIMARY KEY," +
                         "quantity INTEGER NOT NULL," +
                         "price REAL NOT NULL)";
            
            stmt.execute(sql);
            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}