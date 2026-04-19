package ui;

import model.Case;
import model.User;
import service.CaseService;
import util.ReportExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;

/**
 * Premium form panel for registering a new judicial case.
 * Uses custom-drawn components and the UIConstants design system.
 */
public class AddCasePanel extends JPanel {

    private final User        currentUser;
    private final CaseService caseService;

    private JTextField  caseNumberField, titleField, petitionerField,
                        respondentField, descField, filingDateField;
    private JComboBox<String> typeCombo, statusCombo;
    private JLabel      statusLabel;

    public AddCasePanel(User user, CaseService caseService) {
        this.currentUser = user;
        this.caseService = caseService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        // ── Heading ──────────────────────────────────────────────────────────
        JLabel heading = new JLabel("➕  Register New Case");
        heading.setFont(UIConstants.FONT_HEADING);
        heading.setForeground(UIConstants.PRIMARY_DARK);

        JLabel subtitle = new JLabel("Fill in the details below to register a new judicial case.");
        subtitle.setFont(UIConstants.FONT_BODY);
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.add(heading);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitle);

        // ── Form card ────────────────────────────────────────────────────────
        RoundedPanel formCard = new RoundedPanel();
        formCard.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // ── SECTION: Case Info ───────────────────────────────────────────────
        addSectionHeader(formCard, gbc, 0, "Case Information");

        caseNumberField = addFormRow(formCard, gbc, 1, "Case Number",
                "e.g., CRM/2025/001");
        titleField      = addFormRow(formCard, gbc, 2, "Case Title",
                "e.g., State vs Ramesh Kumar");

