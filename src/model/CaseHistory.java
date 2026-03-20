package model;
 
import java.time.LocalDateTime;
 
/**
 * Records every status change or update made to a Case.
 * Used for audit trail and transparency.
 */
public class CaseHistory {
 
    private int           historyId;
    private int           caseId;
    private int           updatedBy;      // userId of the person who made the change
    private LocalDateTime updateTime;
    private String        oldStatus;
    private String        newStatus;
    private String        remarks;
 
    // ── Constructor ──────────────────────────────────────────────────────────
    public CaseHistory(int caseId, int updatedBy,
                       String oldStatus, String newStatus, String remarks) {
        this.caseId     = caseId;
        this.updatedBy  = updatedBy;
        this.oldStatus  = oldStatus;
        this.newStatus  = newStatus;
        this.remarks    = remarks;
        this.updateTime = LocalDateTime.now();
    }
 
    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int           getHistoryId()            { return historyId; }
    public void          setHistoryId(int id)       { this.historyId = id; }
 
    public int           getCaseId()               { return caseId; }
    public int           getUpdatedBy()             { return updatedBy; }
    public LocalDateTime getUpdateTime()            { return updateTime; }
    public String        getOldStatus()             { return oldStatus; }
    public String        getNewStatus()             { return newStatus; }
    public String        getRemarks()               { return remarks; }
 
    @Override
    public String toString() {
        return String.format("History{caseId=%d, %s → %s, by=%d, at=%s}",
                caseId, oldStatus, newStatus, updatedBy, updateTime);
    }
