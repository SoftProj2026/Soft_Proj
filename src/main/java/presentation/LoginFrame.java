package presentation;

import Service.AuthService;
import Service.BookingService;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final BookingService bookingService;
    private final DataRepository repo;

    private JTextField usernameField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton signUpButton;

    public LoginFrame(AuthService authService, BookingService bookingService, DataRepository repo) {
        this.authService = authService;
        this.bookingService = bookingService;
        this.repo = repo;

        initUI();
        attachHandlers();
    }

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
        card.setPreferredSize(new Dimension(420, 240));

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

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        loginButton = new JButton("Log In");
        signUpButton = new JButton("Sign Up");

        buttons.add(loginButton);
        buttons.add(signUpButton);

        card.add(buttons, BorderLayout.SOUTH);
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
        getRootPane().setDefaultButton(loginButton);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (authService.login(username, password)) {
            JOptionPane.showMessageDialog(this, "Login successful!");

            new MainDashboardFrame(authService, bookingService, repo).setVisible(true);

            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials.");
        }
    }

    private void openSignUp() {
        new SignUpFrame(authService).setVisible(true);
    }
}