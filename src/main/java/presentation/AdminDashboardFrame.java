package presentation;

import Service.AuthService;
import Service.BookingService;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Admin-only dashboard.
 * <p>
 * Provides a minimal admin UI:
 * <ul>
 *   <li>System statistics (counts)</li>
 *   <li>Navigation to the Admin Activity window</li>
 *   <li>Logout back to {@link LoginFrame}</li>
 * </ul>
 * </p>
 */
public class AdminDashboardFrame extends JFrame {

    private final JLabel users;
    private final JLabel providers;
    private final JLabel slots;
    private final JLabel appts;

    private final DataRepository repo;

    /**
     * Creates the admin dashboard window.
     *
     * @param auth    authentication service (used for logout)
     * @param booking booking service (passed to LoginFrame on logout)
     * @param repo    repository used to read system statistics/activity
     */
    public AdminDashboardFrame(AuthService auth, BookingService booking, DataRepository repo) {

        this.repo = repo;

        setTitle("Admin Dashboard");
        setSize(860, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        root.add(title, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(4, 1, 10, 10));
        stats.setBorder(new EmptyBorder(10, 10, 10, 10));

        users = new JLabel();
        providers = new JLabel();
        slots = new JLabel();
        appts = new JLabel();

        for (JLabel l : new JLabel[]{users, providers, slots, appts}) {
            l.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            stats.add(l);
        }

        root.add(stats, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton activityBtn = new JButton("User Activity");
        activityBtn.addActionListener(e -> new AdminActivityFrame(repo).setVisible(true));

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            auth.logout();
            new LoginFrame(auth, booking, repo).setVisible(true);
            dispose();
        });

        actions.add(activityBtn);
        actions.add(logout);

        JPanel south = new JPanel(new BorderLayout());
        south.add(actions, BorderLayout.EAST);
        root.add(south, BorderLayout.SOUTH);

        refreshCounts();
    }

    /**
     * Refreshes the displayed system counts.
     */
    private void refreshCounts() {
        users.setText("Users count: " + repo.getUsers().size());
        providers.setText("Providers count: " + repo.getProviders().size());
        slots.setText("Slots count: " + repo.getSlots().size());
        appts.setText("Appointments count: " + repo.getAppointments().size());
    }
}