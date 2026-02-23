package presentation;

import Service.AuthService;
import Service.BookingService;
import persistence.DataRepository;

import javax.swing.*;
import java.awt.*;

/**
 * The login window of the appointment booking application.
 *
 * <p>Presents username and password input fields to the user. On successful
 * authentication the window is replaced by the {@link MainDashboardFrame};
 * otherwise an error dialog is shown. A "Sign Up" button opens the
 * {@link SignUpFrame} for new user registration.</p>
 */
public class LoginFrame extends JFrame {

    private JTextField userF = new JTextField(15);
    private JPasswordField passF = new JPasswordField(15);
    private JButton logBtn = new JButton("Login");
    private JButton goSignBtn = new JButton("Sign Up");

    /**
     * Constructs and configures the login window.
     *
     * @param auth    the {@link AuthService} used to authenticate the user
     * @param booking the {@link BookingService} forwarded to the dashboard after login
     * @param repo    the {@link DataRepository} forwarded to the dashboard after login
     */
    public LoginFrame(AuthService auth,
                      BookingService booking,
                      DataRepository repo) {

        setTitle("Login");
        setSize(350, 200);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        add(new JLabel("Username:"));
        add(userF);

        add(new JLabel("Password:"));
        add(passF);

        add(logBtn);
        add(goSignBtn);

        logBtn.addActionListener(e -> {

            if (auth.login(userF.getText(),
                    new String(passF.getPassword()))) {

                // ✅ نفتح الداشبورد الصح
                new MainDashboardFrame(auth, booking, repo)
                        .setVisible(true);

                this.dispose();

            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid Credentials! Account not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        goSignBtn.addActionListener(e ->
                new SignUpFrame(auth).setVisible(true));
    }
}