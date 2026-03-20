package ui;
 
import model.Case;
import model.User;
import service.CaseService;
import exception.InvalidCaseException;
import util.ReportExporter;
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
 
/**
 * Form panel for registering a new judicial case.
 */
public class AddCasePanel extends JPanel {
 
    private final User        currentUser;
    private final CaseService caseService;
 
    private JTextField caseNumberField, titleField, petitionerField,
                       respondentField, descField;
    private JComboBox<String> typeCombo, statusCombo;
    private JTextField filingDateField;
    private JLabel     statusLabel;
 
    public AddCasePanel(User user, CaseService caseService) {
        this.currentUser = user;
        this.caseService = caseService;
        initUI();
    }
 
    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 249, 255));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
 
        // ── Form heading ───────────────────────────────────────────────────────
        JLabel heading = new JLabel("Register New Case");
        heading.setFont(new Font("Arial", Font.BOLD, 20));
        heading.setForeground(new Color(26, 60, 110));
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
 
        // ── Form fields ───────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 240)),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)));
 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
 
        // Row helper lambda
        int row = 0;
        caseNumberField = addFormRow(form, gbc, row++, "Case Number:", new JTextField(20));
        titleField      = addFormRow(form, gbc, row++, "Title:",       new JTextField(20));
        petitionerField = addFormRow(form, gbc, row++, "Petitioner:",  new JTextField(20));
        respondentField = addFormRow(form, gbc, row++, "Respondent:",  new JTextField(20));
 
        // Case Type dropdown
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Case Type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        typeCombo = new JComboBox<>(new String[]{"Civil","Criminal","Family","Property"});
        form.add(typeCombo, gbc); row++;
 
        // Status dropdown
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        form.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        statusCombo = new JComboBox<>(new String[]{"Pending","In Progress"});
        form.add(statusCombo, gbc); row++;
 
        // Filing date
        filingDateField = addFormRow(form, gbc, row++, "Filing Date (YYYY-MM-DD):", new JTextField(20));
        filingDateField.setText(LocalDate.now().toString());
 
        // Description
        descField = addFormRow(form, gbc, row++, "Description:", new JTextField(20));
 
        // Status label
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        form.add(statusLabel, gbc);
 
        // Buttons
        gbc.gridy = row; gbc.gridwidth = 1;
        JButton saveBtn  = new JButton("Save Case");
        JButton clearBtn = new JButton("Clear");
 
        saveBtn.setBackground(new Color(26, 107, 60));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        clearBtn.setFocusPainted(false);
 
        gbc.gridx = 0; form.add(saveBtn, gbc);
        gbc.gridx = 1; form.add(clearBtn, gbc);
 
        saveBtn.addActionListener(e  -> saveCase());
        clearBtn.addActionListener(e -> clearForm());
 
        add(heading, BorderLayout.NORTH);
        add(new JScrollPane(form), BorderLayout.CENTER);
    }
 
    private <T extends JTextField> T addFormRow(JPanel panel,
            GridBagConstraints gbc, int row, String label, T field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(field, gbc);
        return field;
    }
 
    private void saveCase() {
        try {
            Case c = new Case();
            c.setCaseNumber (caseNumberField.getText().trim());
            c.setTitle      (titleField.getText().trim());
            c.setPetitioner (petitionerField.getText().trim());
            c.setRespondent (respondentField.getText().trim());
            c.setCaseType   ((String) typeCombo.getSelectedItem());
            c.setStatus     ((String) statusCombo.getSelectedItem());
            c.setDescription(descField.getText().trim());
            c.setFilingDate (LocalDate.parse(filingDateField.getText().trim()));
 
            boolean ok = caseService.addCase(c);
            if (ok) {
                statusLabel.setForeground(new Color(26, 107, 60));
                statusLabel.setText("Case #" + c.getCaseId() + " registered successfully!");
                ReportExporter.log("Case added: " + c.getCaseNumber()
                        + " by " + currentUser.getUsername());
                clearForm();
            } else {
                statusLabel.setForeground(Color.RED);
                statusLabel.setText("Failed to save case. Check database.");
            }
        } catch (InvalidCaseException ex) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Validation error: " + ex.getMessage());
        } catch (Exception ex) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }
 
    private void clearForm() {
        caseNumberField.setText(""); titleField.setText("");
        petitionerField.setText(""); respondentField.setText("");
        descField.setText("");
        filingDateField.setText(LocalDate.now().toString());
        typeCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
    }
}
