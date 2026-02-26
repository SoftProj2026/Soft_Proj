package presentation;

import Service.AuthService;
import Service.BookingService;
import persistence.DataRepository;
import domain.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.util.prefs.Preferences;

/**
 * Login window.
 * <p>
 * Allows user to login, open sign-up screen, and recover password
 * from the in-memory repository. Also supports "Keep Me Logged In"
 * by remembering username using {@link Preferences}.
 * </p>
 */
public class LoginFrame extends JFrame {

    private static final String PREF_NODE = "Soft_Proj";
    private static final String PREF_REMEMBER = "remember_me";
    private static final String PREF_USERNAME = "remembered_username";

    private final AuthService authService;
    private final BookingService bookingService;
    private final DataRepository repo;

    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton signUpButton;

    private JButton forgotBtn;
    private JButton registerBtn;

    private JCheckBox keepLoggedIn;

    private final Preferences prefs = Preferences.userRoot().node(PREF_NODE);

    /**
     * Creates the login frame.
     *
     * @param authService    authentication service
     * @param bookingService booking service (passed to next screens)
     * @param repo           data repository for users and appointments
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
     * Initializes UI components and layout.
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
        card.setPreferredSize(new Dimension(420, 300));

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

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        form.add(labelWhite("Username:"), c);
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        form.add(usernameField, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        form.add(labelWhite("Password:"), c);
        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        form.add(passwordField, c);

        keepLoggedIn = new JCheckBox("Keep Me Logged In");
        keepLoggedIn.setOpaque(false);
        keepLoggedIn.setForeground(new Color(255, 255, 255, 230));
        keepLoggedIn.setFocusPainted(false);

        c.gridx = 1; c.gridy = 2; c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        form.add(keepLoggedIn, c);

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        loginButton = new JButton("Log In");
        signUpButton = new JButton("Sign Up");

        buttons.add(loginButton);
        buttons.add(signUpButton);

        JPanel linksRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        linksRow.setOpaque(false);

        forgotBtn = linkButton("Forgot Password?");
        registerBtn = linkButton("New User? Register");
        JLabel sep = new JLabel("|");
        sep.setForeground(new Color(255, 255, 255, 200));

        linksRow.add(forgotBtn);
        linksRow.add(sep);
        linksRow.add(registerBtn);

        south.add(buttons);
        south.add(Box.createVerticalStrut(10));
        south.add(linksRow);

        card.add(south, BorderLayout.SOUTH);
        root.add(card);
    }

    /**
     * Creates a white colored label for dark backgrounds.
     *
     * @param text label text
     * @return configured JLabel
     */
    private JLabel labelWhite(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        return l;
    }

    /**
     * Applies consistent style to input fields.
     *
     * @param field text field component
     */
    private void styleField(JComponent field) {
        field.setFont(field.getFont().deriveFont(14.5f));
        field.setBackground(new Color(255, 255, 255, 235));
        field.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    /**
     * Creates a link-like button (transparent background, underlined behavior handled by UI).
     *
     * @param text link text
     * @return styled JButton
     */
    private JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(new Color(255, 255, 255, 230));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 12.5f));
        return b;
    }

    /**
     * Attaches UI event handlers (login, navigation, remember-me).
     */
    private void attachHandlers() {
        loginButton.addActionListener(e -> login());
        signUpButton.addActionListener(e -> openSignUp());

        registerBtn.addActionListener(e -> openSignUp());
        forgotBtn.addActionListener(e -> openForgotPasswordDialog());

        keepLoggedIn.addActionListener(e -> {
            boolean remember = keepLoggedIn.isSelected();
            prefs.putBoolean(PREF_REMEMBER, remember);
            if (!remember) {
                prefs.remove(PREF_USERNAME);
            }
        });

        getRootPane().setDefaultButton(loginButton);
    }

    /**
     * Loads remembered username if "Keep Me Logged In" was enabled previously.
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
     * Performs login and navigates to the main dashboard upon success.
     */
    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (authService.login(username, password)) {

            if (keepLoggedIn.isSelected()) {
                prefs.putBoolean(PREF_REMEMBER, true);
                prefs.put(PREF_USERNAME, username.trim());
            } else {
                prefs.putBoolean(PREF_REMEMBER, false);
                prefs.remove(PREF_USERNAME);
            }

            new MainDashboardFrame(authService, bookingService, repo).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
        }
    }

    /**
     * Opens the sign-up screen.
     */
    private void openSignUp() {
        new SignUpFrame(authService).setVisible(true);
    }

    /**
     * Shows a password recovery dialog by searching username in the repository.
     */
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