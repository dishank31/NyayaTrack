package thread;
 
import dao.CaseDAO;
import service.CaseService;
import model.Case;
import util.ReportExporter;
 
import java.util.List;
import java.util.concurrent.*;
 
/**
 * Demonstrates multithreading for:
 * 1. Background report generation (Thread subclass)
 * 2. Concurrent case updates (Runnable + ExecutorService)
 */
public class ReportGeneratorThread extends Thread {
 
    private final CaseService caseService;
    private final String      outputPath;
    private volatile boolean  completed = false;
 
    public ReportGeneratorThread(CaseService caseService, String outputPath) {
        this.caseService = caseService;
        this.outputPath  = outputPath;
        setName("ReportGeneratorThread");
        setDaemon(true); // won't block JVM shutdown
    }
 
    // ── Thread 1: Background report generation ────────────────────────────────
    @Override
    public void run() {
        System.out.println("[" + getName() + "] Starting report generation...");
        try {
            List<Case> cases = caseService.getAllCases();
            ReportExporter.exportToCSV(cases, outputPath);
            completed = true;
            System.out.println("[" + getName() + "] Report generated at: " + outputPath);
        } catch (Exception e) {
            System.err.println("[" + getName() + "] Report generation failed: "
                    + e.getMessage());
        }
    }
 
    public boolean isCompleted() { return completed; }
 
    // =========================================================================
    // DEMO: Concurrent case status updates using Runnable + ExecutorService
    // =========================================================================
    public static void simulateConcurrentUpdates(CaseDAO dao) {
        System.out.println("\n[Thread Demo] Simulating concurrent case updates...");
 
        // Thread pool with 3 workers
        ExecutorService executor = Executors.newFixedThreadPool(3);
 
        // Each Runnable simulates a clerk updating a different case
        int[] caseIds    = {1, 2, 3, 4, 5};
        String[] statuses = {"In Progress", "Resolved", "Pending",
                             "In Progress", "Dismissed"};
 
        for (int i = 0; i < caseIds.length; i++) {
            final int    cid    = caseIds[i];
            final String status = statuses[i];
 
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Simulate processing delay
                        Thread.sleep((long)(Math.random() * 500));
                        synchronized (dao) { // thread-safe DB write
                            dao.updateCaseStatus(cid, status, 1);
                        }
                        System.out.printf("[%s] Case #%d updated to '%s'%n",
                                Thread.currentThread().getName(), cid, status);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        System.err.println("[Thread] Update failed: " + e.getMessage());
                    }
                }
            });
        }
 
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS))
                executor.shutdownNow();
            System.out.println("[Thread Demo] All concurrent updates completed.\n");
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
