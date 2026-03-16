package presentation;

import Service.AuthService;
import Service.BookingService;
import domain.Category;
import persistence.DataRepository;
import persistence.RepoStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Main dashboard window that lists booking categories and provides navigation
 * to other presentation screens (contact providers, my bookings, login).
 *
 * <p>The frame displays a grid of category buttons loaded from the repository.
 * Clicking a category opens {@link BookingTypeChoiceDialog} for that category.</p>
 *
 * @author remaa
 * @version 1.0
 */
public class MainDashboardFrame extends JFrame {

    /**
     * Authentication service used to manage login/logout and current user.
     */
    private final AuthService auth;

    /**
     * Booking service used by child screens if needed.
     */
    private final BookingService booking;

    /**
     * Repository providing categories and other persisted data.
     */
    private final DataRepository repo;

    /**
     * Background color used across the frame.
     */
    private static final Color BG = new Color(245, 248, 255);

    /**
     * Primary blue color used for category buttons.
     */
    private static final Color BLUE = new Color(33, 120, 255);

    /**
     * Create a new dashboard frame.
     *
     * @param auth    authentication service instance (must not be null)
     * @param booking booking service instance (must not be null)
     * @param repo    repository instance providing categories (must not be null)
     */
    public MainDashboardFrame(AuthService auth,
                              BookingService booking,
                              DataRepository repo) {

        this.auth = auth;
        this.booking = booking;
        this.repo = repo;

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
                BookingTypeChoiceDialog dialog = new BookingTypeChoiceDialog(this, c, repo, auth, booking);
                dialog.setVisible(true);
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
                new MyBookingsFrame(auth, repo).setVisible(true)
        );

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            RepoStorage.save(repo);
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
     * Create a styled JButton used for category entries.
     *
     * @param text button label text
     * @return configured JButton instance
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