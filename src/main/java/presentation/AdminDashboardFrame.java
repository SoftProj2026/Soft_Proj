package presentation;

import Service.AuthService;
import Service.BookingService;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Admin-only dashboard window.
 * <p>
 * Displays aggregate counts (users, appointments, slots) and provides
 * a Logout button that clears the session and returns to {@link LoginFrame}.
 * </p>
 */
public class AdminDashboardFrame extends JFrame {

    private static final Color BG   = new Color(240, 244, 255);
    private static final Color BLUE = new Color(33, 120, 255);
    private static final Color RED  = new Color(220, 53, 69);

    private final AuthService    auth;
    private final BookingService booking;
    private final DataRepository repo;

    public AdminDashboardFrame(AuthService auth,
                               BookingService booking,
                               DataRepository repo) {
        this.auth    = auth;
        this.booking = booking;
        this.repo    = repo;

        setTitle("Admin Dashboard");
        setSize(600, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(16, 16));

        getContentPane().setBackground(BG);

        // ── Title ──────────────────────────────────────────────────────────
        JLabel titleLabel = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(BLUE);
        titleLabel.setBorder(new EmptyBorder(24, 0, 8, 0));
        add(titleLabel, BorderLayout.NORTH);

        // ── Stats panel ────────────────────────────────────────────────────
        JPanel stats = new JPanel(new GridLayout(1, 3, 20, 0));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(12, 40, 12, 40));

        stats.add(statCard("Users",        String.valueOf(repo.getUsers().size())));
        stats.add(statCard("Appointments", String.valueOf(repo.getAppointments().size())));
        stats.add(statCard("Slots",        String.valueOf(repo.getSlots().size())));

        add(stats, BorderLayout.CENTER);

        // ── Logout ─────────────────────────────────────────────────────────
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
        south.setOpaque(false);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setBackground(RED);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(10, 32, 10, 32));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            auth.logout();
            new LoginFrame(auth, booking, repo).setVisible(true);
            dispose();
        });

        south.add(logoutBtn);
        add(south, BorderLayout.SOUTH);
    }

    /** Creates a simple labelled count card. */
    private JPanel statCard(String label, String value) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 240), 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Segoe UI", Font.BOLD, 36));
        val.setForeground(BLUE);

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(80, 90, 110));

        card.add(val, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }
}