        // Type dropdown
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.gridwidth = 1;
        formCard.add(createFieldLabel("Case Type"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        typeCombo = createStyledCombo(
                new String[]{"Civil", "Criminal", "Family", "Property"});
        formCard.add(typeCombo, gbc);

        // Status dropdown
        gbc.gridx = 2; gbc.weightx = 0;
        formCard.add(createFieldLabel("Initial Status"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        statusCombo = createStyledCombo(
                new String[]{"Pending", "In Progress"});
        formCard.add(statusCombo, gbc);

        // ── SECTION: Parties ─────────────────────────────────────────────────
        addSectionHeader(formCard, gbc, 4, "Parties Involved");

        // Two-column layout for petitioner/respondent
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0; gbc.gridwidth = 1;
        formCard.add(createFieldLabel("Petitioner / Plaintiff"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        petitionerField = createStyledInput("Name of the petitioner");
        formCard.add(petitionerField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        formCard.add(createFieldLabel("Respondent / Defendant"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        respondentField = createStyledInput("Name of the respondent");
        formCard.add(respondentField, gbc);

        // ── SECTION: Details ─────────────────────────────────────────────────
        addSectionHeader(formCard, gbc, 6, "Case Details");

        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0; gbc.gridwidth = 1;
        formCard.add(createFieldLabel("Filing Date"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        filingDateField = createStyledInput("YYYY-MM-DD");
        filingDateField.setText(LocalDate.now().toString());
        formCard.add(filingDateField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        formCard.add(createFieldLabel("Description"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        descField = createStyledInput("Brief description...");
        formCard.add(descField, gbc);

        // ── Status feedback label ────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 4;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_BODY_BOLD);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        formCard.add(statusLabel, gbc);

        // ── Buttons ──────────────────────────────────────────────────────────
        gbc.gridy = 9; gbc.gridwidth = 1;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnPanel.setOpaque(false);

        RoundedButton saveBtn  = RoundedButton.success("Save Case");
        RoundedButton clearBtn = RoundedButton.ghost("Clear Form");

        saveBtn.addActionListener(e -> saveCase());
        clearBtn.addActionListener(e -> clearForm());

        btnPanel.add(saveBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridwidth = 4;
        formCard.add(btnPanel, gbc);

        add(headerPanel,          BorderLayout.NORTH);
        add(new JScrollPane(formCard) {{
            setBorder(null);
            getViewport().setOpaque(false);
            setOpaque(false);
        }}, BorderLayout.CENTER);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addSectionHeader(JPanel panel, GridBagConstraints gbc,
                                  int row, String text) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        JLabel section = new JLabel(text);
        section.setFont(UIConstants.FONT_SUBHEADING);
        section.setForeground(UIConstants.PRIMARY);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_LIGHT),
            BorderFactory.createEmptyBorder(12, 0, 8, 0)
        ));
        panel.add(section, gbc);
        gbc.gridwidth = 1;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_SMALL_BOLD);
        label.setForeground(UIConstants.TEXT_SECONDARY);
        return label;
    }

    private JTextField addFormRow(JPanel panel, GridBagConstraints gbc,
                                  int row, String label, String placeholder) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        panel.add(createFieldLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        JTextField field = createStyledInput(placeholder);
        panel.add(field, gbc);
        gbc.gridwidth = 1;
        return field;
    }

    private JTextField createStyledInput(String placeholder) {
        JTextField field = new JTextField(20) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0,
                        getWidth() - 1, getHeight() - 1,
                        UIConstants.INPUT_ARC, UIConstants.INPUT_ARC));
                g2.dispose();
                super.paintComponent(g);
                // Placeholder text
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D gp = (Graphics2D) g.create();
                    UIConstants.applyRenderingHints(gp);
                    gp.setColor(UIConstants.TEXT_MUTED);
                    gp.setFont(UIConstants.FONT_BODY);
                    Insets ins = getInsets();
                    gp.drawString(placeholder, ins.left + 2, getHeight() / 2 + 5);
                    gp.dispose();
                }
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);
                g2.setColor(isFocusOwner() ? UIConstants.PRIMARY_LIGHT : UIConstants.BORDER_MEDIUM);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
                g2.draw(new RoundRectangle2D.Double(0, 0,
                        getWidth() - 1, getHeight() - 1,
                        UIConstants.INPUT_ARC, UIConstants.INPUT_ARC));
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setBackground(UIConstants.BG_INPUT);
        field.setFont(UIConstants.FONT_BODY);
        field.setForeground(UIConstants.TEXT_PRIMARY);
        field.setCaretColor(UIConstants.PRIMARY_LIGHT);
        field.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        field.setPreferredSize(new Dimension(200, 40));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.repaint(); }
            @Override public void focusLost(FocusEvent e)   { field.repaint(); }
        });
        return field;
    }

    private JComboBox<String> createStyledCombo(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(UIConstants.FONT_BODY);
        combo.setBackground(UIConstants.BG_INPUT);
        combo.setForeground(UIConstants.TEXT_PRIMARY);
        combo.setPreferredSize(new Dimension(200, 40));
        combo.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        return combo;
    }

    // ── Save & Clear logic ───────────────────────────────────────────────────

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

            // Run on background thread
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override protected Boolean doInBackground() throws Exception {
                    return caseService.addCase(c);
                }
                @Override protected void done() {
                    try {
                        boolean ok = get();
                        if (ok) {
                            statusLabel.setForeground(UIConstants.SUCCESS);
                            statusLabel.setText("✓  Case #" + c.getCaseId()
                                    + " registered successfully!");
                            ReportExporter.log("Case added: " + c.getCaseNumber()
                                    + " by " + currentUser.getUsername());
                            clearForm();
                        } else {
                            statusLabel.setForeground(UIConstants.DANGER);
                            statusLabel.setText("✗  Failed to save case. Check database.");
                        }
                    } catch (Exception ex) {
                        statusLabel.setForeground(UIConstants.DANGER);
                        statusLabel.setText("✗  Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();

        } catch (Exception ex) {
            statusLabel.setForeground(UIConstants.DANGER);
            statusLabel.setText("✗  Error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        caseNumberField.setText(""); titleField.setText("");
        petitionerField.setText(""); respondentField.setText("");
        descField.setText("");
        filingDateField.setText(LocalDate.now().toString());
        typeCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        statusLabel.setText(" ");
    }
}
