package dao;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
/**
 * Singleton JDBC connection manager.
 * Provides a single shared connection to the MySQL database.
 */
public class DBConnection {
 
    // ── Database configuration — update before running ───────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/judicial_system_db"
                                         + "?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Dishank@31";   // ← Change this
 
    private static Connection instance = null;
 
    /** Private constructor — prevents direct instantiation */
    private DBConnection() {}
 
    /**
     * Returns the singleton Connection instance.
     * Creates a new connection if none exists or if the existing one is closed.
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (instance == null || instance.isClosed()) {
                // Load MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("[DB] Connection established successfully.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. "
                    + "Add mysql-connector-j JAR to classpath.", e);
        }
        return instance;
    }
 
    /** Gracefully close the connection */
    public static void closeConnection() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                instance = null;
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
