package presentation;

import Service.AuthService;
import Service.BookingService;
import Service.ReminderService;
import domain.User;
import domain.Provider;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.prefs.Preferences;

/**
 * Login window (Swing UI).
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Authenticate users via {@link AuthService#login(String, String)}.</li>
 *   <li>Optionally remember the username locally using {@link Preferences} ("Keep Me Logged In").</li>
 *   <li>Support an Admin login mode protected by an extra Admin Key.</li>
 *   <li>Route the user to the correct screen after login:</li>
 *   <ul>
 *     <li>Admin → {@link AdminDashboardFrame}</li>
 *     <li>Provider account ({@link Provider}) → {@link ProviderInboxFrame}</li>
 *     <li>Normal user → {@link MainDashboardFrame} (and start {@link ReminderService})</li>
 *   </ul>
 * </ul>
 * </p>
 *
 * <p>
 * Notes:
 * <ul>
 *   <li>This is a demo/learning UI (password recovery shows stored password in plain text if enabled).</li>
 *   <li>Admin Key is hardcoded here via {@link #ADMIN_KEY}.</li>
 * </ul>
 * </p>
 */
public class LoginFrame extends JFrame {

    /** Preferences node for storing "remember me" configuration. */
    private static final String PREF_NODE = "Soft_Proj";

    /** Preferences key: whether remember-me is enabled. */
    private static final String PREF_REMEMBER = "remember_me";

    /** Preferences key: remembered username value. */
    private static final String PREF_USERNAME = "remembered_username";

    /** Hardcoded admin key required when "Login as Admin" is selected. */
    private static final String ADMIN_KEY = "ADMIN2026";

    /** Authentication service used to login and read current user. */
    private final AuthService authService;

    /** Booking service (passed to other screens after login). */
    private final BookingService bookingService;

    /** Repository used for password recovery and passing to other screens. */
    private final DataRepository repo;

    // Fields
    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton signUpButton;

    private JCheckBox keepLoggedIn;

    private JCheckBox loginAsAdmin;

    private JPasswordField adminKeyField;
    private JLabel adminKeyLabel;
    private JPanel adminPanel;

    /** Preferences storage for remember-me. */
    private final Preferences prefs = Preferences.userRoot().node(PREF_NODE);

    private JLabel usernameLabel;
    private JLabel passwordLabel;

    /**
     * Creates the login window.
     *
     * @param authService    authentication service
     * @param bookingService booking service (used later after login)
     * @param repo           repository used by UI screens (users/appointments/messages)
     */
    public LoginFrame(AuthService authService, BookingService bookingService, DataRepository repo) {
        this.authService = authService;
        this.bookingService = bookingService;
        this.repo = repo;

        initUI();
        loadRememberedUser();
        attachHandlers();
    }

