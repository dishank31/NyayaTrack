package model;
 
/**
 * Clerk subclass — data entry and case registration.
 */
public class Clerk extends User {
 
    private String courtId; // clerk is assigned to a specific court
 
    public Clerk(int userId, String username, String password, String courtId) {
        super(userId, username, password, "Clerk");
        this.courtId = courtId;
    }
 
    @Override
    public void displayDashboard() {
        System.out.println("=== CLERK DASHBOARD ===");
        System.out.println("Court ID : " + courtId);
        System.out.println("Access   : Register Case, Schedule Hearing, Basic Reports");
    }
 
    public void registerCase(String caseTitle) {
        System.out.println("[Clerk " + getUsername() + "] Registering case: " + caseTitle);
    }
 
    public String getCourtId()               { return courtId; }
    public void   setCourtId(String courtId) { this.courtId = courtId; }
}
