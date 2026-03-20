package service;
 
import dao.CaseDAO;
import exception.InvalidCaseException;
import model.Case;
 
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Business logic layer for Case operations.
 * Validates data and delegates persistence to CaseDAO.
 */
public class CaseService {
 
    private final CaseDAO caseDAO = new CaseDAO();
 
    // ── ADD CASE ──────────────────────────────────────────────────────────────
    public boolean addCase(Case c) throws InvalidCaseException {
        // Business rule validations
        if (c.getTitle() == null || c.getTitle().isBlank())
            throw new InvalidCaseException("Case title is required.",
                    InvalidCaseException.ERR_NULL_TITLE);
 
        if (c.getFilingDate() != null && c.getFilingDate().isAfter(LocalDate.now()))
            throw new InvalidCaseException("Filing date cannot be in the future.",
                    InvalidCaseException.ERR_PAST_DATE);
 
        if (!isValidStatus(c.getStatus()))
            throw new InvalidCaseException("Invalid status: " + c.getStatus(),
                    InvalidCaseException.ERR_INVALID_STATUS);
 
        try {
            return caseDAO.addCase(c);
        } catch (SQLException e) {
            System.err.println("[Service] DB error adding case: " + e.getMessage());
            return false;
        }
    }
 
    // ── UPDATE STATUS ─────────────────────────────────────────────────────────
    public boolean updateCaseStatus(int caseId, String newStatus, int userId)
            throws InvalidCaseException {
 
        if (!isValidStatus(newStatus))
            throw new InvalidCaseException("Invalid status value: " + newStatus,
                    InvalidCaseException.ERR_INVALID_STATUS);
 
        try {
            return caseDAO.updateCaseStatus(caseId, newStatus, userId);
        } catch (SQLException e) {
            System.err.println("[Service] DB error updating status: " + e.getMessage());
            return false;
        }
    }
 
    // ── ASSIGN JUDGE ──────────────────────────────────────────────────────────
    public boolean assignJudge(int caseId, int judgeId) throws InvalidCaseException {
        if (judgeId <= 0)
            throw new InvalidCaseException("Invalid judge ID.",
                    InvalidCaseException.ERR_NO_JUDGE);
        try {
            return caseDAO.assignJudge(caseId, judgeId);
        } catch (SQLException e) {
            System.err.println("[Service] DB error assigning judge: " + e.getMessage());
            return false;
        }
    }
 
    // ── GET ALL CASES ─────────────────────────────────────────────────────────
    public List<Case> getAllCases() {
        try {
            return caseDAO.getAllCases();
        } catch (SQLException e) {
            System.err.println("[Service] Error fetching cases: " + e.getMessage());
            return new ArrayList<>();
        }
    }
 
    // ── GET PENDING CASES ─────────────────────────────────────────────────────
    public List<Case> getPendingCases() {
        try {
            return caseDAO.getPendingCases();
        } catch (SQLException e) {
            System.err.println("[Service] Error fetching pending cases: " + e.getMessage());
            return new ArrayList<>();
        }
    }
 
    // ── BACKLOG ANALYTICS ─────────────────────────────────────────────────────
    public int getTotalBacklog() {
        try { return caseDAO.getPendingCount(); }
        catch (SQLException e) { return 0; }
    }
 
    public double getAvgResolutionDays() {
        try { return caseDAO.getAvgResolutionDays(); }
        catch (SQLException e) { return 0.0; }
    }
 
    public List<Case> getLongPendingCases(int thresholdDays) {
        try { return caseDAO.getLongPendingCases(thresholdDays); }
        catch (SQLException e) { return new ArrayList<>(); }
    }
 
    // ── PRIVATE HELPER ────────────────────────────────────────────────────────
    private boolean isValidStatus(String status) {
        return status != null && status.matches(
                "Pending|In Progress|Resolved|Dismissed");
    }
}
