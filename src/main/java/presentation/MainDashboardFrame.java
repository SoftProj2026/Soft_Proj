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
 * Main booking dashboard UI (Swing).
 * <p>
 * This is the primary home screen shown to a normal (non-admin, non-provider) user
 * after a successful login.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li><b>Categories grid</b>: Displays all {@link Category} entries from {@link DataRepository}.</li>
 *   <li><b>Open booking</b>: Clicking a category opens {@link UnifiedBookingFrame} for that category.</li>
 *   <li><b>Contact Companies</b>: Opens {@link CustomerContactProvidersFrame} to send a message to a provider.</li>
 *   <li><b>My Bookings</b>: Opens {@link MyBookingsFrame} to view and cancel bookings.</li>
 *   <li><b>Logout</b>: Logs out and returns to {@link LoginFrame}. Also stops {@link ReminderService}.</li>
 * </ul>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>This frame expects the user to already be logged in via {@link AuthService}.</li>
 *   <li>Reminder popups are managed by {@link ReminderService} which is stopped on logout.</li>
 * </ul>
 */
public class MainDashboardFrame extends JFrame {

    /** Authentication service used for logout and to ensure a user is logged in. */
    private final AuthService auth;

    /** Booking service used by booking windows that this dashboard opens. */
    private final BookingService booking;

    /** Repository containing categories, slots, appointments, and providers. */
    private final DataRepository repo;

    /**
     * Reminder service used to stop reminder notifications when the user logs out.
     * This may be {@code null} (depending on how the app is started).
     */
    private final ReminderService reminder;

    /** Background color used for the dashboard. */
    private static final Color BG = new Color(245, 248, 255);

    /** Primary accent color used for category buttons. */
    private static final Color BLUE = new Color(33, 120, 255);

    /**
     * Creates the main dashboard window.
     *
     * @param auth     authentication service (used for logout and current user)
     * @param booking  booking service (passed to booking screens)
     * @param repo     repository containing categories and bookings data
     * @param reminder reminder service instance (can be null)
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

        JButton contactBtn = new JButton("Contact Companies");
        contactBtn.addActionListener(e ->
                new CustomerContactProvidersFrame(auth, repo).setVisible(true)
        );

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

        bottom.add(contactBtn);
        bottom.add(myBookingsBtn);
        bottom.add(logoutBtn);

        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Creates a consistently styled category button used in the grid.
     * <p>
     * The button label is the category name. Clicking it opens {@link UnifiedBookingFrame}
     * for that category.
     * </p>
     *
     * @param text button label (category name)
     * @return configured Swing button
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