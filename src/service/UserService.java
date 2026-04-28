package service;
 
import dao.UserDAO;
import model.User;
import java.sql.SQLException;
import java.util.List;
 
/**
 * Business logic layer for User operations.
 * Sits between the UI layer and the DAO layer.
 */
public class UserService {
 
    private final UserDAO userDAO = new UserDAO();
 
    /**
     * Authenticate user credentials.
     * Returns User object on success, null on failure.
     */
    public User login(String username, String password) {
        try {
            if (username == null || username.isBlank()
                    || password == null || password.isBlank()) {
                System.err.println("[Service] Login failed: empty credentials.");
                return null;
            }
            User user = userDAO.authenticate(username, password);
            if (user == null)
                System.err.println("[Service] Login failed: invalid credentials.");
            return user;
        } catch (SQLException e) {
            System.err.println("[Service] DB error during login: " + e.getMessage());
            return null;
        }
    }
 
    /**
     * Register a new system user.
     * Validates inputs before calling DAO.
     */
    public boolean registerUser(String username, String password,
                                String role, String extra) {
        try {
            if (username == null || username.length() < 3)
                throw new IllegalArgumentException("Username must be at least 3 characters.");
            if (password == null || password.length() < 6)
                throw new IllegalArgumentException("Password must be at least 6 characters.");
            if (!role.matches("Admin|Judge|Clerk"))
                throw new IllegalArgumentException("Invalid role: " + role);
 
            return userDAO.addUser(username, password, role, extra);
        } catch (IllegalArgumentException e) {
            System.err.println("[Service] Validation error: " + e.getMessage());
            return false;
        } catch (SQLException e) {
            System.err.println("[Service] DB error registering user: " + e.getMessage());
            return false;
        }
    }
 
    /**
     * Check if a username already exists in the database.
     */
    public boolean usernameExists(String username) {
        try {
            return userDAO.usernameExists(username);
        } catch (java.sql.SQLException e) {
            System.err.println("[Service] DB error checking username: " + e.getMessage());
            return false;
        }
    }

    /**
     * Register a new system user with detailed error messaging.
     * Returns null on success, or an error message string on failure.
     */
    public String registerUserWithMessage(String username, String password,
                                          String role, String extra) {
        try {
            if (username == null || username.length() < 3)
                return "Username must be at least 3 characters.";
            if (password == null || password.length() < 6)
                return "Password must be at least 6 characters.";
            if (!role.matches("Admin|Judge|Clerk"))
                return "Invalid role: " + role;
            if (usernameExists(username))
                return "Username '" + username + "' is already taken.";

            boolean success = userDAO.addUser(username, password, role, extra);
            return success ? null : "Registration failed. Please try again.";
        } catch (java.sql.SQLException e) {
            System.err.println("[Service] DB error registering user: " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }

    /** Fetch all registered users (Admin use) */
    public List<String[]> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (SQLException e) {
            System.err.println("[Service] Error fetching users: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Delete a user account by user ID.
     * Returns true on success, false on failure.
     */
    public boolean deleteUser(int userId) {
        try {
            boolean deleted = userDAO.deleteUser(userId);
            if (deleted) {
                System.out.println("[Service] User ID " + userId + " deleted successfully.");
            } else {
                System.err.println("[Service] No user found with ID: " + userId);
            }
            return deleted;
        } catch (SQLException e) {
            System.err.println("[Service] DB error deleting user: " + e.getMessage());
            return false;
        }
    }
}
