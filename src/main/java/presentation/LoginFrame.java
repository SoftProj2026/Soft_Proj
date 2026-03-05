package presentation;

import Service.AuthService;
import Service.BookingRequestService;
import Service.BookingService;
import Service.ReminderService;
import domain.Administrator;
import domain.Category;
import domain.Provider;
import persistence.DataRepository;
import persistence.RepoStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.prefs.Preferences;

/**
 * Login window implemented using Swing.
 * <p>
 * Supports three login modes:
 * </p>
 * <ul>
 *   <li><b>Normal Login</b>: username/password for regular users and providers.</li>
 *   <li><b>Login for Category Admin</b>: select category + enter Category Admin Key (no username/password).</li>
 *   <li><b>QR Admin for Company</b>: admin-key mode that logs in as the "admin" account.</li>
 * </ul>
 *
 * <p>
 * Security rule:
 * </p>
 * <ul>
 *   <li>Category admins (administrator accounts other than {@code admin}) are blocked in Normal Login mode.</li>
 *   <li>Category admins may login only when "Login for Category Admin" is selected.</li>
 *   <li>The big admin account {@code admin} may always access the admin dashboard.</li>
 * </ul>
 */
public class LoginFrame extends JFrame {

    private static final String PREF_NODE = "Soft_Proj";
    private static final String PREF_REMEMBER = "remember_me";
    private static final String PREF_USERNAME = "remembered_username";

    private static final String ADMIN_KEY = "ADMIN2026";

    private final AuthService authService;
    private final BookingService bookingService;
    private final DataRepository repo;

    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton signUpButton;

    private JCheckBox keepLoggedIn;

    private JCheckBox loginAsQrAdmin;
    private JCheckBox loginAsCategoryAdmin;

    private JPasswordField adminKeyField;
    private JLabel adminKeyLabel;
    private JPanel adminPanel;

    private JPanel categoryAdminPanel;
    private JComboBox<Category> categoryBox;
    private JPasswordField categoryKeyField;
    private JLabel categoryLabel;
    private JLabel categoryKeyLabel;

    private final Preferences prefs = Preferences.userRoot().node(PREF_NODE);

    private JLabel usernameLabel;
    private JLabel passwordLabel;

