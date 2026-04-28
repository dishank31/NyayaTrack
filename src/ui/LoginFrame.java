package ui;

import service.UserService;
import model.User;
import util.ReportExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Premium login screen — the first window the user sees.
 * Features a split-panel design with a branded illustration on the left
 * and a clean login/registration form on the right.  Entirely custom-drawn via Graphics2D.
 *
 * Supports two modes:
 *   - Sign In: authenticate with existing credentials
 *   - Create Account: register a new user with role selection
 */
public class LoginFrame extends JFrame {

    private final UserService userService = new UserService();

    // ── Mode flag ────────────────────────────────────────────────────────────
    private boolean isSignUpMode = false;

    // ── Shared UI Components ─────────────────────────────────────────────────
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JCheckBox      showPasswordBox;
    private RoundedButton  actionButton;
    private JLabel         statusLabel;

    // ── Sign-Up–only components ──────────────────────────────────────────────
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleCombo;
    private JTextField     extraInfoField;
    private JLabel         extraInfoLabel;

    // ── Containers ───────────────────────────────────────────────────────────
    private JPanel rightPanel;         // holds formBox (swapped on mode toggle)
    private JPanel formBox;

    // ── Labels for the toggle link ───────────────────────────────────────────
    private JLabel   headerLabel;
    private JLabel   subHeaderLabel;
    private JLabel   toggleLabel;

    public LoginFrame() {
        super("Nyaya Track — Judicial Case Monitoring System");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());

