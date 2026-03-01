package presentation;

import Service.AuthService;
import Service.BookingService;
import Service.ReminderService;
import domain.Category;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Main booking dashboard UI.
 * <p>
 * Displays categories. When a category is clicked, it opens a unified booking window
 * that contains 3 columns:
 * <ol>
 *   <li>Company available</li>
 *   <li>My free slots</li>
 *   <li>Mutual slots (booking)</li>
 * </ol>
 * </p>
 * <p>
 * Also provides access to "My Bookings" and Logout.
 * </p>
 */
public class MainDashboardFrame extends JFrame {

    private final AuthService auth;
    private final BookingService booking;
    private final DataRepository repo;

    /** Reminder service used to stop reminders on logout (can be null). */
    private final ReminderService reminder;

    private static final Color BG = new Color(245, 248, 255);
    private static final Color BLUE = new Color(33, 120, 255);

    /**
     * Creates the main dashboard window.
     *
     * @param auth     authentication service (must be logged in to use booking screens)
     * @param booking  booking service
     * @param repo     repository containing categories, slots, and appointments
     * @param reminder reminder service (can be null)
     */
    public MainDashboardFrame(AuthService auth,
                              BookingService booking,
                              DataRepository repo,
                              ReminderService reminder) {

        this.auth = auth;
        this.booking = booking;
        this.repo = repo;
        this.reminder = reminder;

        setTitle("Booking Dashboard");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        getContentPane().setBackground(BG);

        JPanel categoryPanel = new JPanel(new GridLayout(0, 2, 16, 16));
        categoryPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        categoryPanel.setBackground(BG);

        List<Category> cats = repo.getCategories();
        for (Category c : cats) {
            JButton btn = createCategoryButton(c.getName());
            btn.addActionListener(e -> {
                new UnifiedBookingFrame(auth, booking, repo, c).setVisible(true);
            });
            categoryPanel.add(btn);
        }

        JScrollPane catScroll = new JScrollPane(categoryPanel);
        catScroll.setBorder(null);
        catScroll.getViewport().setBackground(BG);
        add(catScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(BG);

        JButton myBookingsBtn = new JButton("My Bookings");
        myBookingsBtn.addActionListener(e ->
                new MyBookingsFrame(auth, repo, reminder).setVisible(true)
        );

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            if (reminder != null) reminder.stop();

            auth.logout();
            new LoginFrame(auth, booking, repo).setVisible(true);
            dispose();
        });

        bottom.add(myBookingsBtn);
        bottom.add(logoutBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Creates a consistently styled category button.
     *
     * @param text button label (category name)
     * @return configured {@link JButton}
     */
    private JButton createCategoryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(BLUE);
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        return btn;
    }
}