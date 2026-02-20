package presentation;

import Service.AuthService;
import Service.ScheduleService;
import javax.swing.*;
import java.awt.*;
import Service.BookingService;
import Service.ScheduleService;
import javax.swing.*;
import java.awt.*;


public class LoginFrame extends JFrame {
    private JTextField userF = new JTextField(15);
    private JPasswordField passF = new JPasswordField(15);
    private JButton logBtn = new JButton("Login");
    private JButton goSignBtn = new JButton("Sign Up");

    public LoginFrame(AuthService auth, ScheduleService sched,BookingService booking) {
        setTitle("Login");
        setSize(350, 200);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new JLabel("Username:")); add(userF);
        add(new JLabel("Password:")); add(passF);
        add(logBtn); add(goSignBtn);

        logBtn.addActionListener(e -> {
            if (auth.login(userF.getText(), new String(passF.getPassword()))) {
                new MainDashboardFrame(auth, sched,booking).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Credentials! Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        goSignBtn.addActionListener(e -> new SignUpFrame(auth).setVisible(true));
    }
}
