package dao;
 
import model.User;
import model.Admin;
import model.Judge;
import model.Clerk;
 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Data Access Object for User entity.
 * Handles all CRUD operations on the users table via JDBC.
 */
public class UserDAO {
 
    // ── CREATE ────────────────────────────────────────────────────────────────
    public boolean addUser(String username, String password, String role,
                           String extra) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, extra_info) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setString(1, username);
            ps.setString(2, hashPassword(password));  // store hashed password
            ps.setString(3, role);
            ps.setString(4, extra);
            return ps.executeUpdate() > 0;
        }
    }
 
    // ── READ (authenticate) ───────────────────────────────────────────────────
    /**
     * Authenticates a user and returns the appropriate subclass instance.
     * Returns null if credentials are invalid.
     */
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setString(1, username);
            ps.setString(2, hashPassword(password));
            ResultSet rs = ps.executeQuery();
 
            if (rs.next()) {
                int    id   = rs.getInt("user_id");
                String role = rs.getString("role");
                String extra = rs.getString("extra_info");
 
                // Polymorphic object creation based on role
                return switch (role) {
                    case "Admin" -> new Admin(id, username, password);
                    case "Judge" -> new Judge(id, username, password, extra);
                    case "Clerk" -> new Clerk(id, username, password, extra);
                    default      -> null;
                };
            }
        }
        return null;
    }
 
    // ── READ (all users) ──────────────────────────────────────────────────────
    public List<String[]> getAllUsers() throws SQLException {
        List<String[]> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role, created_at FROM users ORDER BY user_id";
 
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
 
            while (rs.next()) {
                users.add(new String[]{
                    rs.getString("user_id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("created_at")
                });
            }
        }
        return users;
    }
 
    // ── DELETE ────────────────────────────────────────────────────────────────
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }
 
    // ── UTILITY: simple password hash (use BCrypt in production) ─────────────
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md =
                java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return password; // fallback (not for production)
        }
    }
}