    /**
     * Creates the login window.
     *
     * @param authService    authentication service
     * @param bookingService booking service
     * @param repo           data repository
     */
    public LoginFrame(AuthService authService, BookingService bookingService, DataRepository repo) {
        this.authService = authService;
        this.bookingService = bookingService;
        this.repo = repo;

        initUI();

        // Save on close (persist data.json)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                RepoStorage.save(repo);
            }
        });

        loadRememberedUser();
        attachHandlers();
        applyMode();
    }

    /**
     * Initializes and lays out the Swing UI components.
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
        card.setPreferredSize(new Dimension(560, 410));

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

        // Normal login fields
        usernameField = new JTextField(18);
        passwordField = new JPasswordField(18);

        styleField(usernameField);
        styleField(passwordField);

        usernameLabel = labelWhite("Username:");
        passwordLabel = labelWhite("Password:");

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.anchor = GridBagConstraints.WEST;
        form.add(usernameLabel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        form.add(usernameField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        form.add(passwordLabel, c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        form.add(passwordField, c);

        keepLoggedIn = new JCheckBox("Keep Me Logged In");
        keepLoggedIn.setOpaque(false);
        keepLoggedIn.setForeground(new Color(255, 255, 255, 230));
        keepLoggedIn.setFocusPainted(false);

        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        form.add(keepLoggedIn, c);

        loginAsCategoryAdmin = new JCheckBox("Login for Category Admin");
        loginAsCategoryAdmin.setOpaque(false);
        loginAsCategoryAdmin.setForeground(new Color(255, 255, 255, 230));
        loginAsCategoryAdmin.setFocusPainted(false);

        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        form.add(loginAsCategoryAdmin, c);

        loginAsQrAdmin = new JCheckBox("QR Admin for Company");
        loginAsQrAdmin.setOpaque(false);
        loginAsQrAdmin.setForeground(new Color(255, 255, 255, 230));
        loginAsQrAdmin.setFocusPainted(false);

        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        form.add(loginAsQrAdmin, c);

        adminPanel = new JPanel(new GridBagLayout());
        adminPanel.setOpaque(false);

        GridBagConstraints a = new GridBagConstraints();
        a.insets = new Insets(8, 8, 8, 8);
        a.fill = GridBagConstraints.HORIZONTAL;

        adminKeyLabel = labelWhite("QR Admin Key:");
        adminKeyField = new JPasswordField(18);
        styleField(adminKeyField);

        a.gridx = 0;
        a.gridy = 0;
        a.weightx = 0;
        a.anchor = GridBagConstraints.WEST;
        adminPanel.add(adminKeyLabel, a);

        a.gridx = 1;
        a.gridy = 0;
        a.weightx = 1;
        adminPanel.add(adminKeyField, a);

        adminPanel.setVisible(false);

        categoryAdminPanel = new JPanel(new GridBagLayout());
        categoryAdminPanel.setOpaque(false);

        GridBagConstraints ca = new GridBagConstraints();
        ca.insets = new Insets(8, 8, 8, 8);
        ca.fill = GridBagConstraints.HORIZONTAL;

        categoryLabel = labelWhite("Category:");
        categoryKeyLabel = labelWhite("Category Admin Key:");

        categoryBox = new JComboBox<>();
        categoryBox.setFont(categoryBox.getFont().deriveFont(14.5f));
        categoryBox.setBackground(new Color(255, 255, 255, 235));
        categoryBox.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        categoryBox.setPreferredSize(new Dimension(260, 36));

        for (Category cat : repo.getCategories()) {
            categoryBox.addItem(cat);
        }

        categoryKeyField = new JPasswordField(18);
        styleField(categoryKeyField);

        ca.gridx = 0;
        ca.gridy = 0;
        ca.weightx = 0;
        ca.anchor = GridBagConstraints.WEST;
        categoryAdminPanel.add(categoryLabel, ca);

        ca.gridx = 1;
        ca.gridy = 0;
        ca.weightx = 1;
        categoryAdminPanel.add(categoryBox, ca);

        ca.gridx = 0;
        ca.gridy = 1;
        ca.weightx = 0;
        categoryAdminPanel.add(categoryKeyLabel, ca);

        ca.gridx = 1;
        ca.gridy = 1;
        ca.weightx = 1;
        categoryAdminPanel.add(categoryKeyField, ca);

        categoryAdminPanel.setVisible(false);

        c.gridx = 0;
        c.gridy = 5;
        c.weightx = 1;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        form.add(adminPanel, c);

        c.gridy = 6;
        form.add(categoryAdminPanel, c);

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

    /**
     * Creates a white label for dark backgrounds.
     */
    private JLabel labelWhite(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        return l;
    }

    /**
     * Styles an input component.
     */
    private void styleField(JComponent field) {
        field.setFont(field.getFont().deriveFont(14.5f));
        field.setBackground(new Color(255, 255, 255, 235));
        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    /**
     * Attaches UI event handlers.
     */
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

        loginAsQrAdmin.addActionListener(e -> {
            if (loginAsQrAdmin.isSelected()) loginAsCategoryAdmin.setSelected(false);
            applyMode();
        });

        loginAsCategoryAdmin.addActionListener(e -> {
            if (loginAsCategoryAdmin.isSelected()) loginAsQrAdmin.setSelected(false);
            applyMode();
        });

        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * Applies the selected login mode to the UI by showing/hiding relevant controls.
     */
    private void applyMode() {
        boolean qrAdminMode = loginAsQrAdmin != null && loginAsQrAdmin.isSelected();
        boolean categoryAdminMode = loginAsCategoryAdmin != null && loginAsCategoryAdmin.isSelected();

        adminPanel.setVisible(qrAdminMode);
        categoryAdminPanel.setVisible(categoryAdminMode);

        boolean normalFieldsVisible = !qrAdminMode && !categoryAdminMode;

        usernameLabel.setVisible(normalFieldsVisible);
        usernameField.setVisible(normalFieldsVisible);

        passwordLabel.setVisible(normalFieldsVisible);
        passwordField.setVisible(normalFieldsVisible);

        keepLoggedIn.setVisible(normalFieldsVisible);
        signUpButton.setVisible(normalFieldsVisible);

        adminPanel.getParent().revalidate();
        adminPanel.getParent().repaint();
        revalidate();
        repaint();
    }

    /**
     * Loads remembered username from preferences when enabled.
     */
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
     * Performs login based on the current mode and navigates to the appropriate screen.
     */
    private void login() {
        boolean qrAdminMode = loginAsQrAdmin != null && loginAsQrAdmin.isSelected();
        boolean categoryAdminMode = loginAsCategoryAdmin != null && loginAsCategoryAdmin.isSelected();

        if (qrAdminMode) {
            String key = new String(adminKeyField.getPassword()).trim();

            if (!ADMIN_KEY.equals(key)) {
                JOptionPane.showMessageDialog(this, "Invalid QR Admin Key.");
                return;
            }

            boolean ok = authService.loginAsAdmin();
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Admin account not found in repository.");
                return;
            }

            RepoStorage.save(repo);
            new AdminDashboardFrame(authService, bookingService, repo).setVisible(true);
            dispose();
            return;
        }

        if (categoryAdminMode) {
            Category selected = (Category) categoryBox.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a category first.");
                return;
            }

            String inputKey = new String(categoryKeyField.getPassword()).trim();
            if (inputKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter Category Admin Key.");
                return;
            }

            String expectedKey = BookingRequestService.categoryAdminKey(selected); 
            if (!expectedKey.equalsIgnoreCase(inputKey)) {
                JOptionPane.showMessageDialog(this, "Invalid Category Admin Key for this category.");
                return;
            }

            String adminUsername = BookingRequestService.categoryAdminUsername(selected); 

            boolean ok = authService.loginAsUser(adminUsername);
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "Category admin account not found for this category.\n" +
                                "If you have old saved data, delete:\n" +
                                "%USERPROFILE%\\.Soft_Proj\\data.json\n" +
                                "Then run the app again.");
                return;
            }

            RepoStorage.save(repo);
            new AdminDashboardFrame(authService, bookingService, repo).setVisible(true);
            dispose();
            return;
        }

        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (!authService.login(username, password)) {
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
            return;
        }

        if (authService.getCurrentUser() instanceof Administrator) {
            String u = authService.getCurrentUser().getUsername();
            boolean isBigAdmin = u != null && u.equalsIgnoreCase("admin");

            if (!isBigAdmin) {
                authService.logout();
                JOptionPane.showMessageDialog(
                        this,
                        "Category Admin login is blocked here.\n" +
                                "Please enable \"Login for Category Admin\" to continue."
                );
                return;
            }

            RepoStorage.save(repo);
            new AdminDashboardFrame(authService, bookingService, repo).setVisible(true);
            dispose();
            return;
        }

        if (authService.getCurrentUser() instanceof Provider) {
            RepoStorage.save(repo);
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

        RepoStorage.save(repo);
        new MainDashboardFrame(authService, bookingService, repo, reminder).setVisible(true);
        dispose();
    }

    /**
     * Opens the sign-up window.
     */
    private void openSignUp() {
        new SignUpFrame(authService).setVisible(true);
    }
}