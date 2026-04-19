package main;

import model.*;
import service.*;
import exception.*;
import thread.*;
import util.*;
import dao.*;
import ui.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * Entry point for the Nyaya Track — Judicial Case Monitoring System.
 *
 * Sets up global UI defaults (fonts, anti-aliasing) and launches the
 * LoginFrame on the Event Dispatch Thread.
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║   Nyaya Track — Judicial Case Monitoring      ║");
        System.out.println("║   & Backlog Analysis System  v2.0             ║");
        System.out.println("╚════════════════════════════════════════════════╝");

        // ── Apply global UI enhancements ──────────────────────────────────────
        try {
            // Use the native system look and feel as base
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("[Main] Could not set system L&F: " + e.getMessage());
        }

        // Enable font anti-aliasing across all Swing components
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Set global Swing defaults for a consistent premium look
        String fontFamily = UIConstants.FONT_FAMILY;
        UIManager.put("Button.font",       new Font(fontFamily, Font.BOLD, 13));
        UIManager.put("Label.font",        new Font(fontFamily, Font.PLAIN, 14));
        UIManager.put("TextField.font",    new Font(fontFamily, Font.PLAIN, 14));
        UIManager.put("Table.font",        new Font(fontFamily, Font.PLAIN, 13));
        UIManager.put("TableHeader.font",  new Font(fontFamily, Font.BOLD, 13));
        UIManager.put("ComboBox.font",     new Font(fontFamily, Font.PLAIN, 14));
        UIManager.put("TextArea.font",     new Font(fontFamily, Font.PLAIN, 13));
        UIManager.put("OptionPane.messageFont", new Font(fontFamily, Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont",  new Font(fontFamily, Font.BOLD, 13));

        // Custom tooltip styling
        UIManager.put("ToolTip.font",       new Font(fontFamily, Font.PLAIN, 12));
        UIManager.put("ToolTip.background", new Color(33, 37, 41));
        UIManager.put("ToolTip.foreground", Color.WHITE);

        // ── Launch the GUI ────────────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            ReportExporter.log("Application started.");
            new LoginFrame();
        });
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  CONSOLE TEST METHODS (can be invoked for standalone testing)
    // ──────────────────────────────────────────────────────────────────────────

    // ── OOP DEMONSTRATION (no DB required) ────────────────────────────────────
    static void testOOP() {
        System.out.println("\n--- OOP: User Hierarchy Demo ---");

        // Polymorphism: User reference holds different subclass objects
        User[] users = {
            new Admin(1, "admin1",       "admin123"),
            new Judge(2, "judge_sharma", "judge123", "Criminal"),
            new Clerk(3, "clerk_raj",    "clerk123", "1")
        };

        for (User u : users) {
            u.displayDashboard();   // polymorphic method dispatch
            System.out.println("  Role: " + u.getRole()
                    + " | Auth: " + u.authenticate(u.getUsername(), "wrongpass"));
            System.out.println();
        }

        // Case creation with encapsulation
        System.out.println("--- Case Entity Demo ---");
        Case c = new Case();
        c.setCaseId(101);
        c.setCaseNumber("CRM/2021/001");
        c.setTitle("State vs Ramesh Kumar");
        c.setCaseType("Criminal");
        c.setPetitioner("State of Maharashtra");
        c.setRespondent("Ramesh Kumar");
        c.setFilingDate(LocalDate.of(2021, 3, 15));
        c.setStatus(Case.STATUS_IN_PROGRESS);
        System.out.println(c);
        System.out.println("Days pending: " + c.getDaysPending());

        // CaseHistory audit
        CaseHistory history = new CaseHistory(
                101, 2, "Pending", "In Progress", "First hearing completed.");
        System.out.println(history);

        // Custom exception demo
        System.out.println("\n--- Exception Handling Demo ---");
        try {
            Case bad = new Case();
            bad.setTitle("");                        // invalid — empty title
            bad.setFilingDate(LocalDate.now().plusDays(5)); // invalid — future date
            bad.setStatus("InvalidStatus");          // invalid status

            CaseService svc = new CaseService();
            svc.addCase(bad);
        } catch (InvalidCaseException e) {
            System.out.println("Caught: " + e);
        }

        // File handling demo
        System.out.println("\n--- File Handling Demo ---");
        try {
            java.util.List<Case> sampleCases = java.util.List.of(c);
            ReportExporter.exportToCSV(sampleCases, "reports/demo_export.csv");
            ReportExporter.log("Demo run completed.");
            System.out.println("CSV exported. Check reports/ folder.");
        } catch (Exception e) {
            System.err.println("File export error: " + e.getMessage());
        }

        System.out.println("\n--- All OOP tests completed. ---");
    }

    // ── DATABASE INTEGRATION TEST ─────────────────────────────────────────────
    static void testWithDatabase() {
        UserService  userService  = new UserService();
        CaseService  caseService  = new CaseService();

        System.out.println("\n--- Register Users ---");
        userService.registerUser("admin1",       "admin123", "Admin", null);
        userService.registerUser("judge_sharma", "judge123", "Judge","Criminal");
        userService.registerUser("clerk_raj",    "clerk123", "Clerk","1");

        System.out.println("\n--- Login Test ---");
        User user = userService.login("judge_sharma", "judge123");
        if (user != null) user.displayDashboard();

        System.out.println("\n--- Add Cases ---");
        try {
            Case c1 = new Case();
            c1.setCaseNumber("CRM/2021/001");
            c1.setTitle("State vs Ramesh Kumar");
            c1.setCaseType("Criminal");
            c1.setPetitioner("State of MH");
            c1.setRespondent("Ramesh Kumar");
            c1.setFilingDate(LocalDate.of(2021, 3, 15));
            c1.setStatus(Case.STATUS_PENDING);
            caseService.addCase(c1);
            System.out.println("Added: " + c1);

            // Assign a judge
            caseService.assignJudge(c1.getCaseId(), 2);
            System.out.println("Judge assigned to case #" + c1.getCaseId());

            // Update status
            caseService.updateCaseStatus(c1.getCaseId(),
                    Case.STATUS_IN_PROGRESS, 3);
            System.out.println("Status updated.");

        } catch (InvalidCaseException e) {
            System.err.println("Case error: " + e);
        }

        System.out.println("\n--- Pending Cases ---");
        caseService.getPendingCases().forEach(System.out::println);

        System.out.println("\n--- Analytics ---");
        System.out.println("Total Backlog    : " + caseService.getTotalBacklog());
        System.out.printf("Avg Resolution   : %.1f days%n",
                caseService.getAvgResolutionDays());

        System.out.println("\n--- Long Pending (>365 days) ---");
        caseService.getLongPendingCases(365).forEach(System.out::println);

        System.out.println("\n--- Multithreaded Report Generation ---");
        ReportGeneratorThread reporter = new ReportGeneratorThread(
                caseService, "reports/full_report_" + LocalDate.now() + ".csv");
        reporter.start();

        System.out.println("\n--- Concurrent Updates ---");
        ReportGeneratorThread.simulateConcurrentUpdates(new CaseDAO());

        System.out.println("\n--- All tests completed. ---");
        DBConnection.closeConnection();
    }
}
