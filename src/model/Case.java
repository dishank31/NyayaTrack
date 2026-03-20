package model;
 
import java.time.LocalDate;
 
/**
 * Represents a judicial case entity.
 * Encapsulates all case-related attributes.
 */
public class Case {
 
    // Status constants
    public static final String STATUS_PENDING     = "Pending";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_RESOLVED    = "Resolved";
    public static final String STATUS_DISMISSED   = "Dismissed";
 
    private int       caseId;
    private String    caseNumber;   // e.g., "CIV/2024/001"
    private String    title;
    private String    caseType;     // Civil, Criminal, Family, Property
    private String    petitioner;
    private String    respondent;
    private LocalDate filingDate;
    private LocalDate hearingDate;
    private LocalDate resolutionDate;
    private String    status;
    private int       assignedJudgeId;
    private int       courtId;
    private String    description;
 
    // ── Constructors ─────────────────────────────────────────────────────────
    public Case() {}
 
    public Case(int caseId, String caseNumber, String title, String caseType,
                String petitioner, String respondent, LocalDate filingDate, String status) {
        this.caseId      = caseId;
        this.caseNumber  = caseNumber;
        this.title       = title;
        this.caseType    = caseType;
        this.petitioner  = petitioner;
        this.respondent  = respondent;
        this.filingDate  = filingDate;
        this.status      = status;
    }
 
    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int       getCaseId()                        { return caseId; }
    public void      setCaseId(int caseId)              { this.caseId = caseId; }
 
    public String    getCaseNumber()                    { return caseNumber; }
    public void      setCaseNumber(String caseNumber)   { this.caseNumber = caseNumber; }
 
    public String    getTitle()                         { return title; }
    public void      setTitle(String title)             { this.title = title; }
 
    public String    getCaseType()                      { return caseType; }
    public void      setCaseType(String caseType)       { this.caseType = caseType; }
 
    public String    getPetitioner()                    { return petitioner; }
    public void      setPetitioner(String petitioner)   { this.petitioner = petitioner; }
 
    public String    getRespondent()                    { return respondent; }
    public void      setRespondent(String respondent)   { this.respondent = respondent; }
 
    public LocalDate getFilingDate()                    { return filingDate; }
    public void      setFilingDate(LocalDate d)         { this.filingDate = d; }
 
    public LocalDate getHearingDate()                   { return hearingDate; }
    public void      setHearingDate(LocalDate d)        { this.hearingDate = d; }
 
    public LocalDate getResolutionDate()                { return resolutionDate; }
    public void      setResolutionDate(LocalDate d)     { this.resolutionDate = d; }
 
    public String    getStatus()                        { return status; }
    public void      setStatus(String status)           { this.status = status; }
 
    public int       getAssignedJudgeId()               { return assignedJudgeId; }
    public void      setAssignedJudgeId(int id)         { this.assignedJudgeId = id; }
 
    public int       getCourtId()                       { return courtId; }
    public void      setCourtId(int courtId)            { this.courtId = courtId; }
 
    public String    getDescription()                   { return description; }
    public void      setDescription(String desc)        { this.description = desc; }
 
    /** Returns days since filing — used for backlog analytics */
    public long getDaysPending() {
        if (resolutionDate != null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(filingDate, LocalDate.now());
    }
 
    @Override
    public String toString() {
        return String.format("Case{#%d | %s | %s | %s | %s}",
                caseId, caseNumber, caseType, status, petitioner);
    }
}
