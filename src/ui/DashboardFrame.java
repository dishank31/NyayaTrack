package ui;

import model.User;
import service.CaseService;
import model.Case;
import util.ReportExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * Main application window shown after successful login.
 * Features a persistent left sidebar, top header bar, and a
 * card-based content area that transitions between views.
 *
 * The entire UI is ROLE-SPECIFIC:
 *   - Admin:  Full access — Dashboard, View All Cases, Add Case, Analytics, User Mgmt
 *   - Judge:  View assigned cases, Update case status, Analytics (own cases)
 *   - Clerk:  Register new cases, View all cases, Basic analytics
 */
public class DashboardFrame extends JFrame {

    private final User        currentUser;
    private final CaseService caseService = new CaseService();
    private final String      userRole;

    private JPanel     cardPanel;
    private CardLayout cardLayout;
    private JPanel     sidebar;
    private JButton    activeNavButton = null;

    // Navigation item keys
    private static final String NAV_DASHBOARD  = "Dashboard";
    private static final String NAV_VIEW_CASES = "View Cases";
    private static final String NAV_MY_CASES   = "My Cases";
    private static final String NAV_ADD_CASE   = "Add Case";
    private static final String NAV_ANALYTICS  = "Analytics";
    private static final String NAV_USERS      = "Manage Users";
    private static final String NAV_LOGOUT     = "Logout";
    private static final String NAV_DELETE_ACCT = "Delete Account";

