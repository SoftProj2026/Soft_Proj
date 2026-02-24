package presentation;

import Service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class SignUpFrame extends JFrame {

    private JTextField userF = new JTextField(22);
    private JPasswordField passF = new JPasswordField(22);

    private JCheckBox showPassword = new JCheckBox("Show Password");

    // ✅ 2 lines warning
    private JLabel strongHint = new JLabel(
            "<html>Password must be strong:<br/>8+ chars, uppercase, lowercase, number, and symbol.</html>"
    );

    private JButton signBtn = new JButton("Sign Up");
    private JButton backBtn = new JButton("Back to Login");

    public SignUpFrame(AuthService auth) {

        setTitle("Sign Up");
        setSize(650, 540);
        setMinimumSize(new Dimension(600, 500));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // ✅ Plain background
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 246, 250));
        setContentPane(root);

        // ✅ Card only (light blue)
        JPanel card = buildCard();

        JLabel title = new JLabel("Sign Up", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        installPlaceholder(userF, "Username");
        installPasswordPlaceholder(passF, "Password");

        styleField(userF);
        styleField(passF);

        // Hint style
        strongHint.setForeground(new Color(255, 255, 255, 235));
        strongHint.setFont(strongHint.getFont().deriveFont(12.5f));
        strongHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Show password checkbox
        showPassword.setOpaque(false);
        showPassword.setFocusPainted(false);
        showPassword.setForeground(new Color(255, 255, 255, 235));
        showPassword.setAlignmentX(Component.CENTER_ALIGNMENT);

        // default echo char when user starts typing
        char defaultEcho = '•';

        showPassword.addActionListener(e -> {
            String text = new String(passF.getPassword());
            boolean isPlaceholder = text.equals("Password") && passF.getEchoChar() == 0;

            if (showPassword.isSelected()) {
                // show characters
                passF.setEchoChar((char) 0);
            } else {
                // hide characters (but keep placeholder readable)
                if (isPlaceholder) {
                    passF.setEchoChar((char) 0);
                } else {
                    passF.setEchoChar(defaultEcho);
                }
            }
        });

        stylePrimaryButton(signBtn);
        styleLinkButton(backBtn);

        // Layout inside card
        card.add(title);
        card.add(Box.createVerticalStrut(18));
        card.add(userF);
        card.add(Box.createVerticalStrut(10));
        card.add(passF);
        card.add(Box.createVerticalStrut(8));
        card.add(strongHint);
        card.add(Box.createVerticalStrut(10));
        card.add(showPassword);
        card.add(Box.createVerticalStrut(16));
        card.add(signBtn);
        card.add(Box.createVerticalStrut(14));

        JPanel backWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        backWrap.setOpaque(false);
        backWrap.add(backBtn);
        card.add(backWrap);

        root.add(card, new GridBagConstraints());

        // Actions
        signBtn.addActionListener(e -> {
            String username = userF.getText().trim();
            String password = new String(passF.getPassword());

            if (username.equals("Username")) username = "";
            if (password.equals("Password")) password = "";

            if (!isStrongPassword(password)) {
                JOptionPane.showMessageDialog(this,
                        "Weak password!\nUse 8+ chars with uppercase, lowercase, number, and symbol.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Username is required.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                // ✅ عدّل اسم الدالة حسب الموجود عندك:
                // boolean ok = auth.register(username, password);

                // مؤقتًا:
                boolean ok = true;

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Account created successfully!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not create account (username may already exist).",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Sign up failed: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        backBtn.addActionListener(e -> dispose());
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(28, 36, 24, 36));

        // ✅ Light blue card
        card.setBackground(new Color(70, 140, 210, 235));
        card.setBorder(new LineBorder(new Color(255, 255, 255, 70), 1, true));

        card.setPreferredSize(new Dimension(520, 360));
        return card;
    }

    private void styleField(JComponent f) {
        f.setMaximumSize(new Dimension(420, 38));
        f.setPreferredSize(new Dimension(420, 38));
        f.setFont(f.getFont().deriveFont(14.5f));
        f.setBorder(new EmptyBorder(9, 12, 9, 12));
        f.setBackground(new Color(255, 255, 255, 235));
    }

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
            if (!b.isEnabled()) {
                b.setBackground(normal);
                return;
            }
            if (m.isPressed()) b.setBackground(pressed);
            else if (m.isRollover()) b.setBackground(hover);
            else b.setBackground(normal);
        });
    }

    private void styleLinkButton(JButton b) {
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(new Color(255, 255, 255, 230));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private boolean isStrongPassword(String p) {
        if (p == null) return false;
        if (p.length() < 8) return false;

        boolean hasUpper = p.matches(".*[A-Z].*");
        boolean hasLower = p.matches(".*[a-z].*");
        boolean hasDigit = p.matches(".*[0-9].*");
        boolean hasSymbol = p.matches(".*[^A-Za-z0-9].*");

        return hasUpper && hasLower && hasDigit && hasSymbol;
    }

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
}