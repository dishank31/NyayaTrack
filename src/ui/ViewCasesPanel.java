package ui;
 
import model.Case;
import model.User;
import service.CaseService;
import exception.InvalidCaseException;
 
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
 
/**
 * Panel displaying all cases in a sortable JTable.
 * Supports search/filter, status update, and judge assignment.
 */
public class ViewCasesPanel extends JPanel {
 
    private final User        currentUser;
    private final CaseService caseService;
 
    private JTable         table;
    private DefaultTableModel tableModel;
    private JTextField     searchField;
    private JLabel         countLabel;
 
    private static final String[] COLUMNS = {
        "ID", "Case No.", "Title", "Type", "Petitioner", "Status", "Days Pending"
    };
 
    public ViewCasesPanel(User user, CaseService caseService) {
        this.currentUser = user;
        this.caseService = caseService;
        initUI();
        loadCases();
    }
 
    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(245, 249, 255));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
 
        // ── Top bar ───────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(8, 0));
        topBar.setOpaque(false);
 
        JLabel heading = new JLabel("Case Registry");
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        heading.setForeground(new Color(26, 60, 110));
 
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField(18);
        JButton searchBtn = new JButton("Search");
        JButton refreshBtn = new JButton("Refresh");
        countLabel  = new JLabel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        searchPanel.add(countLabel);
 
        topBar.add(heading,     BorderLayout.WEST);
        topBar.add(searchPanel, BorderLayout.EAST);
 
        // ── Table ─────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(26, 60, 110));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 220, 255));
        table.setAutoCreateRowSorter(true);
 
        // Color-code rows by status
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String status = (String) t.getValueAt(row, 5);
                    setBackground(switch (status) {
                        case "Resolved"    -> new Color(220, 255, 220);
                        case "Dismissed"   -> new Color(240, 240, 240);
                        case "In Progress" -> new Color(255, 248, 220);
                        default            -> Color.WHITE;
                    });
                }
                return this;
            }
        });
 
        // ── Bottom action bar ─────────────────────────────────────────────────
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bottomBar.setOpaque(false);
 
        JButton updateBtn = new JButton("Update Status");
        JButton assignBtn = new JButton("Assign Judge");
 
        updateBtn.addActionListener(e -> updateSelectedStatus());
        assignBtn.addActionListener(e -> assignJudgeToSelected());
        searchBtn.addActionListener(e -> filterTable(searchField.getText()));
        refreshBtn.addActionListener(e -> loadCases());
 
        // Only show update/assign for Admin and Clerk
        if (!currentUser.getRole().equals("Judge")) {
            bottomBar.add(updateBtn);
            bottomBar.add(assignBtn);
        }
 
        add(topBar,                 BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomBar,              BorderLayout.SOUTH);
    }
 
    /** Reload all cases from service into table model */
    public void loadCases() {
        tableModel.setRowCount(0);
        List<Case> cases = currentUser.getRole().equals("Judge")
                ? caseService.getPendingCases()
                : caseService.getAllCases();
 
        for (Case c : cases) {
            tableModel.addRow(new Object[]{
                c.getCaseId(), c.getCaseNumber(), c.getTitle(),
                c.getCaseType(), c.getPetitioner(),
                c.getStatus(), c.getDaysPending()
            });
        }
        countLabel.setText("Total: " + cases.size());
    }
 
    private void filterTable(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setRowFilter(query.isBlank() ? null
                : RowFilter.regexFilter("(?i)" + query));
    }
 
    private void updateSelectedStatus() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this,
                "Select a case first."); return; }
 
        int caseId = (int) tableModel.getValueAt(
                table.convertRowIndexToModel(row), 0);
 
        String[] options = {"Pending", "In Progress", "Resolved", "Dismissed"};
        String choice = (String) JOptionPane.showInputDialog(this,
                "Select new status for Case #" + caseId,
                "Update Status", JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
 
        if (choice != null) {
            try {
                boolean ok = caseService.updateCaseStatus(
                        caseId, choice, currentUser.getUserId());
                JOptionPane.showMessageDialog(this,
                        ok ? "Status updated!" : "Update failed.");
                loadCases();
            } catch (InvalidCaseException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
 
    private void assignJudgeToSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this,
                "Select a case first."); return; }
 
        int caseId = (int) tableModel.getValueAt(
                table.convertRowIndexToModel(row), 0);
 
        String input = JOptionPane.showInputDialog(this,
                "Enter Judge User ID to assign to Case #" + caseId + ":");
 
        if (input != null && !input.isBlank()) {
            try {
                int judgeId = Integer.parseInt(input.trim());
                boolean ok  = caseService.assignJudge(caseId, judgeId);
                JOptionPane.showMessageDialog(this,
                        ok ? "Judge assigned successfully!" : "Assignment failed.");
                loadCases();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Judge ID.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidCaseException ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
