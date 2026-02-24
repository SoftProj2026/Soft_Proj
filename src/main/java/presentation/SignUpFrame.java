package presentation;

import Service.AuthService;
import Service.BookingService;
import persistence.DataRepository;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class SignUpFrame extends JFrame {

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField dobField;     
    private JTextField residenceField;
    private JPasswordField passwordField;

    private final AuthService auth;
    private final BookingService booking;
    private final DataRepository repo;

    public SignUpFrame(AuthService auth, BookingService booking, DataRepository repo) {

        this.auth = auth;
        this.booking = booking;
        this.repo = repo;

        setTitle("Sign Up");
        setSize(520, 330);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));

        form.add(new JLabel("First Name:"));
        firstNameField = new JTextField();
        form.add(firstNameField);

        form.add(new JLabel("Last Name:"));
        lastNameField = new JTextField();
        form.add(lastNameField);

        form.add(new JLabel("Date of Birth (yyyy-MM-dd):"));
        dobField = new JTextField();
        form.add(dobField);

        form.add(new JLabel("Residence:"));
        residenceField = new JTextField();
        form.add(residenceField);

        form.add(new JLabel("Password (min 8, letters+digits+special):"));
        passwordField = new JPasswordField();
        form.add(passwordField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton createBtn = new JButton("Create Account");
        JButton backBtn = new JButton("Back");
        buttons.add(createBtn);
        buttons.add(backBtn);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        createBtn.addActionListener(e -> doSignUp());

        backBtn.addActionListener(e -> {
            new LoginFrame(auth, booking, repo).setVisible(true);
            dispose();
        });
    }

    private void doSignUp() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String residence = residenceField.getText().trim();
        String password = new String(passwordField.getPassword());
        String dobStr = dobField.getText().trim();

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr); 
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Use yyyy-MM-dd مثل 2005-01-31",
                    "Invalid DOB",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = auth.register(firstName, lastName, dob, residence, password);

        if (ok) {
            String usernameHint = normalizeName(firstName) + " " + normalizeName(lastName);

            JOptionPane.showMessageDialog(this,
                    "Account created successfully.\n"
                            + "Your username for Sign In is:\n"
                            + usernameHint + "\n\n"
                            + "Note: If another user already has the same name, the system will add a number automatically (e.g., "
                            + usernameHint + " 2).");

            new LoginFrame(auth, booking, repo).setVisible(true);
            dispose();

        } else {
            JOptionPane.showMessageDialog(this,
                    "Sign up failed.\n"
                            + "** Make sure everything is filled\n"
                            + "** The age eust be 18+\n"
                            + "** Password must be strong (at least 8 characters and include a letter/number/symbol",
                    "Sign Up Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String normalizeName(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }
}