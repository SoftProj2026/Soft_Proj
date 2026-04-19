package presentation;

import Service.AuthService;
import domain.User;
import persistence.DataRepository;
import persistence.RepoStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Displays the currently logged-in user's profile information and allows updating the email address.
 *
 * <p>The frame shows:</p>
 * <ul>
 *   <li>Username (read-only)</li>
 *   <li>Full name (first + last, read-only)</li>
 *   <li>Date of birth (read-only)</li>
 *   <li>Email address (editable)</li>
 * </ul>
 *
 * <p>Saving a new email validates basic format, updates the {@link User} object via
 * {@link User#setEmail(String)}, and persists changes via {@link RepoStorage#save(DataRepository)}.</p>
 *
 * @author remaa
 * @version 1.0
 */
public class UserProfileFrame extends JFrame {

    private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AuthService auth;
    private final DataRepository repo;

    private final JTextField emailField;

    /**
     * Creates the user profile frame for the currently logged-in user.
     *
     * @param auth authentication service (must not be null; a user must be logged in)
     * @param repo data repository used for persisting changes
     */
    public UserProfileFrame(AuthService auth, DataRepository repo) {
        this.auth = auth;
        this.repo = repo;

        setTitle("My Profile");
        setSize(480, 400);
        setMinimumSize(new Dimension(420, 340));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout(12, 12));

        add(buildHeader(), BorderLayout.NORTH);

        User user = auth.getCurrentUser();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD);
        form.setBorder(new EmptyBorder(20, 28, 20, 28));

        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(6, 0, 6, 14);
        lc.gridx = 0;

        GridBagConstraints vc = new GridBagConstraints();
        vc.anchor = GridBagConstraints.WEST;
        vc.fill = GridBagConstraints.HORIZONTAL;
        vc.weightx = 1.0;
        vc.insets = new Insets(6, 0, 6, 0);
        vc.gridx = 1;

        int row = 0;

        // Username (read-only)
        lc.gridy = row; vc.gridy = row++;
        form.add(fieldLabel("Username:"), lc);
        form.add(readOnlyField(user.getUsername()), vc);

        // Full name (read-only)
        lc.gridy = row; vc.gridy = row++;
        form.add(fieldLabel("Full Name:"), lc);
        String fullName = (user.getFirstName() + " " + user.getLastName()).trim();
        form.add(readOnlyField(fullName.isEmpty() ? "—" : fullName), vc);

        // Date of birth (read-only)
        lc.gridy = row; vc.gridy = row++;
        form.add(fieldLabel("Date of Birth:"), lc);
        String dob = user.getDateOfBirth() != null
                ? user.getDateOfBirth().format(DOB_FMT)
                : "—";
        form.add(readOnlyField(dob), vc);

        // Email (editable)
        lc.gridy = row; vc.gridy = row;
        form.add(fieldLabel("Email:"), lc);
        emailField = new JTextField(user.getEmail(), 22);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        form.add(emailField, vc);

        add(form, BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(UITheme.TEXT);
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("View your account information. You may update your email address.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(UITheme.MUTED);
        header.add(subtitle, BorderLayout.SOUTH);

        return header;
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(UITheme.BG);

        JButton saveBtn = UITheme.primaryButton("Save Changes");
        JButton closeBtn = UITheme.secondaryButton("Close");

        saveBtn.addActionListener(e -> onSave());
        closeBtn.addActionListener(e -> dispose());

        actions.add(saveBtn);
        actions.add(closeBtn);
        return actions;
    }

    private void onSave() {
        String newEmail = emailField.getText().trim();

        if (newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Email cannot be empty.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isValidEmail(newEmail)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        auth.getCurrentUser().setEmail(newEmail);
        RepoStorage.save(repo);

        JOptionPane.showMessageDialog(this,
                "Profile updated successfully!",
                "Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static boolean isValidEmail(String email) {
        if (email == null) return false;
        String e = email.trim();
        if (e.isEmpty()) return false;
        return e.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(UITheme.TEXT);
        return lbl;
    }

    private static JTextField readOnlyField(String value) {
        JTextField field = new JTextField(value);
        field.setEditable(false);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(240, 243, 250));
        field.setForeground(UITheme.MUTED);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 235), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }
}
