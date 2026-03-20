package dao;
 
import model.Case;
import exception.InvalidCaseException;
 
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
 
/**
 * Data Access Object for Case entity.
 * All JDBC operations for the cases table.
 */
public class CaseDAO {
 
    // ── CREATE ────────────────────────────────────────────────────────────────
    public boolean addCase(Case c) throws SQLException, InvalidCaseException {
        if (c.getTitle() == null || c.getTitle().isBlank())
            throw new InvalidCaseException("Case title cannot be empty.",
                    InvalidCaseException.ERR_NULL_TITLE);
 
        String sql = "INSERT INTO cases (case_number, title, case_type, petitioner, "
                   + "respondent, filing_date, status, assigned_judge_id, court_id, description) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {
 
            ps.setString(1, c.getCaseNumber());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getCaseType());
            ps.setString(4, c.getPetitioner());
            ps.setString(5, c.getRespondent());
            ps.setDate  (6, Date.valueOf(c.getFilingDate()));
            ps.setString(7, c.getStatus() != null ? c.getStatus() : Case.STATUS_PENDING);
            ps.setObject(8, c.getAssignedJudgeId() == 0 ? null : c.getAssignedJudgeId());
            ps.setObject(9, c.getCourtId() == 0 ? null : c.getCourtId());
            ps.setString(10, c.getDescription());
 
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) c.setCaseId(keys.getInt(1));
            }
            return rows > 0;
        }
    }
 
    // ── READ (single by ID) ───────────────────────────────────────────────────
    public Case getCaseById(int caseId) throws SQLException {
        String sql = "SELECT * FROM cases WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setInt(1, caseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }
 
    // ── READ (all cases) ──────────────────────────────────────────────────────
    public List<Case> getAllCases() throws SQLException {
        List<Case> list = new ArrayList<>();
        String sql = "SELECT * FROM cases ORDER BY filing_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }
 
    // ── READ (pending cases) ──────────────────────────────────────────────────
    public List<Case> getPendingCases() throws SQLException {
        List<Case> list = new ArrayList<>();
        String sql = "SELECT * FROM cases WHERE status IN ('Pending','In Progress') "
                   + "ORDER BY filing_date ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }
 
    // ── READ (cases by judge) ─────────────────────────────────────────────────
    public List<Case> getCasesByJudge(int judgeId) throws SQLException {
        List<Case> list = new ArrayList<>();
        String sql = "SELECT * FROM cases WHERE assigned_judge_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, judgeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }
 
    // ── UPDATE (status) ───────────────────────────────────────────────────────
    public boolean updateCaseStatus(int caseId, String newStatus,
                                    int updatedByUserId) throws SQLException {
        String sql = "UPDATE cases SET status = ?, resolution_date = ? "
                   + "WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
 
            ps.setString(1, newStatus);
            ps.setDate(2, newStatus.equals(Case.STATUS_RESOLVED)
                    ? Date.valueOf(LocalDate.now()) : null);
            ps.setInt(3, caseId);
            boolean ok = ps.executeUpdate() > 0;
 
            // Log to case_history
            if (ok) logHistory(caseId, updatedByUserId, newStatus, conn);
            return ok;
        }
    }
 
    // ── UPDATE (assign judge) ─────────────────────────────────────────────────
    public boolean assignJudge(int caseId, int judgeId) throws SQLException {
        String sql = "UPDATE cases SET assigned_judge_id = ? WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, judgeId);
            ps.setInt(2, caseId);
            return ps.executeUpdate() > 0;
        }
    }
 
    // ── DELETE ────────────────────────────────────────────────────────────────
    public boolean deleteCase(int caseId) throws SQLException {
        String sql = "DELETE FROM cases WHERE case_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            return ps.executeUpdate() > 0;
        }
    }
 
    // ── ANALYTICS: backlog count ──────────────────────────────────────────────
    public int getPendingCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM cases WHERE status IN ('Pending','In Progress')";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
 
    // ── ANALYTICS: average resolution days ───────────────────────────────────
    public double getAvgResolutionDays() throws SQLException {
        String sql = "SELECT AVG(DATEDIFF(resolution_date, filing_date)) "
                   + "FROM cases WHERE resolution_date IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }
 
    // ── ANALYTICS: long-pending cases (> given days) ─────────────────────────
    public List<Case> getLongPendingCases(int thresholdDays) throws SQLException {
        List<Case> list = new ArrayList<>();
        String sql = "SELECT * FROM cases "
                   + "WHERE status IN ('Pending','In Progress') "
                   + "AND DATEDIFF(CURDATE(), filing_date) > ? "
                   + "ORDER BY filing_date ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, thresholdDays);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }
 
    // ── PRIVATE: map ResultSet row to Case object ─────────────────────────────
    private Case mapRow(ResultSet rs) throws SQLException {
        Case c = new Case();
        c.setCaseId(rs.getInt("case_id"));
        c.setCaseNumber(rs.getString("case_number"));
        c.setTitle(rs.getString("title"));
        c.setCaseType(rs.getString("case_type"));
        c.setPetitioner(rs.getString("petitioner"));
        c.setRespondent(rs.getString("respondent"));
        c.setStatus(rs.getString("status"));
        c.setDescription(rs.getString("description"));
 
        Date fd = rs.getDate("filing_date");
        if (fd != null) c.setFilingDate(fd.toLocalDate());
 
        Date rd = rs.getDate("resolution_date");
        if (rd != null) c.setResolutionDate(rd.toLocalDate());
 
        int judgeId = rs.getInt("assigned_judge_id");
        if (!rs.wasNull()) c.setAssignedJudgeId(judgeId);
 
        int courtId = rs.getInt("court_id");
        if (!rs.wasNull()) c.setCourtId(courtId);
 
        return c;
    }
 
    // ── PRIVATE: insert case_history record ───────────────────────────────────
    private void logHistory(int caseId, int userId,
                            String newStatus, Connection conn) {
        try {
            String sql = "INSERT INTO case_history "
                       + "(case_id, updated_by, new_status, remarks) "
                       + "VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, caseId);
            ps.setInt(2, userId);
            ps.setString(3, newStatus);
            ps.setString(4, "Status updated to: " + newStatus);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DAO] History log failed: " + e.getMessage());
        }
    }
}
