package presentation;

import Service.AuthService;
import Service.BookingService;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.net.URL;

public class LoginFrame extends JFrame {

    private JTextField userF = new JTextField(22);
    private JPasswordField passF = new JPasswordField(22);

    private JButton logBtn = new JButton("Log In");
    private JButton signUpBtn = new JButton("New User? Register");

    public LoginFrame(AuthService auth,
                      BookingService booking,
                      DataRepository repo) {

        setTitle("Login");
        setSize(1050, 620);
        setMinimumSize(new Dimension(900, 520));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        BackgroundPanel background = new BackgroundPanel("/resources/images/1120.png");
        background.setLayout(new GridBagLayout());
        setContentPane(background);

        JPanel card = buildGlassCard();

        JLabel title = new JLabel("Login", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        installPlaceholder(userF, "Username");
        installPasswordPlaceholder(passF, "Password");

        styleField(userF);
        styleField(passF);

        stylePrimaryButton(logBtn);
        styleLinkButton(signUpBtn);

        card.add(title);
        card.add(Box.createVerticalStrut(18));
        card.add(userF);
        card.add(Box.createVerticalStrut(10));
        card.add(passF);
        card.add(Box.createVerticalStrut(16));
        card.add(logBtn);
        card.add(Box.createVerticalStrut(14));

        JPanel signUpWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        signUpWrap.setOpaque(false);
        signUpWrap.add(signUpBtn);
        card.add(signUpWrap);

        GridBagConstraints outer = new GridBagConstraints();
        outer.anchor = GridBagConstraints.CENTER;
        background.add(card, outer);

        logBtn.addActionListener(e -> {
            String username = userF.getText().trim();
            String password = new String(passF.getPassword());

            if (username.equals("Username")) username = "";
            if (password.equals("Password")) password = "";

            if (auth.login(username, password)) {
                new MainDashboardFrame(auth, booking, repo).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid Credentials! Account not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // ✅ مهم: نفس auth object
        signUpBtn.addActionListener(e -> new SignUpFrame(auth).setVisible(true));
    }

    private JPanel buildGlassCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(26, 34, 22, 34));

        card.setBackground(new Color(25, 25, 25, 135));
        card.setBorder(new LineBorder(new Color(255, 255, 255, 55), 1, true));

        card.setPreferredSize(new Dimension(430, 300));
        return card;
    }

    private void styleField(JComponent f) {
        f.setMaximumSize(new Dimension(360, 38));
        f.setPreferredSize(new Dimension(360, 38));
        f.setFont(f.getFont().deriveFont(14.5f));
        f.setBorder(new EmptyBorder(9, 12, 9, 12));
        f.setBackground(new Color(255, 255, 255, 235));
    }

    private void stylePrimaryButton(JButton b) {
        Color normal = new Color(55, 90, 160);
        Color hover = new Color(75, 115, 200);
        Color pressed = new Color(35, 65, 125);

        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(normal);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(10, 12, 10, 12));
        b.setMaximumSize(new Dimension(360, 40));
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
        b.setForeground(new Color(255, 255, 255, 215));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
                    field.setEchoChar('•');
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

    static class BackgroundPanel extends JPanel {
        private final Image image;

        BackgroundPanel(String resourcePath) {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                throw new IllegalArgumentException("Background image not found at: " + resourcePath);
            }
            this.image = new ImageIcon(url).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = image.getWidth(this);
            int imgH = image.getHeight(this);

            if (panelW <= 0 || panelH <= 0 || imgW <= 0 || imgH <= 0) return;

            double scale = Math.max((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int) Math.ceil(imgW * scale);
            int drawH = (int) Math.ceil(imgH * scale);

            int x = (panelW - drawW) / 2;
            int y = (panelH - drawH) / 2;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(image, x, y, drawW, drawH, this);
            g2.dispose();
        }
    }
}