    /**
     * Initializes and lays out the Swing components of the login window.
     */
    private void initUI() {
        setTitle("Login");
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        URL imgUrl = getClass().getResource("/1120.png");
        Image bg = (imgUrl != null) ? new ImageIcon(imgUrl).getImage() : null;

        BackgroundPanel root = new BackgroundPanel(bg);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        setContentPane(root);

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setOpaque(true);
        card.setBackground(new Color(0, 0, 0, 140));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setPreferredSize(new Dimension(520, 360));

        JLabel title = new JLabel("Login", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        card.add(form, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(18);
        passwordField = new JPasswordField(18);

        styleField(usernameField);
        styleField(passwordField);

        usernameLabel = labelWhite("Username:");
        passwordLabel = labelWhite("Password:");

        c.gridx = 0; c.gridy = 0; c.weightx = 0; c.anchor = GridBagConstraints.WEST;
        form.add(usernameLabel, c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        form.add(usernameField, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        form.add(passwordLabel, c);
        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        form.add(passwordField, c);

        keepLoggedIn = new JCheckBox("Keep Me Logged In");
        keepLoggedIn.setOpaque(false);
        keepLoggedIn.setForeground(new Color(255, 255, 255, 230));
        keepLoggedIn.setFocusPainted(false);

        c.gridx = 1; c.gridy = 2; c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        form.add(keepLoggedIn, c);

        loginAsAdmin = new JCheckBox("Login as Admin");
        loginAsAdmin.setOpaque(false);
        loginAsAdmin.setForeground(new Color(255, 255, 255, 230));
        loginAsAdmin.setFocusPainted(false);

        c.gridx = 1; c.gridy = 3; c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        form.add(loginAsAdmin, c);

        adminPanel = new JPanel(new GridBagLayout());
        adminPanel.setOpaque(false);

        GridBagConstraints a = new GridBagConstraints();
        a.insets = new Insets(8, 8, 8, 8);
        a.fill = GridBagConstraints.HORIZONTAL;

        adminKeyLabel = labelWhite("Admin Key:");
        adminKeyField = new JPasswordField(18);
        styleField(adminKeyField);

        a.gridx = 0; a.gridy = 0; a.weightx = 0; a.anchor = GridBagConstraints.WEST;
        adminPanel.add(adminKeyLabel, a);
        a.gridx = 1; a.gridy = 0; a.weightx = 1;
        adminPanel.add(adminKeyField, a);

        adminPanel.setVisible(false);

        c.gridx = 0; c.gridy = 4; c.weightx = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        form.add(adminPanel, c);
        c.gridwidth = 1;

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        loginButton = new JButton("Log In");
        signUpButton = new JButton("Sign Up");

        buttons.add(loginButton);
        buttons.add(signUpButton);

        south.add(buttons);

        card.add(south, BorderLayout.SOUTH);
        root.add(card);
    }

    private JLabel labelWhite(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        return l;
    }

    private void styleField(JComponent field) {
        field.setFont(field.getFont().deriveFont(14.5f));
        field.setBackground(new Color(255, 255, 255, 235));
        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    private void attachHandlers() {
        loginButton.addActionListener(e -> login());
        signUpButton.addActionListener(e -> openSignUp());

        keepLoggedIn.addActionListener(e -> {
            boolean remember = keepLoggedIn.isSelected();
            prefs.putBoolean(PREF_REMEMBER, remember);
            if (!remember) {
                prefs.remove(PREF_USERNAME);
            }
        });

        loginAsAdmin.addActionListener(e -> {
            boolean adminMode = loginAsAdmin.isSelected();
            setAdminMode(adminMode);
        });

        getRootPane().setDefaultButton(loginButton);
    }

    
    private void setAdminMode(boolean adminMode) {

        adminPanel.setVisible(adminMode);

        usernameLabel.setVisible(!adminMode);
        usernameField.setVisible(!adminMode);

        passwordLabel.setVisible(!adminMode);
        passwordField.setVisible(!adminMode);

        keepLoggedIn.setVisible(!adminMode);

        signUpButton.setVisible(!adminMode);

        if (adminMode) {
            usernameField.setText("");
            passwordField.setText("");
            keepLoggedIn.setSelected(false);
            adminKeyField.setText("");
        } else {
            adminKeyField.setText("");
        }

        adminPanel.getParent().revalidate();
        adminPanel.getParent().repaint();
        this.revalidate();
        this.repaint();
    }

    private void loadRememberedUser() {
        boolean remember = prefs.getBoolean(PREF_REMEMBER, false);
        keepLoggedIn.setSelected(remember);

        if (remember) {
            String remembered = prefs.get(PREF_USERNAME, "");
            if (remembered != null && !remembered.trim().isEmpty()) {
                usernameField.setText(remembered);
                usernameField.setCaretPosition(usernameField.getText().length());
            }
        }
    }

    /**
     * Performs login and navigates based on role.
     */
    private void login() {

        boolean wantsAdmin = (loginAsAdmin != null && loginAsAdmin.isSelected());

        if (wantsAdmin) {
            String key = new String(adminKeyField.getPassword()).trim();

            if (!ADMIN_KEY.equals(key)) {
                JOptionPane.showMessageDialog(this, "Invalid Admin Key.");
                return;
            }

            boolean ok = authService.loginAsAdmin();
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Admin account not found in repository.");
                return;
            }

            new AdminDashboardFrame(authService, bookingService, repo).setVisible(true);
            dispose();
            return;
        }

        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (authService.login(username, password)) {

            if (authService.getCurrentUser() instanceof Provider) {
                new ProviderInboxFrame(authService, repo).setVisible(true);
                dispose();
                return;
            }

            if (keepLoggedIn.isSelected()) {
                prefs.putBoolean(PREF_REMEMBER, true);
                prefs.put(PREF_USERNAME, username != null ? username.trim() : "");
            } else {
                prefs.putBoolean(PREF_REMEMBER, false);
                prefs.remove(PREF_USERNAME);
            }

            ReminderService reminder = new ReminderService(repo, authService, 60);
            reminder.start();

            new MainDashboardFrame(authService, bookingService, repo, reminder).setVisible(true);
            dispose();

        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
        }
    }

    private void openSignUp() {
        new SignUpFrame(authService).setVisible(true);
    }

    @SuppressWarnings("unused")
    private void openForgotPasswordDialog() {
        String u = JOptionPane.showInputDialog(
                this,
                "Enter your username:",
                "Forgot Password",
                JOptionPane.QUESTION_MESSAGE
        );

        if (u == null) return;
        u = u.trim();
        if (u.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.");
            return;
        }

        User found = null;
        for (User user : repo.getUsers()) {
            if (user.getUsername().equalsIgnoreCase(u)) {
                found = user;
                break;
            }
        }

        if (found == null) {
            JOptionPane.showMessageDialog(this,
                    "No account found with this username.",
                    "Not found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Your password is: " + found.getPassword(),
                "Password Recovery",
                JOptionPane.INFORMATION_MESSAGE);
    }
}