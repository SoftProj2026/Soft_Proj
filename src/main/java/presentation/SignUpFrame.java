package presentation;

import Service.AuthService;
import javax.swing.*;
import java.awt.*;

/**
 * The user registration window of the appointment booking application.
 *
 * <p>Allows new users to create an account by entering a username and a
 * password that meets the strong-password policy (minimum 8 characters,
 * at least one digit and one special character). Registration is handled
 * by {@link AuthService#register(String, String)}.</p>
 */
public class SignUpFrame extends JFrame {
    private JTextField userF = new JTextField(15);
    private JPasswordField passF = new JPasswordField(15);
    private JButton regBtn = new JButton("Register");

    /**
     * Constructs and configures the sign-up window.
     *
     * @param auth the {@link AuthService} used to validate and register the new user
     */
    public SignUpFrame(AuthService auth) {
        setTitle("Sign Up");
        setSize(300, 200);
        setLayout(new FlowLayout());
        add(new JLabel("Username:")); add(userF);
        add(new JLabel("Password (8+ chars, symbol, number):")); add(passF);
        add(regBtn);

        regBtn.addActionListener(e -> {
            String p = new String(passF.getPassword());
            if (!auth.isPasswordStrong(p)) {
                JOptionPane.showMessageDialog(this, "Password is too weak!");
            } else if (auth.register(userF.getText(), p)) {
                JOptionPane.showMessageDialog(this, "Account Created!");
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "User already exists!");
            }
        });
    }
}