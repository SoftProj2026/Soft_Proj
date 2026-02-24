package presentation;

import Service.AuthService;
import Service.BookingService;
import persistence.DataRepository;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService auth;
    private final BookingService booking;
    private final DataRepository repo;

    public LoginFrame(AuthService auth, BookingService booking, DataRepository repo) {

        this.auth = auth;
        this.booking = booking;
        this.repo = repo;

        setTitle("Welcome");
        setSize(360, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Welcome", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        JButton signInBtn = new JButton("Sign In");
        JButton signUpBtn = new JButton("Sign Up");
        buttons.add(signInBtn);
        buttons.add(signUpBtn);

        add(buttons, BorderLayout.SOUTH);

        signInBtn.addActionListener(e -> {
            new SignInDialog(this, this.auth, this.booking, this.repo).setVisible(true);
        });

        signUpBtn.addActionListener(e -> {
            new SignUpFrame(this.auth, this.booking, this.repo).setVisible(true);
            dispose();
        });
    }

    private static class SignInDialog extends JDialog {

        private final JTextField usernameField = new JTextField(18);
        private final JPasswordField passwordField = new JPasswordField(18);

        public SignInDialog(JFrame owner,
                            AuthService auth,
                            BookingService booking,
                            DataRepository repo) {

            super(owner, "Sign In", true);

            setSize(420, 220);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(10, 10));

            JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
            form.add(new JLabel("Username:"));
            form.add(usernameField);
            form.add(new JLabel("Password:"));
            form.add(passwordField);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            JButton signInBtn = new JButton("Sign In");
            JButton cancelBtn = new JButton("Cancel");
            buttons.add(signInBtn);
            buttons.add(cancelBtn);

            add(form, BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);

            signInBtn.addActionListener(e -> {
                String u = usernameField.getText().trim();
                String p = new String(passwordField.getPassword());

                boolean ok = auth.login(u, p);
                if (ok) {
                    new MainDashboardFrame(auth, booking, repo).setVisible(true);

                    owner.dispose();

                    dispose();

                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid username or password.",
                            "Sign In Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelBtn.addActionListener(e -> dispose());
        }
    }
}