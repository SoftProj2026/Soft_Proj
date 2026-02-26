package presentation;

import Service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.DateTimeException;
import java.time.LocalDate;

/**
 * Sign-up window used to create a new user account.
 * <p>
 * Collects first/last name, username, strong password, and date of birth,
 * then calls {@link AuthService#register(String, String, String, String, LocalDate)}.
 * </p>
 */
public class SignUpFrame extends JFrame {

    private JTextField firstNameF = new JTextField(22);
    private JTextField lastNameF = new JTextField(22);

    private JTextField userF = new JTextField(22);
    private JPasswordField passF = new JPasswordField(22);

    private JCheckBox showPassword = new JCheckBox("Show Password");

    private JComboBox<Integer> dayBox = new JComboBox<>();
    private JComboBox<Integer> monthBox = new JComboBox<>();
    private JComboBox<Integer> yearBox = new JComboBox<>();

    private JLabel strongHint = new JLabel(
            "<html>Password must be strong:<br/>8+ chars, uppercase, lowercase, number, and symbol.</html>"
    );

    private JButton signBtn = new JButton("Sign Up");
    private JButton backBtn = new JButton("Back to Login");

    /**
     * Creates the sign-up frame.
     *
     * @param auth authentication service used for registration
     */
    public SignUpFrame(AuthService auth) {

        setTitle("Sign Up");
        setSize(650, 620);
        setMinimumSize(new Dimension(600, 560));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 246, 250));
        setContentPane(root);

        JPanel card = buildCard();

        JLabel title = new JLabel("Sign Up", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        installPlaceholder(firstNameF, "First Name");
        installPlaceholder(lastNameF, "Last Name");
        installPlaceholder(userF, "Username");
        installPasswordPlaceholder(passF, "Password");

        styleField(firstNameF);
        styleField(lastNameF);
        styleField(userF);
        styleField(passF);

        initDobCombos();
        styleCombo(dayBox);
        styleCombo(monthBox);
        styleCombo(yearBox);

        JPanel dobRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        dobRow.setOpaque(false);

        dobRow.add(new JLabelWhite("Age / Date of Birth:"));

        dobRow.add(new JLabelWhite("Day"));
        dobRow.add(dayBox);

        dobRow.add(new JLabelWhite("Month"));
        dobRow.add(monthBox);

        dobRow.add(new JLabelWhite("Year"));
        dobRow.add(yearBox);

        strongHint.setForeground(new Color(255, 255, 255, 235));
        strongHint.setFont(strongHint.getFont().deriveFont(12.5f));
        strongHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        showPassword.setOpaque(false);
        showPassword.setFocusPainted(false);
        showPassword.setForeground(new Color(255, 255, 255, 235));
        showPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        showPassword.addActionListener(e -> {
            String text = new String(passF.getPassword());
            boolean isPlaceholder = text.equals("Password") && passF.getEchoChar() == 0;

            if (showPassword.isSelected()) passF.setEchoChar((char) 0);
            else passF.setEchoChar(isPlaceholder ? (char) 0 : '•');
        });

        stylePrimaryButton(signBtn);
        styleLinkButton(backBtn);

        card.add(title);
        card.add(Box.createVerticalStrut(14));
        card.add(firstNameF);
        card.add(Box.createVerticalStrut(10));
        card.add(lastNameF);
        card.add(Box.createVerticalStrut(10));
        card.add(userF);
        card.add(Box.createVerticalStrut(10));
        card.add(passF);
        card.add(Box.createVerticalStrut(8));
        card.add(strongHint);
        card.add(Box.createVerticalStrut(10));
        card.add(showPassword);
        card.add(Box.createVerticalStrut(12));
        card.add(dobRow);
        card.add(Box.createVerticalStrut(16));
        card.add(signBtn);
        card.add(Box.createVerticalStrut(12));

        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        backWrap.setOpaque(false);
        backWrap.add(backBtn);
        card.add(backWrap);

        root.add(card, new GridBagConstraints());

        signBtn.addActionListener(e -> {
            String firstName = firstNameF.getText().trim();
            String lastName = lastNameF.getText().trim();
            String username = userF.getText().trim();
            String password = new String(passF.getPassword());

            if (firstName.equals("First Name")) firstName = "";
            if (lastName.equals("Last Name")) lastName = "";
            if (username.equals("Username")) username = "";
            if (password.equals("Password")) password = "";

            if (firstName.isEmpty()) { warn("First name is required."); return; }
            if (lastName.isEmpty())  { warn("Last name is required."); return; }
            if (username.isEmpty())  { warn("Username is required."); return; }

            if (!isStrongPassword(password)) {
                warn("Weak password!\nUse 8+ chars with uppercase, lowercase, number, and symbol.");
                return;
            }

            LocalDate dob = readDobOrNull();
            if (dob == null) {
                warn("Please select a valid date of birth.");
                return;
            }

            AuthService.RegisterResult result =
                    auth.register(firstName, lastName, username, password, dob);

            if (result == AuthService.RegisterResult.SUCCESS) {
                JOptionPane.showMessageDialog(this, "Account created successfully!");
                dispose();
            } else if (result == AuthService.RegisterResult.USERNAME_TAKEN) {
                JOptionPane.showMessageDialog(this,
                        "This username is already taken.\nPlease choose another one.",
                        "Username exists",
                        JOptionPane.WARNING_MESSAGE);
            } else if (result == AuthService.RegisterResult.UNDER_18) {
                JOptionPane.showMessageDialog(this,
                        "Registration rejected.\nYou must be at least 18 years old.",
                        "Under 18",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid input.\nPlease check your data and try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> dispose());
    }

    /**
     * Shows a warning dialog.
     *
     * @param msg warning message
     */
    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Initializes date-of-birth combos (day, month, year) with ranges.
     */
    private void initDobCombos() {
        dayBox.addItem(null);
        for (int d = 1; d <= 31; d++) dayBox.addItem(d);

        monthBox.addItem(null);
        for (int m = 1; m <= 12; m++) monthBox.addItem(m);

        int currentYear = LocalDate.now().getYear();
        yearBox.addItem(null);
        for (int y = currentYear; y >= currentYear - 100; y--) yearBox.addItem(y);
    }

    /**
     * Reads the selected DOB from combos and validates the date.
     *
     * @return LocalDate if valid; otherwise null
     */
    private LocalDate readDobOrNull() {
        Integer d = (Integer) dayBox.getSelectedItem();
        Integer m = (Integer) monthBox.getSelectedItem();
        Integer y = (Integer) yearBox.getSelectedItem();
        if (d == null || m == null || y == null) return null;

        try {
            return LocalDate.of(y, m, d);
        } catch (DateTimeException ex) {
            return null;
        }
    }

    /**
     * Builds the main sign-up card UI container.
     *
     * @return configured JPanel
     */
    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(24, 36, 22, 36));

        card.setBackground(new Color(70, 140, 210, 235));
        card.setBorder(new LineBorder(new Color(255, 255, 255, 70), 1, true));

        card.setPreferredSize(new Dimension(540, 470));
        return card;
    }

    /**
     * Styles a field for consistent UI look.
     *
     * @param f component to style
     */
    private void styleField(JComponent f) {
        f.setMaximumSize(new Dimension(420, 38));
        f.setPreferredSize(new Dimension(420, 38));
        f.setFont(f.getFont().deriveFont(14.5f));
        f.setBorder(new EmptyBorder(9, 12, 9, 12));
        f.setBackground(new Color(255, 255, 255, 235));
    }

    /**
     * Styles a combo box for consistent UI look.
     *
     * @param c combo box
     */
    private void styleCombo(JComboBox<?> c) {
        c.setPreferredSize(new Dimension(92, 30));
        c.setBackground(new Color(255, 255, 255, 235));
        c.setFont(c.getFont().deriveFont(13.5f));
    }

    /**
     * Styles the primary sign-up button with hover/pressed states.
     *
     * @param b button to style
     */
    private void stylePrimaryButton(JButton b) {
        Color normal = new Color(40, 95, 170);
        Color hover = new Color(55, 120, 205);
        Color pressed = new Color(30, 75, 140);

        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(normal);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        b.setMaximumSize(new Dimension(420, 40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.getModel().addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (!b.isEnabled()) { b.setBackground(normal); return; }
            if (m.isPressed()) b.setBackground(pressed);
            else if (m.isRollover()) b.setBackground(hover);
            else b.setBackground(normal);
        });
    }

    /**
     * Styles a link-like button.
     *
     * @param b button to style
     */
    private void styleLinkButton(JButton b) {
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(new Color(255, 255, 255, 230));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Checks if a password is strong:
     * 8+ chars, uppercase, lowercase, digit, and symbol.
     *
     * @param p password text
     * @return true if strong; false otherwise
     */
    private boolean isStrongPassword(String p) {
        if (p == null) return false;
        if (p.length() < 8) return false;

        boolean hasUpper = p.matches(".*[A-Z].*");
        boolean hasLower = p.matches(".*[a-z].*");
        boolean hasDigit = p.matches(".*[0-9].*");
        boolean hasSymbol = p.matches(".*[^A-Za-z0-9].*");

        return hasUpper && hasLower && hasDigit && hasSymbol;
    }

    /**
     * Installs placeholder behavior on a text field.
     *
     * @param field       target field
     * @param placeholder placeholder text
     */
    private void installPlaceholder(JTextField field, String placeholder) {
        field.setForeground(new Color(120, 120, 120));
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(30, 30, 30));
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(120, 120, 120));
                }
            }
        });
    }

    /**
     * Installs placeholder behavior on a password field,
     * including controlling echo char based on show/hide setting.
     *
     * @param field       target password field
     * @param placeholder placeholder text
     */
    private void installPasswordPlaceholder(JPasswordField field, String placeholder) {
        field.setForeground(new Color(120, 120, 120));
        field.setEchoChar((char) 0);
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                String text = new String(field.getPassword());
                if (text.equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(30, 30, 30));
                    field.setEchoChar(showPassword.isSelected() ? (char) 0 : '•');
                }
            }
            @Override public void focusLost(FocusEvent e) {
                String text = new String(field.getPassword()).trim();
                if (text.isEmpty()) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(new Color(120, 120, 120));
                }
            }
        });
    }

    /**
     * A white label used on colored backgrounds.
     */
    static class JLabelWhite extends JLabel {
        /**
         * Creates a white label.
         *
         * @param text label text
         */
        JLabelWhite(String text) {
            super(text);
            setForeground(new Color(255, 255, 255, 235));
        }
    }
}