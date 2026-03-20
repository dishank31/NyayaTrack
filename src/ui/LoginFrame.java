package ui;
 
import service.UserService;
import model.User;
import util.ReportExporter;
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
 
/**
 * Login screen — first window the user sees.
 * Uses Java Swing components.
 */
public class LoginFrame extends JFrame {
 
    private final UserService userService = new UserService();
 
    // UI Components
    private JTextField  usernameField;
    private JPasswordField passwordField;
    private JButton     loginButton;
    private JLabel      statusLabel;
 
    public LoginFrame() {
        super("Judicial Case Monitoring System — Login");
        initUI();
    }
 
    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 360);
        setLocationRelativeTo(null);  // center on screen
        setResizable(false);
 
        // ── Main panel ────────────────────────────────────────────────────────
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(26, 60, 110)); // DARK_BLUE
 
        // ── Header banner ────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(26, 60, 110));
        header.setBorder(BorderFactory.createEmptyBorder(24, 20, 10, 20));
 
        JLabel title = new JLabel("⚖ Judicial Case Monitoring");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
 
        JLabel subtitle = new JLabel("Backlog Analysis System");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(new Color(180, 200, 230));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
 
        header.add(title,    BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
 
        // ── Form panel ────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(24, 36, 24, 36));
 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
 
        // Username row
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        usernameField = new JTextField(18);
        form.add(usernameField, gbc);
 
        // Password row
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        passwordField = new JPasswordField(18);
        form.add(passwordField, gbc);
 
        // Status label
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        form.add(statusLabel, gbc);
 
        // Login button
        gbc.gridy = 3;
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(46, 95, 170));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        form.add(loginButton, gbc);
 
        // ── Event: login button click ─────────────────────────────────────────
        loginButton.addActionListener(e -> performLogin());
 
        // ── Event: press Enter in password field ──────────────────────────────
        passwordField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        });
 
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(form,   BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }
 
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
 
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }
 
        loginButton.setEnabled(false);
        statusLabel.setText("Authenticating...");
 
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
                        ReportExporter.log("User logged in: " + username);
                        dispose(); // close login window
                        new DashboardFrame(user); // open dashboard
                    } else {
                        statusLabel.setText("Invalid credentials. Try again.");
                        loginButton.setEnabled(true);
                        passwordField.setText("");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("System error. Check DB connection.");
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
