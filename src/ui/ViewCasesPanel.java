package ui;

import model.Case;
import model.User;
import service.CaseService;
import exception.InvalidCaseException;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Premium panel displaying all cases in a styled, sortable JTable.
 * Features zebra striping, status pill badges, search/filter,
 * status update, and judge assignment — all with the unified design system.
 */
public class ViewCasesPanel extends JPanel {

    private final User        currentUser;
    private final CaseService caseService;

    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        searchField;
    private JLabel            countLabel;

    private static final String[] COLUMNS = {
        "ID", "Case No.", "Title", "Type", "Petitioner", "Respondent", "Status", "Days Pending"
    };

    public ViewCasesPanel(User user, CaseService caseService) {
        this.currentUser = user;
        this.caseService = caseService;
        initUI();
        loadCases();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setOpaque(false);

        // ── TOP BAR ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        String headingText = switch (currentUser.getRole()) {
            case "Judge" -> "📋  My Assigned Cases";
            case "Clerk" -> "📋  Case Registry — Records";
            case "Admin" -> "📋  All Cases — Administrator View";
            default      -> "📋  Case Registry";
        };
        JLabel heading = new JLabel(headingText);
        heading.setFont(UIConstants.FONT_HEADING);
        heading.setForeground(UIConstants.PRIMARY_DARK);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);

        searchField = new JTextField(18) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0,
                        getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D gp = (Graphics2D) g.create();
                    UIConstants.applyRenderingHints(gp);
                    gp.setColor(UIConstants.TEXT_MUTED);
                    gp.setFont(UIConstants.FONT_BODY);
                    gp.drawString("🔍 Search cases...", getInsets().left + 2,
                            getHeight() / 2 + 5);
                    gp.dispose();
                }
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);
                g2.setColor(isFocusOwner() ? UIConstants.PRIMARY_LIGHT : UIConstants.BORDER_MEDIUM);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
                g2.draw(new RoundRectangle2D.Double(0, 0,
                        getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };
        searchField.setOpaque(false);
        searchField.setBackground(UIConstants.BG_INPUT);
        searchField.setFont(UIConstants.FONT_BODY);
        searchField.setForeground(UIConstants.TEXT_PRIMARY);
        searchField.setCaretColor(UIConstants.PRIMARY_LIGHT);
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        searchField.setPreferredSize(new Dimension(220, 36));
        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { searchField.repaint(); }
            @Override public void focusLost(FocusEvent e)   { searchField.repaint(); }
        });

        RoundedButton searchBtn  = RoundedButton.primary("Search");
        RoundedButton refreshBtn = RoundedButton.ghost("↻ Refresh");

        countLabel = new JLabel();
        countLabel.setFont(UIConstants.FONT_SMALL_BOLD);
        countLabel.setForeground(UIConstants.TEXT_SECONDARY);

        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        searchPanel.add(Box.createHorizontalStrut(8));
        searchPanel.add(countLabel);

        topBar.add(heading,      BorderLayout.WEST);
        topBar.add(searchPanel,  BorderLayout.EAST);

        // ── TABLE ────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setFont(UIConstants.FONT_BODY);
        table.setGridColor(UIConstants.BORDER_LIGHT);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(46, 95, 170, 30));
        table.setSelectionForeground(UIConstants.TEXT_PRIMARY);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(110);  // Case No
        table.getColumnModel().getColumn(2).setPreferredWidth(200);  // Title
        table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Type
        table.getColumnModel().getColumn(4).setPreferredWidth(120);  // Petitioner
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // Respondent
        table.getColumnModel().getColumn(6).setPreferredWidth(110);  // Status
        table.getColumnModel().getColumn(7).setPreferredWidth(90);   // Days

        // ── Custom header renderer ───────────────────────────────────────────
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
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
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });
        header.setPreferredSize(new Dimension(0, 40));

        // ── Zebra striping + status pill rendering ───────────────────────────
        // Default cell renderer for non-status columns
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setFont(UIConstants.FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!sel) {
                    setBackground(row % 2 == 0 ? UIConstants.BG_CARD
                            : new Color(245, 248, 255));
                }
                setForeground(UIConstants.TEXT_PRIMARY);
                return this;
            }
        });

        // Status column gets the pill renderer
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusPillRenderer());

        // Table in scrollpane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(UIConstants.BG_CARD);

        // ── BOTTOM ACTION BAR ────────────────────────────────────────────────
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        bottomBar.setOpaque(false);

        RoundedButton updateBtn = RoundedButton.accent("Update Status");
        RoundedButton assignBtn = RoundedButton.primary("Assign Judge");
        RoundedButton deleteBtn = RoundedButton.danger("Delete Case");

        updateBtn.addActionListener(e -> updateSelectedStatus());
        assignBtn.addActionListener(e -> assignJudgeToSelected());
        deleteBtn.addActionListener(e -> deleteSelectedCase());
        searchBtn.addActionListener(e -> filterTable(searchField.getText()));
        refreshBtn.addActionListener(e -> loadCases());

        // Live search as user types
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                filterTable(searchField.getText());
            }
        });

        // Show action buttons based on role
        switch (currentUser.getRole()) {
            case "Admin" -> {
                bottomBar.add(updateBtn);
                bottomBar.add(assignBtn);
                bottomBar.add(deleteBtn);
            }
            case "Clerk" -> {
                bottomBar.add(updateBtn);
                bottomBar.add(assignBtn);
            }
            case "Judge" -> {
                // Judge can only update status of their assigned cases
                bottomBar.add(updateBtn);
            }
        }

        add(topBar,      BorderLayout.NORTH);
        add(scrollPane,  BorderLayout.CENTER);
        add(bottomBar,   BorderLayout.SOUTH);
    }

    /**
     * Reload cases from service into table model.
     * For Judge: shows only cases assigned to this judge.
     * For Admin/Clerk: shows all cases.
     */
    public void loadCases() {
        SwingWorker<List<Case>, Void> worker = new SwingWorker<>() {
            @Override protected List<Case> doInBackground() {
                return caseService.getAllCases();
            }
            @Override protected void done() {
                try {
                    List<Case> allCases = get();
                    tableModel.setRowCount(0);
                    int count = 0;
                    for (Case c : allCases) {
                        // Judge filter: only show cases assigned to this judge
                        if ("Judge".equals(currentUser.getRole())
                                && c.getAssignedJudgeId() != currentUser.getUserId()) {
                            continue;
                        }
                        tableModel.addRow(new Object[]{
                            c.getCaseId(), c.getCaseNumber(), c.getTitle(),
                            c.getCaseType(), c.getPetitioner(), c.getRespondent(),
                            c.getStatus(), c.getDaysPending()
                        });
                        count++;
                    }
                    String prefix = "Judge".equals(currentUser.getRole())
                            ? "Assigned: " : "Showing ";
                    countLabel.setText(prefix + count + " cases");
                } catch (Exception ex) {
                    countLabel.setText("Error loading cases");
                }
            }
        };
        worker.execute();
    }

    private void filterTable(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setRowFilter(query.isBlank() ? null
                : RowFilter.regexFilter("(?i)" + query));
        countLabel.setText("Showing " + table.getRowCount() + " cases");
    }

    private void updateSelectedStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a case from the table first.");
            return;
        }

        int caseId = (int) tableModel.getValueAt(
                table.convertRowIndexToModel(row), 0);

        String[] options = {"Pending", "In Progress", "Resolved", "Dismissed"};
        String choice = (String) JOptionPane.showInputDialog(this,
                "Select new status for Case #" + caseId + ":",
                "Update Case Status", JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice != null) {
            try {
                boolean ok = caseService.updateCaseStatus(
                        caseId, choice, currentUser.getUserId());
                if (ok) {
                    showSuccess("Status updated to '" + choice + "' successfully!");
                } else {
                    showWarning("Update failed. Please try again.");
                }
                loadCases();
            } catch (InvalidCaseException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void assignJudgeToSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a case from the table first.");
            return;
        }

        int caseId = (int) tableModel.getValueAt(
                table.convertRowIndexToModel(row), 0);

        String input = JOptionPane.showInputDialog(this,
                "Enter Judge User ID to assign to Case #" + caseId + ":",
                "Assign Judge", JOptionPane.QUESTION_MESSAGE);

        if (input != null && !input.isBlank()) {
            try {
                int judgeId = Integer.parseInt(input.trim());
                boolean ok  = caseService.assignJudge(caseId, judgeId);
                if (ok) {
                    showSuccess("Judge (ID: " + judgeId + ") assigned successfully!");
                } else {
                    showWarning("Assignment failed.");
                }
                loadCases();
            } catch (NumberFormatException ex) {
                showError("Invalid Judge ID. Please enter a valid number.");
            } catch (InvalidCaseException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void deleteSelectedCase() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showWarning("Please select a case to delete.");
            return;
        }

        int caseId = (int) tableModel.getValueAt(
                table.convertRowIndexToModel(row), 0);
        String title = (String) tableModel.getValueAt(
                table.convertRowIndexToModel(row), 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete:\n\nCase #" + caseId
                + " — " + title + "?\n\nThis action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = new dao.CaseDAO().deleteCase(caseId);
                if (ok) {
                    showSuccess("Case #" + caseId + " deleted.");
                    util.ReportExporter.log("Case deleted: #" + caseId
                            + " by " + currentUser.getUsername());
                } else {
                    showWarning("Deletion failed.");
                }
                loadCases();
            } catch (Exception ex) {
                showError("Error: " + ex.getMessage());
            }
        }
    }

    // ── Dialog helpers ──────────────────────────────────────────────────────
    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning",
                JOptionPane.WARNING_MESSAGE);
    }
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
