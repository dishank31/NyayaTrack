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
 * and a clean login form on the right.  Entirely custom-drawn via Graphics2D.
 */
public class LoginFrame extends JFrame {

    private final UserService userService = new UserService();

    // UI Components
    private JTextField     usernameField;
    private JPasswordField passwordField;
    private RoundedButton  loginButton;
    private JLabel         statusLabel;
    private JCheckBox      showPasswordBox;

    public LoginFrame() {
        super("Nyaya Track — Judicial Case Monitoring System");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 540);
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
                String title1 = "न्याय Track";
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

        // ── RIGHT: Login form ────────────────────────────────────────────────
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UIConstants.BG_MAIN);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets    = new Insets(0, 0, 0, 0);
        gbc.anchor    = GridBagConstraints.CENTER;

        // Inner form container
        JPanel formBox = new JPanel();
        formBox.setLayout(new BoxLayout(formBox, BoxLayout.Y_AXIS));
        formBox.setOpaque(false);
        formBox.setPreferredSize(new Dimension(320, 380));

        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(UIConstants.FONT_TITLE);
        welcomeLabel.setForeground(UIConstants.PRIMARY_DARK);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel("Sign in to your account");
        subLabel.setFont(UIConstants.FONT_BODY);
        subLabel.setForeground(UIConstants.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        formBox.add(welcomeLabel);
        formBox.add(Box.createVerticalStrut(4));
        formBox.add(subLabel);
        formBox.add(Box.createVerticalStrut(32));

        // Username field
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(UIConstants.FONT_SMALL_BOLD);
        userLabel.setForeground(UIConstants.TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = createStyledTextField();
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formBox.add(userLabel);
        formBox.add(Box.createVerticalStrut(6));
        formBox.add(usernameField);
        formBox.add(Box.createVerticalStrut(18));

        // Password field
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(UIConstants.FONT_SMALL_BOLD);
        passLabel.setForeground(UIConstants.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = createStyledPasswordField();
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formBox.add(passLabel);
        formBox.add(Box.createVerticalStrut(6));
        formBox.add(passwordField);
        formBox.add(Box.createVerticalStrut(8));

        // Show password checkbox
        showPasswordBox = new JCheckBox("Show password");
        showPasswordBox.setFont(UIConstants.FONT_SMALL);
        showPasswordBox.setForeground(UIConstants.TEXT_SECONDARY);
        showPasswordBox.setOpaque(false);
        showPasswordBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordBox.addActionListener(e -> {
            passwordField.setEchoChar(showPasswordBox.isSelected() ? (char)0 : '\u2022');
        });
        formBox.add(showPasswordBox);
        formBox.add(Box.createVerticalStrut(20));

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL_BOLD);
        statusLabel.setForeground(UIConstants.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formBox.add(statusLabel);
        formBox.add(Box.createVerticalStrut(8));

        // Login button
        loginButton = RoundedButton.primary("Sign In");
        loginButton.setFont(UIConstants.FONT_BODY_BOLD);
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(320, 44));
        loginButton.setPreferredSize(new Dimension(320, 44));
        formBox.add(loginButton);

        formBox.add(Box.createVerticalStrut(18));

        // Footer info
        JLabel footerInfo = new JLabel("<html><center>Demo Credentials:<br>"
                + "<b>admin1</b> / admin123 (Admin)<br>"
                + "<b>judge_sharma</b> / judge123 (Judge)<br>"
                + "<b>clerk_raj</b> / clerk123 (Clerk)</center></html>");
        footerInfo.setFont(UIConstants.FONT_SMALL);
        footerInfo.setForeground(UIConstants.TEXT_MUTED);
        footerInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formBox.add(footerInfo);

        rightPanel.add(formBox, gbc);

        // ── Events ───────────────────────────────────────────────────────────
        loginButton.addActionListener(e -> performLogin());
        passwordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        });
        usernameField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) passwordField.requestFocusInWindow();
            }
        });

        root.add(leftPanel,  BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);
        setContentPane(root);
        setVisible(true);
        usernameField.requestFocusInWindow();
    }

    // ── Creates a modern styled text field ────────────────────────────────────
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
        // Repaint on focus change for border color
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

    // ── Draw the Scales of Justice using pure Graphics2D ──────────────────────
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

    // ── Login action ──────────────────────────────────────────────────────────
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setForeground(UIConstants.DANGER);
            statusLabel.setText("⚠  Please enter username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");
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
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");
                        passwordField.setText("");
                        passwordField.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    statusLabel.setForeground(UIConstants.DANGER);
                    statusLabel.setText("✗  System error. Check DB connection.");
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                }
            }
        };
        worker.execute();
    }
}
