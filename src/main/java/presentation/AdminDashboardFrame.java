package presentation;

import Service.AuthService;
import Service.BookingService;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Minimal admin-only dashboard.
 * Shows basic system counts and provides logout back to LoginFrame.
 */
public class AdminDashboardFrame extends JFrame {

    public AdminDashboardFrame(AuthService auth, BookingService booking, DataRepository repo) {

        setTitle("Admin Dashboard");
        setSize(720, 360);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        root.add(title, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(3, 1, 10, 10));
        stats.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel users = new JLabel("Users count: " + repo.getUsers().size());
        JLabel slots = new JLabel("Slots count: " + repo.getSlots().size());
        JLabel appts = new JLabel("Appointments count: " + repo.getAppointments().size());

        users.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        slots.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        appts.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        stats.add(users);
        stats.add(slots);
        stats.add(appts);

        root.add(stats, BorderLayout.CENTER);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            auth.logout();
            new LoginFrame(auth, booking, repo).setVisible(true);
            dispose();
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(logout);
        root.add(south, BorderLayout.SOUTH);
    }
}