    public DashboardFrame(User user) {
        super("Nyaya Track — " + user.getRole() + ": " + user.getUsername());
        this.currentUser = user;
        this.userRole    = user.getRole();
        user.displayDashboard();
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 740);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 600));

        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(UIConstants.BG_MAIN);

        sidebar = createSidebar();
        JPanel header = createHeader();

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(UIConstants.BG_MAIN);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // ── Add panels BASED ON ROLE ─────────────────────────────────────────
        cardPanel.add(createDashboardHome(), NAV_DASHBOARD);

        switch (userRole) {
            case "Admin":
                cardPanel.add(new ViewCasesPanel(currentUser, caseService), NAV_VIEW_CASES);
                cardPanel.add(new AddCasePanel(currentUser, caseService), NAV_ADD_CASE);
                cardPanel.add(createAnalyticsPanel(), NAV_ANALYTICS);
                cardPanel.add(createUserManagementPanel(), NAV_USERS);
                break;
            case "Judge":
                cardPanel.add(new ViewCasesPanel(currentUser, caseService), NAV_MY_CASES);
                cardPanel.add(createAnalyticsPanel(), NAV_ANALYTICS);
                break;
            case "Clerk":
                cardPanel.add(new ViewCasesPanel(currentUser, caseService), NAV_VIEW_CASES);
                cardPanel.add(new AddCasePanel(currentUser, caseService), NAV_ADD_CASE);
                cardPanel.add(createAnalyticsPanel(), NAV_ANALYTICS);
                break;
        }

        rootPanel.add(sidebar,   BorderLayout.WEST);
        rootPanel.add(header,    BorderLayout.NORTH);
        rootPanel.add(cardPanel, BorderLayout.CENTER);

        setContentPane(rootPanel);
        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  SIDEBAR — Role-specific navigation
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel createSidebar() {
        JPanel sb = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);
                GradientPaint gp = new GradientPaint(0, 0, UIConstants.PRIMARY_DARK,
                        0, getHeight(), new Color(10, 25, 50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        sb.setOpaque(false);
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // ── Brand logo area ──────────────────────────────────────────────────
        JPanel brandPanel = new JPanel() {
            @Override public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);
                g2.setColor(UIConstants.ACCENT_GOLD);
                g2.fillRect(20, getHeight() - 1, getWidth() - 40, 1);
                g2.dispose();
            }
        };
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(BorderFactory.createEmptyBorder(22, 20, 16, 20));
        brandPanel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 90));

        JLabel brandName = new JLabel("Nyaya Track");
        brandName.setFont(UIConstants.FONT_HEADING);
        brandName.setForeground(UIConstants.ACCENT_GOLD);
        brandName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandSub = new JLabel(getRoleSubtitle());
        brandSub.setFont(UIConstants.FONT_SMALL);
        brandSub.setForeground(UIConstants.TEXT_MUTED);
        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandPanel.add(brandName);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(brandSub);

        sb.add(brandPanel);
        sb.add(Box.createVerticalStrut(10));

        // ── ROLE-SPECIFIC navigation buttons ─────────────────────────────────
        List<String> navItems = new ArrayList<>();
        navItems.add(NAV_DASHBOARD);

        switch (userRole) {
            case "Admin":
                navItems.add(NAV_VIEW_CASES);
                navItems.add(NAV_ADD_CASE);
                navItems.add(NAV_ANALYTICS);
                navItems.add(NAV_USERS);
                break;
            case "Judge":
                navItems.add(NAV_MY_CASES);
                navItems.add(NAV_ANALYTICS);
                break;
            case "Clerk":
                navItems.add(NAV_VIEW_CASES);
                navItems.add(NAV_ADD_CASE);
                navItems.add(NAV_ANALYTICS);
                break;
        }

        for (String item : navItems) {
            JButton btn = createNavButton(item, item);
            sb.add(btn);
            sb.add(Box.createVerticalStrut(2));
            if (item.equals(NAV_DASHBOARD)) {
                activeNavButton = btn;
                btn.setBackground(new Color(255, 255, 255, 15));
            }
        }

        sb.add(Box.createVerticalGlue());

        // ── User card at bottom ──────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 30));
        sep.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH - 40, 1));
        sb.add(sep);

        JPanel userPanel = new JPanel();
        userPanel.setOpaque(false);
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        userPanel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 80));

        JLabel userIcon = new JLabel(currentUser.getUsername());
        userIcon.setFont(UIConstants.FONT_BODY_BOLD);
        userIcon.setForeground(UIConstants.TEXT_ON_DARK);
        userIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel(currentUser.getRole());
        roleLabel.setFont(UIConstants.FONT_SMALL);
        roleLabel.setForeground(UIConstants.TEXT_MUTED);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userPanel.add(userIcon);
        userPanel.add(Box.createVerticalStrut(2));
        userPanel.add(roleLabel);
        sb.add(userPanel);

        JButton logoutBtn = createNavButton(NAV_LOGOUT, NAV_LOGOUT);
        logoutBtn.setForeground(new Color(255, 120, 120));
        sb.add(logoutBtn);
        sb.add(Box.createVerticalStrut(4));

        JButton deleteBtn = createNavButton(NAV_DELETE_ACCT, NAV_DELETE_ACCT);
        deleteBtn.setForeground(new Color(255, 80, 80));
        sb.add(deleteBtn);
        sb.add(Box.createVerticalStrut(12));

        return sb;
    }

    /** Returns a role-specific subtitle for the sidebar brand area. */
    private String getRoleSubtitle() {
        switch (userRole) {
            case "Admin": return "System Administrator";
            case "Judge": return "Judicial Officer Panel";
            case "Clerk": return "Court Clerk Panel";
            default:      return "Case Monitoring System";
        }
    }

    private JButton createNavButton(String label, String command) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);

                if (getModel().isRollover() || this == activeNavButton) {
                    g2.setColor(new Color(255, 255, 255, 15));
                    g2.fillRoundRect(4, 2, getWidth() - 8, getHeight() - 4, 8, 8);

                    if (this == activeNavButton) {
                        g2.setColor(UIConstants.ACCENT_GOLD);
                        g2.fillRoundRect(0, 6, 4, getHeight() - 12, 2, 2);
                    }
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(UIConstants.TEXT_LIGHT);
        btn.setFont(UIConstants.FONT_NAV);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 16));
        btn.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 44));

        btn.addActionListener(e -> {
            if (NAV_LOGOUT.equals(command)) {
                ReportExporter.log("User logged out: " + currentUser.getUsername());
                dispose();
                new LoginFrame();
            } else if (NAV_DELETE_ACCT.equals(command)) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to permanently delete your account?\n"
                        + "Username: " + currentUser.getUsername() + "\n"
                        + "Role: " + currentUser.getRole() + "\n\n"
                        + "This action cannot be undone.",
                        "Delete Account", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean deleted = new service.UserService().deleteUser(currentUser.getUserId());
                    if (deleted) {
                        ReportExporter.log("Account deleted: " + currentUser.getUsername());
                        JOptionPane.showMessageDialog(this,
                                "Your account has been deleted.",
                                "Account Deleted", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        new LoginFrame();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Failed to delete account. Please try again.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                if (activeNavButton != null) activeNavButton.repaint();
                activeNavButton = btn;
                btn.repaint();
                cardLayout.show(cardPanel, command);
            }
        });
        return btn;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  HEADER BAR — role-specific title
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(UIConstants.BORDER_LIGHT);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, UIConstants.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        header.setOpaque(false);

        // Role-specific title
        String pageTitleText = "Judicial Case Monitoring Dashboard";
        if ("Admin".equals(userRole))      pageTitleText = "Administration Dashboard — Full System Access";
        else if ("Judge".equals(userRole)) pageTitleText = "Judicial Officer Dashboard — " + getJudgeSpecialization();
        else if ("Clerk".equals(userRole)) pageTitleText = "Court Clerk Dashboard — Case Registration & Records";

        JLabel pageTitle = new JLabel(pageTitleText);
        pageTitle.setFont(UIConstants.FONT_SUBHEADING);
        pageTitle.setForeground(UIConstants.PRIMARY_DARK);

        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightInfo.setOpaque(false);

        JLabel dateLabel = new JLabel(LocalDate.now().toString());
        dateLabel.setFont(UIConstants.FONT_SMALL);
        dateLabel.setForeground(UIConstants.TEXT_SECONDARY);

        JLabel roleBadge = createRoleBadge(userRole);
        rightInfo.add(dateLabel);
        rightInfo.add(roleBadge);

        header.add(pageTitle, BorderLayout.WEST);
        header.add(rightInfo, BorderLayout.EAST);

        pageTitle.setBorder(BorderFactory.createEmptyBorder(
                (UIConstants.HEADER_HEIGHT - 20) / 2, 0, 0, 0));

        return header;
    }

    /** Extracts specialization from Judge model if available. */
    private String getJudgeSpecialization() {
        if (currentUser instanceof model.Judge) {
            model.Judge j = (model.Judge) currentUser;
            return j.getSpecialization() + " Law";
        }
        return "General";
    }

    private JLabel createRoleBadge(String role) {
        Color bc = UIConstants.TEXT_SECONDARY;
        if ("Admin".equals(role))      bc = UIConstants.ACCENT_GOLD;
        else if ("Judge".equals(role)) bc = UIConstants.PRIMARY_LIGHT;
        else if ("Clerk".equals(role)) bc = UIConstants.SUCCESS;

        final Color badgeColor = bc;

        JLabel badge = new JLabel(role) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);
                g2.setColor(new Color(badgeColor.getRed(), badgeColor.getGreen(),
                        badgeColor.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(badgeColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(UIConstants.FONT_SMALL_BOLD);
        badge.setForeground(badgeColor);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        return badge;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  DASHBOARD HOME — role-specific cards & charts
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel createDashboardHome() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        // --- Welcome section with role-specific message ---
        String welcomeMsg = "Welcome back, " + currentUser.getUsername();
        if ("Admin".equals(userRole))      welcomeMsg = "Welcome, Administrator " + currentUser.getUsername();
        else if ("Judge".equals(userRole)) welcomeMsg = "Welcome, Hon. " + currentUser.getUsername();
        else if ("Clerk".equals(userRole)) welcomeMsg = "Welcome, " + currentUser.getUsername();

        JLabel heading = new JLabel(welcomeMsg);
        heading.setFont(UIConstants.FONT_TITLE);
        heading.setForeground(UIConstants.PRIMARY_DARK);

        String subText = "Here's your judicial system overview for today.";
        if ("Admin".equals(userRole))      subText = "Full system overview — manage cases, users, and generate reports.";
        else if ("Judge".equals(userRole)) subText = "Your assigned cases and courtroom analytics.";
        else if ("Clerk".equals(userRole)) subText = "Register cases, manage records, and generate basic reports.";

        JLabel sub = new JLabel(subText);
        sub.setFont(UIConstants.FONT_BODY);
        sub.setForeground(UIConstants.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JPanel topSection = new JPanel();
        topSection.setOpaque(false);
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.add(heading);
        topSection.add(Box.createVerticalStrut(4));
        topSection.add(sub);

        // --- Stats row (role-specific) ---
        JPanel statsRow;
        List<Case> casesToShow;

        if ("Judge".equals(userRole)) {
            // Judge sees ONLY assigned cases
            casesToShow = caseService.getPendingCases();
            // Try to get judge-specific data
            List<Case> allCases = caseService.getAllCases();
            int myTotal = 0, myPending = 0;
            for (Case c : allCases) {
                if (c.getAssignedJudgeId() == currentUser.getUserId()) {
                    myTotal++;
                    if ("Pending".equals(c.getStatus()) || "In Progress".equals(c.getStatus()))
                        myPending++;
                }
            }
            int myResolved = myTotal - myPending;

            statsRow = new JPanel(new GridLayout(1, 3, 16, 0));
            statsRow.setOpaque(false);
            statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
            statsRow.add(createStatCard("My Cases", String.valueOf(myTotal),
                    UIConstants.PRIMARY_LIGHT));
            statsRow.add(createStatCard("Pending", String.valueOf(myPending),
                    UIConstants.STATUS_PENDING));
            statsRow.add(createStatCard("Resolved", String.valueOf(myResolved),
                    UIConstants.SUCCESS));
        } else {
            // Admin and Clerk see global stats
            int pending  = caseService.getTotalBacklog();
            int total    = caseService.getAllCases().size();
            int resolved = total - pending;
            double avg   = caseService.getAvgResolutionDays();
            casesToShow  = caseService.getAllCases();

            statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
            statsRow.setOpaque(false);
            statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
            statsRow.add(createStatCard("Total Cases", String.valueOf(total),
                    UIConstants.PRIMARY_LIGHT));
            statsRow.add(createStatCard("Pending", String.valueOf(pending),
                    UIConstants.STATUS_PENDING));
            statsRow.add(createStatCard("Resolved", String.valueOf(resolved),
                    UIConstants.SUCCESS));
            statsRow.add(createStatCard("Avg. Resolution", String.format("%.1f d", avg),
                    UIConstants.INFO));
        }

        // --- Charts row ---
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);

        LinkedHashMap<String, Integer> typeData   = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> statusData = new LinkedHashMap<>();

        // For Judge, only count their assigned cases
        List<Case> chartCases = caseService.getAllCases();
        for (Case c : chartCases) {
            if ("Judge".equals(userRole) && c.getAssignedJudgeId() != currentUser.getUserId())
                continue;
            typeData.merge(c.getCaseType(), 1, Integer::sum);
            statusData.merge(c.getStatus(), 1, Integer::sum);
        }

        String barTitle = "Judge".equals(userRole) ? "My Cases by Type" : "Cases by Type";
        String pieTitle = "Judge".equals(userRole) ? "My Cases by Status" : "Cases by Status";

        RoundedPanel barCard = new RoundedPanel();
        barCard.setLayout(new BorderLayout());
        BarChartPanel barChart = new BarChartPanel(barTitle, typeData, null);
        barCard.add(barChart, BorderLayout.CENTER);

        RoundedPanel pieCard = new RoundedPanel();
        pieCard.setLayout(new BorderLayout());
        PieChartPanel pieChart = new PieChartPanel(pieTitle, statusData,
                new Color[]{ UIConstants.STATUS_PENDING, UIConstants.STATUS_IN_PROGRESS,
                             UIConstants.STATUS_RESOLVED, UIConstants.STATUS_DISMISSED });
        pieCard.add(pieChart, BorderLayout.CENTER);

        chartsRow.add(barCard);
        chartsRow.add(pieCard);

        // --- Role-specific quick actions ---
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        quickActions.setOpaque(false);
        quickActions.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        switch (userRole) {
            case "Admin":
                RoundedButton aAddBtn = RoundedButton.success("+ Register New Case");
                aAddBtn.addActionListener(e -> cardLayout.show(cardPanel, NAV_ADD_CASE));
                RoundedButton aViewBtn = RoundedButton.primary("View All Cases");
                aViewBtn.addActionListener(e -> cardLayout.show(cardPanel, NAV_VIEW_CASES));
                RoundedButton aUsersBtn = RoundedButton.accent("Manage Users");
                aUsersBtn.addActionListener(e -> cardLayout.show(cardPanel, NAV_USERS));
                quickActions.add(aAddBtn);
                quickActions.add(aViewBtn);
                quickActions.add(aUsersBtn);
                break;
            case "Judge":
                RoundedButton jViewBtn = RoundedButton.primary("View My Assigned Cases");
                jViewBtn.addActionListener(e -> cardLayout.show(cardPanel, NAV_MY_CASES));
                RoundedButton jAnalyticsBtn = RoundedButton.accent("View Analytics");
                jAnalyticsBtn.addActionListener(e -> cardLayout.show(cardPanel, NAV_ANALYTICS));
                quickActions.add(jViewBtn);
                quickActions.add(jAnalyticsBtn);
                break;
            case "Clerk":
                RoundedButton cAddBtn = RoundedButton.success("+ Register New Case");
                cAddBtn.addActionListener(e -> cardLayout.show(cardPanel, NAV_ADD_CASE));
                RoundedButton cViewBtn = RoundedButton.primary("View Case Registry");
                cViewBtn.addActionListener(e -> cardLayout.show(cardPanel, NAV_VIEW_CASES));
                quickActions.add(cAddBtn);
                quickActions.add(cViewBtn);
                break;
        }

        panel.add(topSection, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(statsRow,     BorderLayout.NORTH);
        center.add(chartsRow,    BorderLayout.CENTER);
        center.add(quickActions, BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private RoundedPanel createStatCard(String label, String value,
                                        Color accentColor) {
        RoundedPanel card = new RoundedPanel();
        card.setLayout(new BorderLayout(4, 4));

        JLabel lblTitle = new JLabel(label);
        lblTitle.setFont(UIConstants.FONT_SMALL_BOLD);
        lblTitle.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        topRow.setOpaque(false);
        topRow.add(lblTitle);

        JLabel valLabel = new JLabel(value);
        valLabel.setFont(UIConstants.FONT_STAT_VALUE);
        valLabel.setForeground(accentColor);

        final Color barColor = accentColor;
        JPanel accentBar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        accentBar.setOpaque(false);
        accentBar.setPreferredSize(new Dimension(0, 4));

        card.add(topRow,     BorderLayout.NORTH);
        card.add(valLabel,   BorderLayout.CENTER);
        card.add(accentBar,  BorderLayout.SOUTH);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  ANALYTICS PANEL — role-specific content
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        String headingText = "Judge".equals(userRole)
                ? "My Case Analytics" : "Backlog Analytics & Reports";
        JLabel heading = new JLabel(headingText);
        heading.setFont(UIConstants.FONT_HEADING);
        heading.setForeground(UIConstants.PRIMARY_DARK);

        // Long-pending cases
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(UIConstants.FONT_FAMILY_MONO, Font.PLAIN, 13));
        area.setBackground(UIConstants.BG_INPUT);
        area.setForeground(UIConstants.TEXT_PRIMARY);
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        if ("Judge".equals(userRole)) {
            // Judge sees only their assigned long-pending cases
            area.append("  YOUR ASSIGNED LONG-PENDING CASES (> 1 Year)\n");
            area.append("  " + "═".repeat(55) + "\n");
            area.append(String.format("  %-6s %-28s %-12s %s\n",
                    "ID", "Title", "Type", "Days Pending"));
            area.append("  " + "─".repeat(55) + "\n");

            List<Case> allCases = caseService.getAllCases();
            int count = 0;
            for (Case c : allCases) {
                if (c.getAssignedJudgeId() == currentUser.getUserId()
                        && c.getDaysPending() > 365) {
                    String title = c.getTitle();
                    if (title.length() > 26) title = title.substring(0, 23) + "...";
                    area.append(String.format("  %-6d %-28s %-12s %d\n",
                            c.getCaseId(), title, c.getCaseType(), c.getDaysPending()));
                    count++;
                }
            }
            if (count == 0) area.append("  No long-pending cases assigned to you. ✓\n");
            area.append("\n  Total: " + count + "\n");
        } else {
            // Admin & Clerk see system-wide analytics
            var longPending = caseService.getLongPendingCases(365);
            area.append("  SYSTEM-WIDE LONG-PENDING CASES (> 1 Year)\n");
            area.append("  " + "═".repeat(55) + "\n");
            area.append(String.format("  %-6s %-28s %-12s %s\n",
                    "ID", "Title", "Type", "Days Pending"));
            area.append("  " + "─".repeat(55) + "\n");

            for (var c : longPending) {
                String title = c.getTitle();
                if (title.length() > 26) title = title.substring(0, 23) + "...";
                area.append(String.format("  %-6d %-28s %-12s %d\n",
                        c.getCaseId(), title, c.getCaseType(), c.getDaysPending()));
            }
            if (longPending.isEmpty()) {
                area.append("  No long-pending cases found. ✓\n");
            }
            area.append("\n  Total long-pending: " + longPending.size() + "\n");
        }

        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));

        // Export buttons — Admin & Clerk only (Judge gets read-only analytics)
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        btnRow.setOpaque(false);

        if (!"Judge".equals(userRole)) {
            RoundedButton exportCsvBtn = RoundedButton.primary("Export Pending to CSV");
            RoundedButton exportTxtBtn = RoundedButton.accent("Export Summary (.txt)");

            exportCsvBtn.addActionListener(e -> {
                try {
                    String path = "reports/pending_" + LocalDate.now() + ".csv";
                    ReportExporter.exportToCSV(caseService.getPendingCases(), path);
                    JOptionPane.showMessageDialog(this,
                            "Report exported successfully!\n\nPath: " + path,
                            "Export Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Export failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            exportTxtBtn.addActionListener(e -> {
                try {
                    String path = "reports/summary_" + LocalDate.now() + ".txt";
                    ReportExporter.exportPendingSummary(caseService.getPendingCases(), path);
                    JOptionPane.showMessageDialog(this,
                            "Summary exported successfully!\n\nPath: " + path,
                            "Export Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Export failed: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            btnRow.add(exportCsvBtn);
            btnRow.add(exportTxtBtn);
        }

        panel.add(heading,     BorderLayout.NORTH);
        panel.add(scrollPane,  BorderLayout.CENTER);
        panel.add(btnRow,      BorderLayout.SOUTH);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  USER MANAGEMENT PANEL — Admin only
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        JLabel heading = new JLabel("User Management");
        heading.setFont(UIConstants.FONT_HEADING);
        heading.setForeground(UIConstants.PRIMARY_DARK);

        // ── User list table ──────────────────────────────────────────────────
        String[] cols = {"User ID", "Username", "Role", "Created At"};
        javax.swing.table.DefaultTableModel userModel =
                new javax.swing.table.DefaultTableModel(cols, 0) {
                    @Override public boolean isCellEditable(int r, int c) { return false; }
                };

        JTable userTable = new JTable(userModel);
        userTable.setRowHeight(32);
        userTable.setFont(UIConstants.FONT_BODY);
        userTable.setGridColor(UIConstants.BORDER_LIGHT);
        userTable.setShowHorizontalLines(true);
        userTable.setShowVerticalLines(false);
        userTable.setSelectionBackground(new Color(46, 95, 170, 30));
        userTable.setFillsViewportHeight(true);

        // Styled header
        userTable.getTableHeader().setDefaultRenderer(
                new javax.swing.table.DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable t, Object val,
                            boolean sel, boolean foc, int row, int col) {
                        JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                                t, val, sel, foc, row, col);
                        lbl.setBackground(UIConstants.PRIMARY_DARK);
                        lbl.setForeground(UIConstants.TEXT_ON_DARK);
                        lbl.setFont(UIConstants.FONT_SMALL_BOLD);
                        lbl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 2, 0, UIConstants.ACCENT_GOLD),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                        return lbl;
                    }
                });
        userTable.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Load users
        Runnable refreshUsers = () -> {
            userModel.setRowCount(0);
            var users = new service.UserService().getAllUsers();
            for (String[] u : users) {
                userModel.addRow(u);
            }
        };
        refreshUsers.run();

        JScrollPane tableScroll = new JScrollPane(userTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));

        // ── Action bar: Delete + Refresh ──────────────────────────────────
        RoundedPanel actionBar = new RoundedPanel();
        actionBar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        RoundedButton deleteUserBtn = RoundedButton.danger("Delete Selected User");
        RoundedButton refreshUsrBtn = RoundedButton.ghost("Refresh");

        deleteUserBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this,
                        "Please select a user from the table first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int    selUserId  = Integer.parseInt(userModel.getValueAt(row, 0).toString());
            String selUname   = userModel.getValueAt(row, 1).toString();
            String selRole    = userModel.getValueAt(row, 2).toString();

            // Prevent admin from deleting themselves
            if (selUserId == currentUser.getUserId()) {
                JOptionPane.showMessageDialog(this,
                        "You cannot delete your own account from here.\nUse 'Delete Account' in the sidebar.",
                        "Not Allowed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Permanently delete this user?\n\n"
                    + "Username : " + selUname + "\n"
                    + "Role     : " + selRole  + "\n"
                    + "User ID  : " + selUserId + "\n\n"
                    + "This action cannot be undone.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = new service.UserService().deleteUser(selUserId);
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "User '" + selUname + "' deleted successfully.",
                            "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    refreshUsers.run();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete user. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        refreshUsrBtn.addActionListener(e -> refreshUsers.run());

        actionBar.add(deleteUserBtn);
        actionBar.add(refreshUsrBtn);

        panel.add(heading,     BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);
        panel.add(actionBar,   BorderLayout.SOUTH);
        return panel;
    }
}
