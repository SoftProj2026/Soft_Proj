
package presentation;

import Service.AuthService;

import javax.swing.*;
import java.awt.*;

public class SignUpFrame extends JFrame {

    private JTextField userField = new JTextField(15);
    private JPasswordField passField = new JPasswordField(15);
    private JButton signBtn = new JButton("Create Account");

    public SignUpFrame(AuthService auth) {

        setTitle("Sign Up");
        setSize(300, 180);
        setLayout(new FlowLayout());
        setLocationRelativeTo(null);

        add(new JLabel("Username:"));
        add(userField);

        add(new JLabel("Password:"));
        add(passField);

        add(signBtn);

        signBtn.addActionListener(e -> {

            boolean success = auth.register(
                    userField.getText(),
                    new String(passField.getPassword())
            );

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Account created successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Username already exists or invalid input!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

