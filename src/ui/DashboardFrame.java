package ui;
 
import model.User;
import service.CaseService;
import util.ReportExporter;
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
 
/**
 * Main application window shown after successful login.
 * Contains a navigation menu and card-based content panel.
 */
public class DashboardFrame extends JFrame {
 
    private final User        currentUser;
    private final CaseService caseService = new CaseService();
 
    private JPanel cardPanel;
    private CardLayout cardLayout;
 
    public DashboardFrame(User user) {
        super("Judicial System — " + user.getRole() + ": " + user.getUsername());
        this.currentUser = user;
        user.displayDashboard();
        initUI();
    }
 
    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
 
        // ── Left navigation sidebar ───────────────────────────────────────────
        JPanel sidebar = new JPanel(new GridLayout(0, 1, 0, 4));
        sidebar.setBackground(new Color(26, 60, 110));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 8));
        sidebar.setPreferredSize(new Dimension(180, 0));
 
        // User info label
        JLabel userInfo = new JLabel("<html><center>" + currentUser.getUsername()
                + "<br><small>(" + currentUser.getRole() + ")</small></center></html>");
        userInfo.setForeground(Color.WHITE);
        userInfo.setHorizontalAlignment(SwingConstants.CENTER);
        userInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        sidebar.add(userInfo);
 
        // Navigation buttons
        String[] navItems = {"Dashboard", "View Cases", "Add Case", "Analytics", "Logout"};
        for (String item : navItems) {
            JButton btn = createNavButton(item);
            sidebar.add(btn);
        }
 
        // ── Right content panel (CardLayout) ──────────────────────────────────
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.add(createDashboardHome(), "Dashboard");
        cardPanel.add(new ViewCasesPanel(currentUser, caseService), "View Cases");
        cardPanel.add(new AddCasePanel(currentUser, caseService), "Add Case");
        cardPanel.add(createAnalyticsPanel(), "Analytics");
 
        add(sidebar,    BorderLayout.WEST);
        add(cardPanel,  BorderLayout.CENTER);
        setVisible(true);
    }
 
    private JButton createNavButton(String label) {
        JButton btn = new JButton(label);
        btn.setBackground(new Color(46, 95, 170));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        btn.addActionListener(e -> {
            if ("Logout".equals(label)) {
                ReportExporter.log("User logged out: " + currentUser.getUsername());
                dispose();
                new LoginFrame();
            } else {
                cardLayout.show(cardPanel, label);
            }
        });
        return btn;
    }
 
    // ── Dashboard home panel with quick stats ─────────────────────────────────
    private JPanel createDashboardHome() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 249, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
 
        JLabel heading = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(new Color(26, 60, 110));
 
        // Stats row
        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
 
        int pending  = caseService.getTotalBacklog();
        int total    = caseService.getAllCases().size();
        double avg   = caseService.getAvgResolutionDays();
 
        stats.add(statCard("Total Cases",         String.valueOf(total),   new Color(46, 95, 170)));
        stats.add(statCard("Pending / Backlog",   String.valueOf(pending), new Color(200, 60, 60)));
        stats.add(statCard("Avg. Resolution Days",String.format("%.1f", avg), new Color(26, 107, 60)));
 
        panel.add(heading, BorderLayout.NORTH);
        panel.add(stats,   BorderLayout.CENTER);
        return panel;
    }
 
    private JPanel statCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
 
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Arial", Font.BOLD, 36));
        val.setForeground(color);
 
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 13));
        lbl.setForeground(Color.GRAY);
 
        card.add(val, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }
 
    // ── Analytics panel ───────────────────────────────────────────────────────
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 249, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
 
        JLabel heading = new JLabel("Backlog Analytics");
        heading.setFont(new Font("Arial", Font.BOLD, 20));
        heading.setForeground(new Color(26, 60, 110));
 
        // Long-pending cases (> 365 days)
        var longPending = caseService.getLongPendingCases(365);
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Courier New", Font.PLAIN, 13));
        area.append("Cases pending for more than 1 year:\n");
        area.append(String.format("%-6s %-30s %-12s %s\n",
                "ID", "Title", "Type", "Days Pending"));
        area.append("─".repeat(60) + "\n");
        for (var c : longPending) {
            area.append(String.format("%-6d %-30s %-12s %d\n",
                    c.getCaseId(), c.getTitle(), c.getCaseType(), c.getDaysPending()));
        }
        if (longPending.isEmpty()) area.append("No long-pending cases found.\n");
 
        JButton exportBtn = new JButton("Export Pending Report (CSV)");
        exportBtn.addActionListener(e -> {
            try {
                String path = "reports/pending_" +
                        java.time.LocalDate.now() + ".csv";
                ReportExporter.exportToCSV(caseService.getPendingCases(), path);
                JOptionPane.showMessageDialog(this,
                        "Report exported to: " + path, "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Export failed: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
 
        panel.add(heading,              BorderLayout.NORTH);
        panel.add(new JScrollPane(area),BorderLayout.CENTER);
        panel.add(exportBtn,            BorderLayout.SOUTH);
        return panel;
    }
}
