package model;
 
/**
 * Judge subclass — access to assigned cases and personal analytics.
 */
public class Judge extends User {
 
    private String specialization; // e.g., "Criminal", "Civil"
 
    public Judge(int userId, String username, String password, String specialization) {
        super(userId, username, password, "Judge");
        this.specialization = specialization;
    }
 
    @Override
    public void displayDashboard() {
        System.out.println("=== JUDGE DASHBOARD ===");
        System.out.println("Specialization : " + specialization);
        System.out.println("Access         : Assigned Cases, Case Status Update");
    }
 
    /** Judge updates the status of a case they are presiding over */
    public void updateCaseStatus(int caseId, String newStatus) {
        System.out.println("[Judge " + getUsername() + "] Updating case #"
                + caseId + " → " + newStatus);
    }
 
    public String getSpecialization()               { return specialization; }
    public void   setSpecialization(String spec)    { this.specialization = spec; }
}
