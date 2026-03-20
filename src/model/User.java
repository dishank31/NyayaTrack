package model;
 
/**
 * Abstract base class for all system users.
 * Demonstrates: Abstraction, Encapsulation, Inheritance
 */
public abstract class User {
 
    private int    userId;
    private String username;
    private String password;
    private String role;
 
    // ── Constructor ──────────────────────────────────────────────────────────
    public User(int userId, String username, String password, String role) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
        this.role     = role;
    }
 
    // ── Abstract method — each role implements its own dashboard logic ────────
    public abstract void displayDashboard();
 
    // ── Concrete shared method ────────────────────────────────────────────────
    public boolean authenticate(String inputUsername, String inputPassword) {
        return this.username.equals(inputUsername) && this.password.equals(inputPassword);
    }
 
    // ── Getters & Setters (Encapsulation) ─────────────────────────────────────
    public int    getUserId()              { return userId;   }
    public void   setUserId(int userId)    { this.userId = userId; }
 
    public String getUsername()            { return username; }
    public void   setUsername(String u)    { this.username = u; }
 
    public String getPassword()            { return password; }
    public void   setPassword(String p)    { this.password = p; }
 
    public String getRole()                { return role; }
    public void   setRole(String role)     { this.role = role; }
 
    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username + "', role='" + role + "'}";
    }
}
