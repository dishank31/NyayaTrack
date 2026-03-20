package util;
 
import model.Case;
 
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
 
/**
 * Utility class for exporting case data and logging system events.
 * Demonstrates Java File I/O (java.io.*).
 */
public class ReportExporter {
 
    private static final String LOG_FILE = "reports/system_log.txt";
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
 
    // ── EXPORT: all cases to CSV ──────────────────────────────────────────────
    /**
     * Exports a list of cases to a CSV file at the given path.
     * Creates the reports/ directory if it doesn't exist.
     */
    public static void exportToCSV(List<Case> cases, String filePath)
            throws IOException {
 
        // Ensure output directory exists
        File dir = new File("reports");
        if (!dir.exists()) dir.mkdirs();
 
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
 
            // CSV header row
            writer.write("Case ID,Case Number,Title,Type,Petitioner,"
                       + "Respondent,Filing Date,Status,Days Pending");
            writer.newLine();
 
            // Data rows
            for (Case c : cases) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%d",
                        c.getCaseId(),
                        escapeCsv(c.getCaseNumber()),
                        escapeCsv(c.getTitle()),
                        escapeCsv(c.getCaseType()),
                        escapeCsv(c.getPetitioner()),
                        escapeCsv(c.getRespondent()),
                        c.getFilingDate() != null ? c.getFilingDate() : "N/A",
                        escapeCsv(c.getStatus()),
                        c.getDaysPending()));
                writer.newLine();
            }
        }
        log("CSV report exported to: " + filePath + " (" + cases.size() + " records)");
    }
 
    // ── EXPORT: pending cases summary to TXT ──────────────────────────────────
    public static void exportPendingSummary(List<Case> pending, String filePath)
            throws IOException {
 
        File dir = new File("reports");
        if (!dir.exists()) dir.mkdirs();
 
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("========================================");
            pw.println("  JUDICIAL BACKLOG SUMMARY REPORT");
            pw.println("  Generated: " + LocalDateTime.now().format(FMT));
            pw.println("========================================");
            pw.printf("  Total Pending Cases: %d%n", pending.size());
            pw.println("----------------------------------------");
            pw.printf("  %-6s %-20s %-12s %-15s%n",
                    "ID", "Title", "Type", "Days Pending");
            pw.println("----------------------------------------");
 
            for (Case c : pending) {
                pw.printf("  %-6d %-20s %-12s %-15d%n",
                        c.getCaseId(),
                        truncate(c.getTitle(), 20),
                        c.getCaseType(),
                        c.getDaysPending());
            }
            pw.println("========================================");
        }
        log("Pending summary exported to: " + filePath);
    }
 
    // ── SYSTEM LOGGER ─────────────────────────────────────────────────────────
    /**
     * Appends a timestamped log entry to the system log file.
     */
    public static void log(String message) {
        File dir = new File("reports");
        if (!dir.exists()) dir.mkdirs();
 
        try (FileWriter fw = new FileWriter(LOG_FILE, true); // append mode
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("[" + LocalDateTime.now().format(FMT) + "] " + message);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("[Logger] Failed to write log: " + e.getMessage());
        }
    }
 
    // ── HELPERS ───────────────────────────────────────────────────────────────
    private static String escapeCsv(String value) {
        if (value == null) return "";
        // Wrap in quotes if value contains comma, quote, or newline
        if (value.contains(",") || value.contains(""") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"\"") + "\"";
        return value;
    }
 
    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
