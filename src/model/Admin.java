package model;
 
/**
 * Admin subclass — full system privileges.
 */
public class Admin extends User {
 
    public Admin(int userId, String username, String password) {
        super(userId, username, password, "Admin");
    }
 
    @Override
    public void displayDashboard() {
        System.out.println("=== ADMIN DASHBOARD ===");
        System.out.println("Access: All modules, User Management, Full Reports");
    }
 
    /** Admin-specific: manage other user accounts */
    public void manageUsers() {
        System.out.println("[Admin] Opening User Management module...");
    }
 
    /** Admin-specific: generate complete system report */
    public void generateFullReport() {
        System.out.println("[Admin] Generating full system report...");
    }
}