        // ── LEFT: Branded illustration panel ──────────────────────────────────
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);
                int w = getWidth(), h = getHeight();

                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, UIConstants.PRIMARY_DARK,
                        0, h, UIConstants.PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Decorative circles (abstract pattern)
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillOval(-60, -60, 300, 300);
                g2.fillOval(w - 200, h - 250, 350, 350);
                g2.setColor(new Color(255, 255, 255, 5));
                g2.fillOval(80, h - 180, 200, 200);

                // ── Draw Scales of Justice icon ───────────────────────────────
                drawScalesOfJustice(g2, w / 2, h / 2 - 30, 80);

                // App Title
                g2.setColor(UIConstants.TEXT_ON_DARK);
                g2.setFont(UIConstants.FONT_TITLE);
                String title1 = "Nyaya Track";
                FontMetrics fm1 = g2.getFontMetrics();
                g2.drawString(title1, (w - fm1.stringWidth(title1)) / 2, h / 2 + 80);

                g2.setFont(UIConstants.FONT_BODY);
                g2.setColor(UIConstants.TEXT_LIGHT);
                String sub = "Judicial Case Monitoring & Backlog Analysis";
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(sub, (w - fm2.stringWidth(sub)) / 2, h / 2 + 106);

                // Gold accent bar
                g2.setColor(UIConstants.ACCENT_GOLD);
                g2.fillRoundRect((w - 60) / 2, h / 2 + 120, 60, 3, 2, 2);

                // Version tag
                g2.setFont(UIConstants.FONT_SMALL);
                g2.setColor(UIConstants.TEXT_MUTED);
                String ver = "v2.0  •  JDBC + Swing";
                FontMetrics fm3 = g2.getFontMetrics();
                g2.drawString(ver, (w - fm3.stringWidth(ver)) / 2, h - 24);

                g2.dispose();
            }
        };
        leftPanel.setPreferredSize(new Dimension(420, 0));

        // ── RIGHT: Form panel ────────────────────────────────────────────────
        rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UIConstants.BG_MAIN);

        buildForm();  // builds formBox and places it in rightPanel

        root.add(leftPanel,  BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);
        setContentPane(root);
        setVisible(true);
        usernameField.requestFocusInWindow();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FORM BUILDER — constructs the right-side form for the current mode
    // ═════════════════════════════════════════════════════════════════════════
    private void buildForm() {
        rightPanel.removeAll();

        formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setOpaque(false);
        formBox.setPreferredSize(new Dimension(320, isSignUpMode ? 520 : 400));

        // ── Header ───────────────────────────────────────────────────────────
        headerLabel = new JLabel(isSignUpMode ? "Create Account" : "Welcome Back");
        headerLabel.setFont(UIConstants.FONT_TITLE);
        headerLabel.setForeground(UIConstants.PRIMARY_DARK);
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        subHeaderLabel = new JLabel(isSignUpMode
                ? "Register a new account to get started"
                : "Sign in to your account");
        subHeaderLabel.setFont(UIConstants.FONT_BODY);
        subHeaderLabel.setForeground(UIConstants.TEXT_SECONDARY);
        subHeaderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        formBox.add(headerLabel);
        formBox.add(Box.createVerticalStrut(4));
        formBox.add(subHeaderLabel);
        formBox.add(Box.createVerticalStrut(28));

        // ── Username ─────────────────────────────────────────────────────────
        formBox.add(createLabel("USERNAME"));
        formBox.add(Box.createVerticalStrut(6));
        usernameField = createStyledTextField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formBox.add(usernameField);
        formBox.add(Box.createVerticalStrut(14));

        // ── Password ─────────────────────────────────────────────────────────
        formBox.add(createLabel("PASSWORD"));
        formBox.add(Box.createVerticalStrut(6));
        passwordField = createStyledPasswordField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formBox.add(passwordField);
        formBox.add(Box.createVerticalStrut(6));

        if (isSignUpMode) {
            // ── Confirm Password ─────────────────────────────────────────────
            formBox.add(createLabel("CONFIRM PASSWORD"));
            formBox.add(Box.createVerticalStrut(6));
            confirmPasswordField = createStyledPasswordField();
            confirmPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
            formBox.add(confirmPasswordField);
            formBox.add(Box.createVerticalStrut(14));

            // ── Role Selection ───────────────────────────────────────────────
            formBox.add(createLabel("ROLE"));
            formBox.add(Box.createVerticalStrut(6));
            roleCombo = createStyledComboBox(new String[]{"Admin", "Judge", "Clerk"});
            roleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
            formBox.add(roleCombo);
            formBox.add(Box.createVerticalStrut(14));

            // ── Extra Info (dynamic label) ───────────────────────────────────
            extraInfoLabel = createLabel("ADDITIONAL INFO (OPTIONAL)");
            extraInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            formBox.add(extraInfoLabel);
            formBox.add(Box.createVerticalStrut(6));
            extraInfoField = createStyledTextField();
            extraInfoField.setAlignmentX(Component.LEFT_ALIGNMENT);
            formBox.add(extraInfoField);
            formBox.add(Box.createVerticalStrut(6));

            // Update extra label dynamically based on role
            updateExtraInfoLabel();
            roleCombo.addActionListener(e -> updateExtraInfoLabel());

        } else {
            // Show password checkbox (login only)
            showPasswordBox = new JCheckBox("Show password");
            showPasswordBox.setFont(UIConstants.FONT_SMALL);
            showPasswordBox.setForeground(UIConstants.TEXT_SECONDARY);
            showPasswordBox.setOpaque(false);
            showPasswordBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            showPasswordBox.addActionListener(e -> {
                passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : '\u2022');
            });
            formBox.add(showPasswordBox);
        }
        formBox.add(Box.createVerticalStrut(14));

        // ── Status label ─────────────────────────────────────────────────────
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL_BOLD);
        statusLabel.setForeground(UIConstants.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formBox.add(statusLabel);
        formBox.add(Box.createVerticalStrut(8));

        // ── Action button ────────────────────────────────────────────────────
        if (isSignUpMode) {
            actionButton = RoundedButton.success("Create Account");
        } else {
            actionButton = RoundedButton.primary("Sign In");
        }
        actionButton.setFont(UIConstants.FONT_BODY_BOLD);
        actionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButton.setMaximumSize(new Dimension(320, 44));
        actionButton.setPreferredSize(new Dimension(320, 44));
        formBox.add(actionButton);
        formBox.add(Box.createVerticalStrut(16));

        // ── Toggle link ──────────────────────────────────────────────────────
        toggleLabel = new JLabel(isSignUpMode
                ? "<html>Already have an account? <b><u>Sign In</u></b></html>"
                : "<html>Don't have an account? <b><u>Create Account</u></b></html>");
        toggleLabel.setFont(UIConstants.FONT_SMALL);
        toggleLabel.setForeground(UIConstants.PRIMARY_LIGHT);
        toggleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        toggleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isSignUpMode = !isSignUpMode;
                buildForm();
                rightPanel.revalidate();
                rightPanel.repaint();
                usernameField.requestFocusInWindow();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                toggleLabel.setForeground(UIConstants.PRIMARY_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                toggleLabel.setForeground(UIConstants.PRIMARY_LIGHT);
            }
        });
        formBox.add(toggleLabel);

        // ── Events ───────────────────────────────────────────────────────────
        actionButton.addActionListener(e -> {
            if (isSignUpMode) {
                performRegistration();
            } else {
                performLogin();
            }
        });

        // Enter key handling
        KeyAdapter enterHandler = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (isSignUpMode) performRegistration();
                    else              performLogin();
                }
            }
        };
        passwordField.addKeyListener(enterHandler);
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) passwordField.requestFocusInWindow();
            }
        });
        if (isSignUpMode && confirmPasswordField != null) {
            confirmPasswordField.addKeyListener(enterHandler);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(formBox, gbc);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPER: Update the extra-info label based on selected role
    // ═════════════════════════════════════════════════════════════════════════
    private void updateExtraInfoLabel() {
        if (roleCombo == null || extraInfoLabel == null || extraInfoField == null) return;
        String role = (String) roleCombo.getSelectedItem();
        switch (role) {
            case "Judge":
                extraInfoLabel.setText("SPECIALIZATION");
                extraInfoField.setToolTipText("e.g., Criminal, Civil, Family");
                break;
            case "Clerk":
                extraInfoLabel.setText("COURT ID");
                extraInfoField.setToolTipText("e.g., 1, 2, 3");
                break;
            default:
                extraInfoLabel.setText("ADDITIONAL INFO (OPTIONAL)");
                extraInfoField.setToolTipText("");
                break;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  STYLED COMPONENT FACTORIES
    // ═════════════════════════════════════════════════════════════════════════

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIConstants.FONT_SMALL_BOLD);
        label.setForeground(UIConstants.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createStyledTextField() {
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
        field.setMaximumSize(new Dimension(320, 42));
        field.setPreferredSize(new Dimension(320, 42));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.repaint(); }
            @Override public void focusLost(FocusEvent e)   { field.repaint(); }
        });
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyRenderingHints(g2);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0,
                        getWidth() - 1, getHeight() - 1,
                        UIConstants.INPUT_ARC, UIConstants.INPUT_ARC));
                g2.dispose();
                super.paintComponent(g);
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
        field.setMaximumSize(new Dimension(320, 42));
        field.setPreferredSize(new Dimension(320, 42));
        field.setEchoChar('\u2022');
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.repaint(); }
            @Override public void focusLost(FocusEvent e)   { field.repaint(); }
        });
        return field;
    }

    /**
     * Creates a styled JComboBox that visually matches the rest of the form.
     */
    @SuppressWarnings("unchecked")
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(UIConstants.FONT_BODY);
        combo.setBackground(UIConstants.BG_INPUT);
        combo.setForeground(UIConstants.TEXT_PRIMARY);
        combo.setMaximumSize(new Dimension(320, 42));
        combo.setPreferredSize(new Dimension(320, 42));
        combo.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Custom renderer for styled dropdown items
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setFont(UIConstants.FONT_BODY);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                if (isSelected) {
                    lbl.setBackground(UIConstants.PRIMARY_LIGHT);
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(UIConstants.BG_INPUT);
                    lbl.setForeground(UIConstants.TEXT_PRIMARY);
                }
                // Add role descriptor for visual clarity
                String text = (String) value;
                switch (text) {
                    case "Admin" -> lbl.setText("[A]  Administrator");
                    case "Judge" -> lbl.setText("[J]  Judge");
                    case "Clerk" -> lbl.setText("[C]  Clerk");
                }
                return lbl;
            }
        });
        return combo;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SCALES OF JUSTICE (Graphics2D illustration)
    // ═════════════════════════════════════════════════════════════════════════

    private void drawScalesOfJustice(Graphics2D g2, int cx, int cy, int size) {
        g2.setColor(UIConstants.ACCENT_GOLD);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Pillar (vertical line)
        g2.drawLine(cx, cy - size, cx, cy + size);

        // Base
        g2.fillRoundRect(cx - size / 2, cy + size - 4, size, 8, 4, 4);

        // Cross beam
        int beamY = cy - size + 5;
        g2.drawLine(cx - size + 10, beamY, cx + size - 10, beamY);

        // Left scale pan
        int lx = cx - size + 10;
        drawPan(g2, lx, beamY, 22, 15);

        // Right scale pan
        int rx = cx + size - 10;
        drawPan(g2, rx, beamY, 22, -8);

        // Top ornament (circle)
        g2.setColor(UIConstants.ACCENT_GOLD);
        g2.fillOval(cx - 6, cy - size - 6, 12, 12);
    }

    private void drawPan(Graphics2D g2, int x, int topY, int depth, int tilt) {
        int py = topY + depth + tilt;
        // Strings
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(x, topY, x - 14, py);
        g2.drawLine(x, topY, x + 14, py);

        // Pan (arc)
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Arc2D.Double(x - 16, py - 5, 32, 14, 0, -180, Arc2D.OPEN));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LOGIN ACTION
    // ═════════════════════════════════════════════════════════════════════════
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setForeground(UIConstants.DANGER);
            statusLabel.setText("⚠  Please enter username and password.");
            return;
        }

        actionButton.setEnabled(false);
        actionButton.setText("Authenticating...");
        statusLabel.setForeground(UIConstants.INFO);
        statusLabel.setText("Connecting to database...");

        // Run authentication in background thread
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return userService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        ReportExporter.log("User logged in: " + username
                                + " [Role: " + user.getRole() + "]");
                        dispose();
                        new DashboardFrame(user);
                    } else {
                        statusLabel.setForeground(UIConstants.DANGER);
                        statusLabel.setText("✗  Invalid credentials. Try again.");
                        actionButton.setEnabled(true);
                        actionButton.setText("Sign In");
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(UIConstants.DANGER);
                    statusLabel.setText("✗  System error. Check DB connection.");
                    actionButton.setEnabled(true);
                    actionButton.setText("Sign In");
                }
            }
        };
        worker.execute();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  REGISTRATION ACTION
    // ═════════════════════════════════════════════════════════════════════════
    private void performRegistration() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPwd = new String(confirmPasswordField.getPassword());
        String role = (String) roleCombo.getSelectedItem();
        String extra = extraInfoField.getText().trim();

        // ── Client-side validation ───────────────────────────────────────────
        if (username.isEmpty() || password.isEmpty() || confirmPwd.isEmpty()) {
            statusLabel.setForeground(UIConstants.DANGER);
            statusLabel.setText("⚠  Please fill in all required fields.");
            return;
        }
        if (!password.equals(confirmPwd)) {
            statusLabel.setForeground(UIConstants.DANGER);
            statusLabel.setText("✗  Passwords do not match.");
            confirmPasswordField.setText("");
            confirmPasswordField.requestFocusInWindow();
            return;
        }
        if (password.length() < 6) {
            statusLabel.setForeground(UIConstants.DANGER);
            statusLabel.setText("✗  Password must be at least 6 characters.");
            return;
        }
        if (username.length() < 3) {
            statusLabel.setForeground(UIConstants.DANGER);
            statusLabel.setText("✗  Username must be at least 3 characters.");
            return;
        }

        // Extra info validation for Judge/Clerk
        if ("Judge".equals(role) && extra.isEmpty()) {
            statusLabel.setForeground(UIConstants.DANGER);
            statusLabel.setText("✗  Specialization is required for Judges.");
            extraInfoField.requestFocusInWindow();
            return;
        }
        if ("Clerk".equals(role) && extra.isEmpty()) {
            statusLabel.setForeground(UIConstants.DANGER);
            statusLabel.setText("✗  Court ID is required for Clerks.");
            extraInfoField.requestFocusInWindow();
            return;
        }

        actionButton.setEnabled(false);
        actionButton.setText("Creating account...");
        statusLabel.setForeground(UIConstants.INFO);
        statusLabel.setText("Registering...");

        // If role is Admin, extra info is optional (pass null if empty)
        String extraVal = extra.isEmpty() ? null : extra;

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return userService.registerUserWithMessage(username, password, role, extraVal);
            }

            @Override
            protected void done() {
                try {
                    String error = get();
                    if (error == null) {
                        // Success — switch to login mode
                        ReportExporter.log("New user registered: " + username
                                + " [Role: " + role + "]");
                        statusLabel.setForeground(UIConstants.SUCCESS);
                        statusLabel.setText("✓  Account created! Please sign in.");

                        // Auto-switch to login mode after a brief delay
                        Timer switchTimer = new Timer(1500, ev -> {
                            isSignUpMode = false;
                            buildForm();
                            rightPanel.revalidate();
                            rightPanel.repaint();
                            usernameField.setText(username);
                            passwordField.requestFocusInWindow();
                            statusLabel.setForeground(UIConstants.SUCCESS);
                            statusLabel.setText("✓  Account created! Sign in below.");
                        });
                        switchTimer.setRepeats(false);
                        switchTimer.start();
                    } else {
                        statusLabel.setForeground(UIConstants.DANGER);
                        statusLabel.setText("✗  " + error);
                        actionButton.setEnabled(true);
                        actionButton.setText("Create Account");
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(UIConstants.DANGER);
                    statusLabel.setText("✗  System error. Check DB connection.");
                    actionButton.setEnabled(true);
                    actionButton.setText("Create Account");
                }
            }
        };
        worker.execute();
    }
}
